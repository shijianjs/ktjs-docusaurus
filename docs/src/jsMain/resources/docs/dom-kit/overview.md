---
sidebar_position: 1
---

# DOM kit

`ktjs-dom-kit` is the layer under the kit: helpers for writing components in Kotlin/JS against the
native DOM, with no knowledge of Docusaurus and no component runtime behind them.

It is a module of this build rather than a library of its own. The wrapper (`ktjs-docusaurus`) maps
npm packages to Kotlin declarations, the docs kit is this project's own UI, and this is the plumbing
both sit on — a base class a custom element can extend, an attribute delegate that makes the DOM the
single source of truth, a typed view of an element's inline style, a way to build a `CSSStyleSheet`
out of Kotlin values instead of CSS text, and a typed face for the `loadjs` loader.

## Why it lives here

The module depends on kotlin-browser and kotlin-csstype, not on Docusaurus, so it *could* be released
from a repository of its own. It is not: its documentation would then have to be written with the
very framework it helps build — a docs site about the helpers that build docs sites, published by the
site that consumes them — which is a cycle with no clean direction. Keeping the module and its
documentation in one build is what makes them consistent, so this repository is its home, not a
holding area.

The name is deliberately not prefixed with `ktjs-docusaurus`: the module is not part of the published
API and never reaches Dokka. It is documented here because it is how this project's components are
written, and because the alternatives are worse.

## What is in it

| File | Provides |
| --- | --- |
| `CustomElementAdapter.kt` | the base class: lifecycle hooks under their native names, the `attr` delegate host, and [`stripBridgesFrom`](./custom-elements.md) |
| `CustomElementDefinition.kt` | `defineIfMissing` — defining a tag after cleaning the class's prototype chain |
| `Attributes.kt` | the `attr` property delegate, `AttributeCodec`, `AttributeChange` |
| `AttributeCodecs.kt` | the `String` / `Boolean` / `Int` / `Double` codecs and `enumAttributeCodec` |
| `ShadowStyles.kt` | `attachShadowRoot` and `attachStyledShadow` |
| `InlineStyles.kt` | `HTMLElement.inlineStyle`, the element's own style seen as `csstype.Properties` |
| `DomExtensions.kt` | `TagName<T>.createElement { … }` |
| `loadjs/` | a wrapper for the [`loadjs`](https://github.com/kubetail-org/loadjs) loader: its four call shapes as overloads, the path prefixes (`css!`, `img!`, `module!`, `nomodule!`), and the bundle registry (`ready` / `done` / `reset` / `isDefined`) |
| `CssSheet.kt` | `cssSheet { … }` and `cssText { … }` |
| `css/` | what cssom does not model, filled in with typed values: `Pseudos`, `SupportsQuery`, `radialGradient`, `webkitBackgroundClip`, … |

## What it does not do

- **No component model.** There is no state, no `render()`, no diffing and no UI runtime. A component
  builds its subtree once and mutates the nodes it owns from then on — see
  [home sections](../kit/home-sections.md) for what that looks like in practice.
- **No HTML strings.** Nodes are created as nodes (`document.createElement` through a typed
  `TagName<T>`), never assembled from markup and re-parsed.
- **No `<style>` nodes.** A component's own style is either inline declarations on the node
  (`inlineStyle`) or a `CSSStyleSheet` adopted by a shadow root; a sheet belongs to the class, not to
  the instance.

## The pages

- [Custom elements](./custom-elements.md) — the adapter, the lifecycle hooks, and defining a tag.
- [Attributes](./attributes.md) — the `attr` delegate and its codecs.
- [Styling](./styling.md) — inline styles, shadow roots and adopted sheets.
- [Typed stylesheets](./css-sheet.md) — `cssSheet { … }`: the serializer, the typed values, the
  traps, and what to do when cssom has no type for something.
