---
sidebar_position: 3
---

# Local search

The search box in the navbar is `@easyops-cn/docusaurus-search-local`: the index is built from the
Markdown during `docusaurus build`, so there is no search server and no third-party crawler. The
`ktjs-docusaurus` wrapper maps its options, and this page uses them.

## Registering it

The package is a **theme**, not a plugin — it supplies `@theme/SearchBar` and `@theme/SearchPage` —
which is why it goes in `themes`. Docusaurus renders `@theme/SearchBar` in the navbar by itself, so
no navbar item is needed:

```kotlin
themes = arrayOf<PluginConfig?>(
    PluginConfig(
        "@easyops-cn/docusaurus-search-local",
        unsafeJso<SearchLocalOptions> {
            // Every language on the site: each one gets its own tokenizer.
            language = SearchLocalStringOrStringArray(arrayOf("en", "zh"))
            // The blog is not enabled here, so there is nothing to index.
            indexBlog = false
            // Content hash in the index URL, so a redeploy invalidates browser caches.
            hashed = BooleanOrHashed(Hasheds.query)
        },
    ),
)
```

`language` has to list every language the site publishes. `zh` is not a plain lunr language: the
package segments Chinese with jieba, which is why the option is `zh` and not `zh-CN`.

## What the options do

| Option | Used here | Why |
| --- | --- | --- |
| `language` | `["en", "zh"]` | both locales are indexed, each with its own tokenizer |
| `indexBlog` | `false` | the classic preset has no blog in this site |
| `hashed` | `"query"` | the index iframe is cached aggressively; the hash busts it |
| `searchBarPosition` | `"auto"` | left to the theme, which follows the navbar's own layout |
| `docsRouteBasePath` | default `/docs` | matches the preset's `routeBasePath` |

The rest of the upstream options are declared with the same names and types — `searchResultLimits`,
`ignoreFiles`, `searchContextByPaths`, `zhUserDict`, `fuzzyMatchingDistance` and so on. Unions keep
one factory per branch, so a polymorphic option can only be built through the wrapper:

```kotlin
ignoreFiles = IgnoreFiles(arrayOf(StringOrRegExp("/legacy"), StringOrRegExp(RegExp("^/v1/"))))
```

## Building

The index is a post-build step, so it is produced by `docusaurusBuild` (and by the dev server as you
edit pages):

```
build/js/docusaurus/docs/build/
├── search-index.json            # default locale
└── zh-Hans/search-index.json    # the other locale
```

Pages that are built but should not be found — drafts, `noIndex` pages — stay out of the index unless
`forceIgnoreNoIndex` is set. The `/search` route shows the full result list; the box in the navbar
shows suggestions.

## Keying in

`Ctrl`/`Cmd`+`K` focuses the box (`searchBarShortcutKeymap`, `"mod+k"` by default). The hint inside the
box can be hidden on its own with `searchBarShortcutHint = false` while the shortcut keeps working.
