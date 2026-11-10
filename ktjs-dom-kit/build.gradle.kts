plugins {
    kotlin("multiplatform")
}

/**
 * Generic DOM helpers for Kotlin/JS, independent of Docusaurus.
 *
 * `CustomElementAdapter` is the base for custom elements written in Kotlin; `attachStyledShadow`
 * attaches a shadow root and adopts the constructed `CSSStyleSheet`s it is given. Both live in this
 * repository while they are validated against the docs kit; they are deliberately **not** published
 * from here — the root build only releases modules whose name starts with `ktjs-docusaurus` — and are
 * meant to move to their own `ktjs-dom-kit` repository later.
 *
 * The module shares the root build's KMP settings (`-Xir-dce=false`, ES2015 target, detekt) through
 * the `subprojects` blocks, so it needs no local copy of those workarounds.
 */
kotlin {
    js {
        useEsModules()
        browser()
        binaries.library()
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            // `api`: the public surface exposes kotlin-browser types (`HTMLElement`, `Element`, …).
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
        }
    }
}
