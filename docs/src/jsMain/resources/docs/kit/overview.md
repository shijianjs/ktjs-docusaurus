---
sidebar_position: 1
---

# The kit

`ktjs-docusaurus-kit` is not a wrapper. The wrapper (`ktjs-docusaurus`) maps npm packages; the kit
contains logic of its own, written against the DOM, and it deliberately has no upstream version to
follow — it carries its own semantic version.

It is not part of a working setup either: the wrapper plus the Gradle plugin already cover
Docusaurus. The kit is this project's own taste applied to its site — the pieces no npm package
provides — so depend on it only if you want the same look and the same TOC behaviour.

It exists because a documentation site needs three things that no npm package provides:

- **Home sections** — the hero, the feature grid and the "where to go next" cards. Plain DOM, built
  once, updated through setters.
- **A TOC toggle** — Docusaurus only offers `themeConfig.docs.sidebar.hideable` for the left
  sidebar; the right-hand table of contents has no collapse control.
- **Cheap widgets** that do not justify a React component or a component runtime.

## Why DOM and not components

A landing page is mostly static, and MDX carries the little dynamic part the sections need: the page
writes `<ktjs-hero>` / `<ktjs-features>` / `<ktjs-next-steps>` tags, and the client module upgrades
them once. No `.tsx` file, no React page, nothing to reconcile.

The sections build their own subtree once and mutate only the nodes they own afterwards, so a
re-render (or a second upgrade pass) never rebuilds a card. That also keeps the kit usable from a
site that has no React at all — a Markdown page plus a client module is enough.

## Icons

Icon names are [Iconify](https://iconify.design) names (`lucide:sparkles`,
`simple-icons:github`). The kit creates `<iconify-icon>` elements and lets the official package
fetch the glyph; no icon data ships with it, and a page with no icon costs nothing.

## Styling

Every class name is prefixed with `ktjs-`, and the palette is read from inherited custom
properties, so a site can restyle the sections without forking them:

```css
:root {
  --ktjs-accent: #1f6feb;
  --ktjs-accent-strong: #1a5fd0;
}
```

The sections are not rendered into a shadow root: they are mounted into elements the page owns, and
a shadow root would have to be opened on the page's node. The prefix and the custom properties are
what keep them out of the way instead.
