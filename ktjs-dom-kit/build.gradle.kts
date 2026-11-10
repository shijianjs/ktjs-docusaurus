plugins {
    kotlin("multiplatform")
}

/**
 * Generic DOM helpers for Kotlin/JS, independent of Docusaurus.
 *
 * Currently one class: `CustomElementAdapter`, the base for custom elements written in Kotlin. It
 * lives in this repository while it is validated against the docs kit; it is deliberately **not**
 * published from here — the root build only releases modules whose name starts with
 * `ktjs-docusaurus` — and is meant to move to its own `ktjs-dom-kit` repository later.
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
