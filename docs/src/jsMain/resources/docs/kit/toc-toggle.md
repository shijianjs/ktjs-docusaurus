---
sidebar_position: 2
---

# TOC toggle

Docusaurus lets a reader hide the left sidebar (`themeConfig.docs.sidebar.hideable`) but has no
equivalent for the right-hand table of contents. `TocToggle` adds the button and the layout switch.

## Wiring

Two pieces: a client module that Docusaurus calls after every route change, and the CSS the toggle
injects itself.

```kotlin
private val toggle = TocToggle()

@JsExport
fun onRouteDidUpdate() {
    toggle.install()
    toggle.refresh()
}
```

`install()` is idempotent — it reads the stored preference and wires the viewport listener once.
`refresh()` reconciles the button with the page: the TOC is rendered by React, so it only exists on
pages with headings, and only on wide viewports. The button is created once and removed again on pages
without a TOC; it is re-inserted when React has rebuilt the column underneath it, so a re-render
cannot take it with it.

The Gradle plugin turns the export into `client-module.mjs`; the site lists it in `clientModules`.

## Layout

The button is inserted into the TOC column, directly above the list it controls, and pinned the same
way the list is: `position: sticky` just under the navbar. A button in the viewport's corner would be
simple, but the bottom-right corner already belongs to the theme's own "back to top" button, and the
two overlapped.

Collapsing shrinks the TOC column to a narrow strip instead of hiding it — the button lives in the
column, so hiding the column would hide the control that brings the TOC back. Only the list is hidden
inside the strip, and the article column (tagged `.ktjs-doc-article`) takes the freed width with
`max-width: 100% !important`: Docusaurus pins the article with `max-width: 75% !important` in a hashed
CSS module, so hiding the list alone would leave a 25% gap.

The button's own height is what pushes the TOC's sticky offset down (`calc(var(--ifm-navbar-height) +
3rem)`), which is what keeps the two from overlapping.

The toggle only appears at `min-width: 997px`, the same breakpoint Docusaurus uses for the desktop
TOC: below it there is no `theme-doc-toc-desktop`, and `refresh()` removes the button again.

## Why the class is on `<body>`

Docusaurus rewrites the whole `class` attribute of `<html>` on every route, which drops a class
applied there. `<body>` is left alone, so the state lives there as `ktjs-toc-collapsed`.

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

`en` is the fallback for a language without an entry.

## Storage failures are not errors

`localStorage` throws in private mode and when cookies are blocked. A failed read means "expanded";
a failed write means the choice is not remembered. Neither breaks the page.
