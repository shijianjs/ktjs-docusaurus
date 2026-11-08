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
pages with headings, only on wide viewports, and the button can be discarded by a re-render.

The Gradle plugin turns the export into `client-module.mjs`; the site lists it in `clientModules`.

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
