---
sidebar_position: 1
---

# Home sections

A landing page is written in MDX: the section tags below read like the markup they produce, and each
one is a real custom element. No React page and no `.tsx` file.

```mdx
<ktjs-hero logo-src="img/favicon.svg" title="my-project" tagline="One sentence about it">
  <ktjs-action kind="primary" href="docs/intro">Get started</ktjs-action>
  <ktjs-action kind="secondary" href="https://github.com/…" icon="simple-icons:github">GitHub</ktjs-action>
</ktjs-hero>

<ktjs-features section-title="Why this project" tinted>
  <ktjs-feature icon="lucide:package" title="No npm project to maintain">
    Versions live in one package.json for Dependabot.
  </ktjs-feature>
</ktjs-features>

<ktjs-next-steps section-title="Where to go next">
  <ktjs-step href="docs/intro" title="Introduction">What the project is.</ktjs-step>
</ktjs-next-steps>
```

| Tag | Attributes | Children |
| --- | --- | --- |
| `ktjs-hero` | `logo-src`, `title`, `tagline` | `ktjs-action` (`kind`, `href`, `icon`), `ktjs-badge` (`href`, `src`, `alt`) |
| `ktjs-features` | `section-title`, `tinted` | `ktjs-feature` (`icon`, `title`), body text is the details |
| `ktjs-next-steps` | `section-title` | `ktjs-step` (`href`, `title`), body text is the details |

## Attributes are reactive

