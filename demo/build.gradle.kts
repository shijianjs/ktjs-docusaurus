plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

/**
 * The demo is a documentation site, not a published module: Markdown lives in
 * `src/jsMain/resources`, configuration is Kotlin, and `./gradlew :demo:docusaurusBuild` turns both
 * into a static site.
 *
 * This module is loaded by Node (Docusaurus imports `docusaurus.config.mjs` from its bundle), so it
 * must not depend on `ktjs-docusaurus-kit-elements` — those classes evaluate `HTMLElement` at module load.
 * That package's linked output is published under `static/ktjs-client/` instead and loaded by the
 * browser after hydration (see `ClientModule.kt`).
 */
kotlin {
    js {
        useEsModules()
        nodejs()
        binaries.library()
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":ktjs-docusaurus"))
            implementation(project(":ktjs-docusaurus-kit"))
        }
    }
}

docusaurus {
    sidebarsFunction = "sidebars"

    // TOC toggle plus the browser bundle loader; runs after hydration on every route change.
    clientModuleFunction = "onRouteDidUpdate"

    // Browser-only package: the plugin copies `.mjs` files into static/ktjs-client/ and writes an
    // index.mjs that imports the bundle and calls registerHomeElements().
    browserBundle = "ktjs-docusaurus-ktjs-docusaurus-kit-elements.mjs"
    browserBundleEntry = "registerHomeElements"
    browserBundleFiles.from(
        project(":ktjs-docusaurus-kit-elements")
            .layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin"),
    )
}

// A file collection cannot carry a task dependency across projects, so link the browser package
// before the site is assembled.
tasks.named("docusaurusSite") {
    dependsOn(":ktjs-docusaurus-kit-elements:jsProductionLibraryCompileSync")
}
