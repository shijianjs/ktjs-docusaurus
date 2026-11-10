plugins {
    kotlin("multiplatform")
}

/**
 * Browser bundle for the docs kit's custom elements.
 *
 * `ktjs-docusaurus-kit-elements` stays a `library` so other Kotlin projects (ktjs-*, KMP docs) can
 * depend on it and get the klib metadata. This module links that library into the single file a site
 * actually loads, and because it is an `executable` the webpack pass inlines npm dependencies such as
 * `iconify-icon` — no bare module specifier reaches the browser, so no import map is needed.
 */
kotlin {
    js {
        useEsModules()
        browser {
            binaries.executable()
        }
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":ktjs-docusaurus-kit-elements"))
        }
    }
}
