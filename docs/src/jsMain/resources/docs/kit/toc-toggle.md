---
sidebar_position: 2
---

# TOC toggle

Docusaurus lets a reader hide the left sidebar (`themeConfig.docs.sidebar.hideable`) but has no
equivalent for the right-hand table of contents. `TocToggle` adds the control and the layout switch.

## Wiring

Two pieces, and no stylesheet of its own:

1. `TocToggle`, the Node-safe half. A client module calls it after every route change; it finds the
   desktop TOC and inserts a `<ktjs-toc-toggle>` tag in front of the list.
2. `<ktjs-toc-toggle>`, the browser half. It lives in the kit's browser bundle, owns the button, and
   changes the layout of the nodes it was inserted between.

```kotlin
private val toggle = TocToggle()

@JsExport
fun onRouteDidUpdate() {
    toggle.install()
    toggle.refresh()
}
```

`install()` is idempotent — it wires the viewport listener once. `refresh()` reconciles the tag with
the page: the TOC is rendered by React, so it only exists on pages with headings, and only on wide
viewports. The tag is removed on pages without a TOC and re-inserted when React has rebuilt the
column underneath it, so a re-render cannot take it with it.

The Gradle plugin turns the export into `client-module.mjs`; the site lists it in `clientModules`.
The tag itself is defined by the bundle the plugin publishes under `static/ktjs-client/`, which the
client module loads after hydration — the kit module never evaluates it (see
[Home sections](./home-sections.md)).

## The tag

```html
<ktjs-toc-toggle
  hide-label="Collapse table of contents"
  show-label="Expand table of contents"
  storage-key="ktjs:toc-collapsed"
></ktjs-toc-toggle>
```

The element derives everything else from where it sits, so it needs no Docusaurus selector:

| Node | Where it comes from | Collapsed state |
| --- | --- | --- |
| the column | `parentElement` | shrinks to `2.5rem` (`--ifm-col-width`) and drops its padding |
| the list | `nextElementSibling` | `display: none` |
| the article | the column's `previousElementSibling` | `max-width: 100% !important` |

Those are inline styles on exactly those nodes. Shrinking the column instead of hiding it is
deliberate: the button lives in the column, so hiding the column would hide the only way back. The
article is widened because Docusaurus pins it with `max-width: 75% !important` in a hashed CSS
module — hiding the list alone would leave a 25% gap.

`collapsed` is the state, and the attribute is the only place it lives. The button toggles it; the
element also seeds it from `localStorage` when it connects, and restores the layout when it
disconnects (a column left collapsed after the control is gone would have no way back).

The button's styles — sticky under the navbar, the pill, the arrow and its rotation — are the
element's own `CSSStyleSheet`, adopted by every instance. Nothing is written into `document.head`,
and nothing outside the component is styled by a page-wide rule. The chevron points right while the
list is expanded (the direction the column collapses towards) and left while it is collapsed, which
is where the list comes back from.

## Placement

The button is inserted into the TOC column, directly above the list it controls, and pinned the same
way the list is: `position: sticky` just under the navbar. A button in the viewport's corner would be
simple, but the bottom-right corner already belongs to the theme's own "back to top" button, and the
two overlapped.

The button's own height is what pushes the TOC's sticky offset down
(`calc(var(--ifm-navbar-height) + 3rem)`), which is what keeps the two from overlapping.

The toggle only appears at `min-width: 997px`, the same breakpoint Docusaurus uses for the desktop
TOC: below it there is no `theme-doc-toc-desktop`, and `refresh()` removes the tag again.

## Labels

The label follows `html-lang`, so a Chinese page gets Chinese labels:

```kotlin
TocToggle(
    TocToggleOptions(
        labels = mapOf(
            "en" to TocToggleLabels("Collapse table of contents", "Expand table of contents"),
            "zh-hans" to TocToggleLabels("收起目录", "展开目录"),
        ),
        storageKey = "ktjs:toc-collapsed",
    ),
)
```

`en` is the fallback for a language without an entry. The labels reach the element as `hide-label` /
`show-label`; without them the element falls back to its English defaults.

## Storage failures are not errors

`localStorage` throws in private mode and when cookies are blocked. A failed read means "expanded";
a failed write means the choice is not remembered. Neither breaks the page.
