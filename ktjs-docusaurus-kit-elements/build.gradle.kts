plugins {
    kotlin("multiplatform")
}

/**
 * The browser half of the docs kit, published as a package of its own.
 *
 * `ktjs-docusaurus-kit` is usable from Node (Docusaurus loads it while building the site); this module
 * defines custom elements, so its classes evaluate `HTMLElement` at module load and must only ever be
 * loaded by a browser. The Gradle plugin publishes its linked output under `static/ktjs-client/` and
 * the site loads it after hydration.
 *
 * Both modules are shared packages: a site depends on the kit for the section classes and adds this
 * one as a browser-only bundle.
 *
 * The `CustomElementAdapter` base class lives in `ktjs-dom-kit`, a Docusaurus-independent module;
 * its linked output is emitted next to this module's into the same `kotlin/` directory, so the
 * browser bundle keeps working without an import map.
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
            implementation(project(":ktjs-docusaurus-kit"))
            implementation(project(":ktjs-dom-kit"))
        }
    }
}
