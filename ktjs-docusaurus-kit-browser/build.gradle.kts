import org.jetbrains.kotlin.gradle.ExperimentalJsTestDsl

plugins {
    kotlin("multiplatform")
}

/**
 * The browser half of the docs kit: every custom element the site defines, in one webpack bundle.
 *
 * `ktjs-docusaurus-kit` is usable from Node (Docusaurus evaluates it while building and prerendering
 * the site); this module defines custom elements, so its classes evaluate `HTMLElement` at module
 * load and must only ever be loaded by a browser. The Gradle plugin publishes the bundle under
 * `static/ktjs-client/` and the site loads it after hydration.
 *
 * It is an `executable` rather than a `library` on purpose: nothing consumes its Kotlin API, and the
 * executable link is what runs the webpack pass that inlines npm dependencies such as
 * `iconify-icon` — no bare module specifier reaches the browser, so no import map is needed.
 * [browser.MainKt] is the whole entry point.
 *
 * Not published: this is the site's UI, not a reusable contract. What other projects consume is the
 * bundle, distributed as the kit's `browser` artifact.
 *
 * The `CustomElementAdapter` base class lives in `ktjs-dom-kit`, a Docusaurus-independent module;
 * its linked output is emitted next to this module's into the same `kotlin/` directory, so the
 * browser bundle keeps working without an import map.
 */
kotlin {
    js {
        useEsModules()
        browser {
            // These are the site's elements, so their tests check them in a real browser: markup in,
            // shadow tree out, attributes changed afterwards. Playwright's Chromium runs these
            // browser tests (Karma is deprecated).
            @OptIn(ExperimentalJsTestDsl::class)
            test {
                chromium()
            }

            binaries.executable()
        }
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":ktjs-dom-kit"))
            // Wrapper for <iconify-icon>; it also registers the element and ships the npm package.
            implementation("io.github.shijianjs:ktjs-iconify-icon:3.0.4-kt.6")
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