Each attribute in that table is a Kotlin delegate (`ktjs-dom-kit`'s `attr`), not a field read once on
connect:

```kotlin
class KtjsHero : CustomElementAdapter() {
    @JsName("logoSrc")
    var logoSrc: String by attr("logo-src") { change -> /* repaint from change.newValue */ }

    @JsName("titleText")
    var titleText: String by attr("title") { change -> headingElement.textContent = change.newValue }
}
```

- The getter reads the attribute and the setter writes it, so there is no second copy to keep in sync:
  `hero.titleText = "x"` and `hero.setAttribute("title", "x")` end up in the same place, and the getter
  sees an outside write immediately.
- The callback runs when the value actually changes — from the property, from `setAttribute`, from
  devtools. Writing the same value again is not a change; removing the attribute is.
- Call it from `connectedCallback()` with `applyInitialAttributes()`: markup that was already parsed
  never passes through a setter, so that is how the callbacks first hear about it. It is idempotent,
  so reconnecting an element does not repaint it twice.
- The codec follows the property type: `String`, `Int`, `Double` and `Boolean` (presence, like
  `hidden`) are built in; `kind` passes `enumAttributeCodec(ActionKind.entries)` to show the extension
  point for a custom type. `default = …` is what the getter reports while the attribute is absent — it
  is not written to the markup unless `applyInitialAttributes(writeDefaults = true)` asks for it.
- The attribute name is the property name in kebab case (`logoSrc` → `logo-src`); pass one when they
  differ (`titleText` → `title`, because `HTMLElement` already owns `title`). The properties carry
  `@JsName`: without it Kotlin/JS compiles a delegated property into mangled accessor methods and
  JavaScript sees no property at all. TypeScript users get a predictable `titleText` / `logoSrc`.

Two consequences:

- **Do not set an attribute on the element itself in its constructor.** `document.createElement("ktjs-hero")`
  throws `NotSupportedError` if the constructed element already has attributes (the parser and the
  upgrade path do not). The hero's `display: block` is a `:host` rule for that reason.
- A write from outside is reported on a microtask — the element watches itself with a
  `MutationObserver`, since the spec's `observedAttributes` list would have to repeat every attribute
  at `customElements.define`, before the delegates exist. A Kotlin write reports synchronously.

## How the components fit together

Every tag is its own custom element with its own shadow root — parents included. The parent renders the
frame (heading, grid, hero shell) and exposes a `<slot>`; the children render their own card. `ktjs-hero`
routes its children into named slots by tag name, so MDX does not need a `slot` attribute.

That means a card is not data handed to a section object: `<ktjs-feature>` is a node the browser
upgrades on its own, and the grid is ordinary CSS on the slot's assigned elements.

The elements package uses the official custom-element API: `class KtjsFeature : CustomElementAdapter()`
(which extends `HTMLElement`) plus a `CustomElementReference` companion and `customElements.define`.
`CustomElementAdapter` lives in `ktjs-dom-kit`, which knows nothing about Docusaurus: it declares
`connectedCallback()` once under its native name — subclasses just override it. Before defining a tag,
`defineIfMissing` calls `stripBridgesFrom`, which deletes the self-recursive bridges Kotlin/JS puts on
every class in the prototype chain; after that the native spelling works directly (`querySelectorAll`,
`append`, …), with no `…Super` forwarders (see `AGENTS.md`). The official `CustomElement` interface is
deliberately not implemented: its `val connectedCallback` compiles to a getter the browser never calls.

## Packages

| Package | Published | Contents |
| --- | --- | --- |
| `ktjs-docusaurus-kit` | yes, with the UI bundle | Node-safe helpers (`TocToggle`) + a `browser` artifact |
| `ktjs-docusaurus-kit-elements` | no | all home elements and their shadows |
| `ktjs-dom-kit` | no — it has [its own section](../dom-kit/overview.md) | `CustomElementAdapter`, `attr` + `AttributeCodec`, `defineIfMissing`, `createElement`, `attachShadowRoot`, `attachStyledShadow`, `inlineStyle`, `cssSheet` / `cssText` |

The home UI travels with the kit: the webpack bundle is attached to the kit's coordinate as a
`browser` artifact, so another project needs one dependency and one copy:

```kotlin
val browserUi by configurations.creating
dependencies {
    browserUi("io.github.shijianjs:ktjs-docusaurus-kit:<version>") {
        artifact {
            classifier = "browser"
            type = "js"
        }
    }
}

tasks.register<Copy>("stageHomeUi") {
    from(browserUi)
    into("static/ktjs-client")
}
```

The elements module itself is not published: it is this site's UI, not a reusable contract.

Because those classes evaluate `HTMLElement` at module load, the elements bundle must never be imported
by the Node side. The Gradle plugin publishes it under `static/ktjs-client/` and the client module
inserts its `<script type="module">` **after hydration**:

```kotlin
// build.gradle.kts
docusaurus {
    clientModuleFunction = "onRouteDidUpdate"
    browserBundle = "ktjs-docusaurus-ktjs-docusaurus-kit-elements.mjs"
    browserBundleEntry = "registerHomeElements"
    browserBundleFiles.from(/* the elements module's productionLibrary/kotlin output */)
}
```

Loading after hydration means React has already rendered the tags, so there is no hydration mismatch.

The js targets still compile at ES2015 (set once in the root build). Kotlin/JS 2.4 accepts only
`es5` / `es2015`; `es2020` arrives with 2.5.

## Links

Write `href`s **relative to the page** — `docs/intro`, not `/docs/intro`. The same MDX file then works
under every locale prefix (`/my-site/` and `/my-site/zh-Hans/`) without resolving a base URL.

## Icons

`icon` values are Iconify names (`lucide:sparkles`, `simple-icons:github`). The elements wrap
`<iconify-icon>` through [`ktjs-iconify`](https://github.com/shijianjs/ktjs-iconify) — `registerIconifyIcon()`
pulls in the official package, and the glyph is fetched at runtime.

The library bundle leaves `import 'iconify-icon'` as a bare specifier, so the site ships the npm ESM file
next to the bundle and declares an import map in `headTags`:

```kotlin
// docusaurus.config.kt
headTags = arrayOf(
    unsafeJso<HtmlTagObject> {
        tagName = "script"
        attributes = /* type = "importmap" */
        innerHTML = """{"imports":{"iconify-icon":"/my-site/ktjs-client/iconify-icon.mjs"}}"""
    },
)
```

## Styling

Every element renders into a shadow root, so the page stylesheet cannot leak in and these rules cannot
leak out. Theme changes cross the boundary through inherited custom properties:

```css
:root {
  --ktjs-accent: #1f6feb;
  --ktjs-accent-strong: #1a5fd0;
  --ktjs-card-bg: var(--ifm-background-surface-color);
}
```

Every `--ktjs-*` token falls back to an Infima `--ifm-*` value, so light and dark mode work without a
single `[data-theme]` selector inside the shadow trees.

Styles live where the element is written, in three grades:

| Where | What goes there | Example |
| --- | --- | --- |
| `inlineStyle.apply { … }` in the element's constructor | everything a plain declaration can say | `display = Display.flex`, `padding = Padding(1.5.rem)`, `color = accent` |
| one sheet of that element's own (`cssSheet { … }` in its `companion object`) | what inline styles cannot: `:hover`, `::before`, `@media`, `@supports` | the button's hover colours, the hero's glow blobs |
| the sheet every element adopts | what all of them need, or what several genuinely share | `box-sizing` and `:host { display: block }` in `HomeReset.kt` |

Values are typed (`Display.flex`, `1.5.rem`, `Color("var(--ktjs-accent, …)")`, `clamp(…)`), never
strings built by hand. `ktjs-dom-kit` exposes the mechanics: `HTMLElement.inlineStyle`, the element's
own `CSSStyleDeclaration` seen as `csstype.Properties`, and `attachStyledShadow(sheet…)`, which attaches
the root and adopts the sheets it is given.

A component's own rules are **one `CSSStyleSheet` per class**, built once in that class's companion
object and adopted by every instance — Lit's `static styles`, in the place Kotlin keeps a class's
statics. That is why no `<style>` node is ever created: a hundred cards share one parsed sheet instead
of carrying a hundred copies of the same text. A component's rules
therefore never travel into a shared file, and no element carries a stylesheet it does not paint with.

The rules themselves are written as a typed object, not as CSS text:

```kotlin
private val SHEET: CSSStyleSheet = cssSheet {
    cardClass {
        border = Border(1.px, LineStyle.solid, var(cardBorderHook, rgb(127, 127, 127, 0.24)))
        transition = many(Transition(PropertyName.borderColor, 0.2.s, TransitionTimingFunction.ease))
        Pseudos.hover { borderColor = var(accentHook, var(primaryColorHook)) }
    }
}
```

`ktjs-dom-kit` serializes that object through the same two packages emotion's cache uses
(`@emotion/serialize` + `stylis`), so what lands in the sheet is what emotion would emit. Property
names, values, units and pseudo-class structure come from types; the only strings left are the theme's
custom property names; state variants come from `Pseudos` + `pseudo` / `pseudos` in the same package.

What cssom does not model lives in `ktjs-dom-kit`'s own `css` package instead of back in a string:
`radialGradient` / `colorStop` (cssom ships the linear and angular shapes only),
`webkitBackgroundClip` (the prefixed property is missing) and `pointerEventsNone` (the value union
lists only the SVG keywords). Every element, the hero included, builds its sheet the same way.

## From Kotlin

Building a section in code uses the same elements — create, set attributes, append:

```kotlin
val hero = KtjsHero.NAME.createElement {
    logoSrc = "img/ktjs-docusaurus-logo.png"
    titleText = "my-project"
}
hero.appendChild(
    KtjsAction.NAME.createElement {
        kind = ActionKind.Primary
        href = "docs/intro"
        textContent = "Get started"
    },
)
host.appendChild(hero)
```

The properties write the attributes, so this is the same element MDX produces. `TagName<T>.createElement { }`
is the typed wrapper over `document.createElement(TagName<T>)`; the callback receives the concrete
element type.
