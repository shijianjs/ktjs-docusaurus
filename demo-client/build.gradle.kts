plugins {
    kotlin("multiplatform")
}

/**
 * The demo site's browser half: it registers the landing-page custom elements and drives the TOC
 * toggle.
 *
 * It is a separate module because `class KtjsHero : HTMLElement()` reads `HTMLElement` at module load,
 * and the configuration's bundle is loaded by Node. Keeping the element classes here means the config
 * never imports them; the plugin points `client-module.mjs` at this module's output instead.
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
        }
    }
}
