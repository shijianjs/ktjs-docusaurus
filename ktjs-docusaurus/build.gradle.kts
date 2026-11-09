plugins {
    kotlin("multiplatform")
}

// Wrapper version = {upstream npm version}-kt.{revision}. See the versioning section in AGENTS.md.
@Suppress("UNCHECKED_CAST")
val npmVersions = rootProject.extra["npmVersions"] as Map<String, String>
@Suppress("UNCHECKED_CAST")
val wrapperRevisions = rootProject.extra["wrapperRevisions"] as Map<String, Int>

// Every Docusaurus package is published in lockstep, so any of them is a valid version anchor.
val docusaurusVersion = npmVersions.getValue("@docusaurus/core")
// Added by the single first upstream package, so it is looked up through its own key.
val prismReactRendererVersion = npmVersions.getValue("prism-react-renderer")
val wrapperRevision = wrapperRevisions.getValue(project.name)

version = "$docusaurusVersion-kt.$wrapperRevision"
description = "Kotlin/JS wrapper for Docusaurus: typed site configuration, presets, plugins and theme options."

kotlin {
    js {
        useEsModules()

        // The configuration runs in Node (it is what `docusaurus build` loads), so the test runtime
        // is Node as well and the whole module stays free of DOM dependencies.
        nodejs()

        binaries.library()
    }

    compilerOptions {
        // `-Xir-dce=false` is set once for every KMP subproject in the root build: see the DCE note
        // in AGENTS.md.
        // `kotlin.js.JsAny` is the type of every upstream member declared as `unknown`.
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            // `js.objects.unsafeJso` / `js.objects.Record` live in kotlin-wrappers' `kotlin-js`
            // module; the old standalone `kotlin-js-plain-objects` artifact stops at Kotlin 2.2.
            api("org.jetbrains.kotlin-wrappers:kotlin-js:2026.11.0")

            // Client APIs and theme components are React: a site that swizzles a component or writes a
            // page uses the official Kotlin binding instead of a hand-written createElement surface.
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
            api("org.jetbrains.kotlin-wrappers:kotlin-react:2026.11.0-19.3.1")
            implementation(npm("react", npmVersions.getValue("react")))
            implementation(npm("react-dom", npmVersions.getValue("react-dom")))

            // Declared here so the npm workspace installs them next to the wrapper: the generated
            // configuration imports them at runtime and the demo site consumes them through it.
            implementation(npm("@docusaurus/core", docusaurusVersion))
            implementation(npm("@docusaurus/preset-classic", docusaurusVersion))
            implementation(npm("prism-react-renderer", prismReactRendererVersion))
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// The Node test task is not part of `check` for a Node-only js target, so `./gradlew build` would skip
// the tests entirely. Wire it in explicitly.
tasks.named("check") {
    dependsOn("jsNodeTest")
}
