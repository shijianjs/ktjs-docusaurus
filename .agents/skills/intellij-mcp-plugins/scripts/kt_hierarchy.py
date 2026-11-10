#!/usr/bin/env python3
"""Kotlin type hierarchy (parents + subtypes) via the Index MCP plugin.

The plugin's ide_type_hierarchy/find_implementations/find_super_methods do not bridge Kotlin PSI,
but ide_symbol_info (signature incl. supertypes), ide_find_symbol (symbolId, Kotlin + libraries) and
ide_find_references (its hits include `class X : Base()` declarations) all work on Kotlin.

UP   : ide_symbol_info -> signature -> supertype names -> ide_find_symbol -> symbolId -> recurse
DOWN : ide_find_references -> inheritance declarations -> ide_find_symbol -> recurse

Usage:
  python kt_hierarchy.py <file> <line> <column> [--project PATH] [--url URL] [--up N] [--down N]
"""

import argparse
import os
import re

from mcp_client import Client


def info_at(client, file, line, column):
    return client.call("ide_symbol_info", {"target": {"position": {"file": file, "line": line, "column": column}}})


def info_of(client, symbol_id):
    return client.call("ide_symbol_info", {"target": {"symbolId": symbol_id}})


def archive_of(path):
    if "!/" in path:
        return path.split("!/")[0]
    return "project" if not path.endswith(".class") else path.rsplit("/", 1)[0]


def find_symbol(client, name, prefer_file=None):
    """Resolve a possibly-short supertype name, preferring the declaring class's own file/archive."""
    data = client.call("ide_find_symbol", {
        "query": name, "language": "Kotlin", "scope": "project_and_libraries", "pageSize": 50,
    })
    symbols = data.get("symbols", [])
    exact = [s for s in symbols if s["name"] == name] or symbols
    if not exact:
        return None

    def score(symbol):
        rank = 4
        if prefer_file:
            if symbol["file"] == prefer_file:
                rank = 0
            elif os.path.dirname(symbol["file"]) == os.path.dirname(prefer_file):
                rank = 1
            elif archive_of(symbol["file"]) == archive_of(prefer_file):
                rank = 2
        if symbol["file"].endswith(".class"):
            rank += 10
        if symbol["kind"] not in ("CLASS", "INTERFACE", "ENUM", "SYMBOL"):
            rank += 5
        return rank

    return sorted(exact, key=score)[0]


def supertypes(signature):
    match = re.search(r":\s*(.+)$", signature or "")
    if not match:
        return []
    names = []
    for part in match.group(1).split(","):
        part = part.strip().rstrip("()").split("<", 1)[0].strip()
        if part and part != "Companion":
            names.append(part)
    return names


def up(client, symbol_id, file_hint=None, depth=0, seen=None):
    seen = seen or set()
    if symbol_id in seen or depth > 8:
        return []
    seen.add(symbol_id)
    result = []
    for name in supertypes(info_of(client, symbol_id)["signature"]):
        target = find_symbol(client, name, file_hint)
        if not target:
            result.append((name, None))
            continue
        result.append((name, target))
        if not target["file"].endswith(".class"):
            result += up(client, target["symbolId"], target["file"], depth + 1, seen)
    return result


def down(client, file, line, column, depth=0, seen=None):
    seen = seen or set()
    if depth > 8:
        return []
    data = client.call("ide_find_references", {
        "target": {"position": {"file": file, "line": line, "column": column}},
    })
    result = []
    for usage in data.get("usages", []):
        context = usage.get("context", "")
        match = re.search(r"\b(class|interface|object|enum class)\s+(\w+)", context)
        if not match or ":" not in context:
            continue
        sub = match.group(2)
        if sub in seen:
            continue
        seen.add(sub)
        target = find_symbol(client, sub, file)
        if not target:
            result.append((sub, usage.get("file"), usage.get("line")))
            continue
        result.append((sub, target["file"], target["line"]))
        result += down(client, target["file"], target["line"], target["column"], depth + 1, seen)
    return result


def main():
    parser = argparse.ArgumentParser(description="Kotlin type hierarchy via IDE index")
    parser.add_argument("file")
    parser.add_argument("line", type=int)
    parser.add_argument("column", type=int)
    parser.add_argument("--project", default=os.environ.get("IDE_MCP_PROJECT"))
    parser.add_argument("--url", default=None)
    parser.add_argument("--up", type=int, default=8)
    parser.add_argument("--down", type=int, default=8)
    args = parser.parse_args()

    client = Client(args.url, project=args.project)
    start = info_at(client, args.file, args.line, args.column)
    print(f"Hierarchy for: {start['name']}  ({args.file}:{args.line})  [{start.get('signature')}]")

    print("\nUP (supertypes):")
    for depth, (name, target) in enumerate(up(client, start["symbolId"], args.file)):
        where = f"{target['file']}:{target['line']}" if target else "unresolved"
        print(f"  {'  ' * depth}{'└ ' if depth else ''}{name}  @ {where}")

    print("\nDOWN (subtypes):")
    for name, file, line in down(client, args.file, args.line, args.column):
        print(f"  - {name}  @ {file}:{line}")


if __name__ == "__main__":
    main()
