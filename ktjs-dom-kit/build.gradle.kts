import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl

plugins {
    kotlin("multiplatform")
}

/**
 * Generic DOM helpers for Kotlin/JS, independent of Docusaurus.
 *
 * `CustomElementAdapter` is the base for custom elements written in Kotlin; `attachShadowRoot` /
 * `attachStyledShadow` attach a shadow root (the latter adopting constructed `CSSStyleSheet`s), and
 * `HTMLElement.inlineStyle` is the typed view used to style a node in place. All of it lives in this
 * repository while it is validated against the docs kit; it is deliberately **not** published from
 * here — the root build only releases modules whose name starts with `ktjs-docusaurus` — and is meant
 * to move to its own `ktjs-dom-kit` repository later.
 *
 * The module shares the root build's KMP settings (`-Xir-dce=false`, ES2015 target, detekt) through
 * the `subprojects` blocks, so it needs no local copy of those workarounds.
 */
kotlin {
    js {
        useEsModules()
        browser {
            // The attribute delegates drive real attributes on a real custom element, and the change
            // notifications come from a `MutationObserver`; neither can be faked convincingly in a
            // Node test. Playwright's Chromium runs these browser tests (Karma is deprecated).
            @OptIn(ExperimentalJsTestDsl::class)
            test {
                chromium()
            }
        }
        binaries.library()
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            // `api`: the public surface exposes kotlin-browser types (`HTMLElement`, `Element`, …)
            // and kotlin-csstype's `Properties`, the typed view of an element's inline style.
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
            api("org.jetbrains.kotlin-wrappers:kotlin-csstype:2026.11.0-3.2.3")
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
