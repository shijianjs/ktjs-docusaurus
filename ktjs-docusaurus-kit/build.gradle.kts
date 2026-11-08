plugins {
    kotlin("multiplatform")
}

/**
 * The kit is **not** a wrapper: it has no upstream npm package to follow, so it keeps its own
 * semantic version instead of the `{npm version}-kt.{revision}` scheme. Publishing, signing and the
 * POM metadata are configured once for every published module in the root build.
 */
group = "io.github.shijianjs"
version = "0.1.0"
description = "Reusable Kotlin/JS building blocks for Docusaurus sites: home sections and a TOC toggle."

kotlin {
    js {
        useEsModules()
        browser()
        binaries.library()
    }

    compilerOptions {
        // The library output must keep every public declaration: the Kotlin/JS linker
        // dead-code-eliminates a library down to its @JsExport roots by default, which would leave
        // this module's classes missing from the bundle a consumer imports. See AGENTS.md.
        freeCompilerArgs.add("-Xir-dce=false")
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")

            // Side-effect dependency: registers the <iconify-icon> element the sections create.
            implementation(npm("iconify-icon", "3.0.4"))
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
