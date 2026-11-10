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
        // `library` is what the release process publishes. KGP keeps only one binary per target, so
        // the browser bundle stays a set of linked `.mjs` files; the npm import is resolved by the
        // site's import map instead of a webpack pass.
        binaries.library()
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
    }
}

// The bare `iconify-icon` import in this library's linked output is resolved by the webpack pass in
// `ktjs-docusaurus-kit-bundle`; nothing has to be staged next to this module's output.
