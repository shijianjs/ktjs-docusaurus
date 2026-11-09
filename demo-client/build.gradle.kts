plugins {
    kotlin("multiplatform")
}

/**
 * The demo site's browser half: the landing-page custom elements and the TOC toggle.
 *
 * It is a separate module because a custom-element class reads `HTMLElement` when the module loads,
 * and the configuration's bundle is loaded by Node, which has none.
 */
kotlin {
    js {
        useEsModules()
        browser()
        binaries.library()

        // The js target compiles at ES2015 (set once for every module in the root build), which is
        // what makes the element classes below real `class X extends HTMLElement` declarations.
        // Without it the browser throws "Please use the 'new' operator".
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":ktjs-docusaurus-kit"))
        }
    }
}
