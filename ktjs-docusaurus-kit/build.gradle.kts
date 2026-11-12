import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

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
        // `-Xir-dce=false` is set once for every KMP subproject in the root build: see the DCE note
        // in AGENTS.md.
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

/**
 * The kit ships the home UI as data: the browser bundle is attached to the kit's own coordinate with
 * a `browser` classifier, so a site depends on the kit and copies the artifact into `static/` — no
 * second library coordinate, and the version cannot drift.
 *
 * The `kotlinMultiplatform` publication is created by the Kotlin plugin and configured by the root
 * build's shared publishing block, both of which finish after this script; hence `afterEvaluate`.
 */
// `builtBy` takes a task, not a task path (`builtBy(":…:jsBrowserDistribution")` fails with "Cannot
// convert … to a task"), so the browser project must be evaluated before its task can be looked up.
// That is the only reason this script reaches into another project.
evaluationDependsOn(":ktjs-docusaurus-kit-browser")

afterEvaluate {
    val bundleProject = project(":ktjs-docusaurus-kit-browser")
    val bundleTask = bundleProject.tasks.named("jsBrowserDistribution")
    val bundleFile = bundleProject.layout.buildDirectory
        .file("dist/js/productionExecutable/ktjs-docusaurus-kit-browser.js")
        .get()
        .asFile
    extensions.configure<PublishingExtension> {
        publications.named<MavenPublication>("kotlinMultiplatform") {
            artifact(bundleFile) {
                classifier = "browser"
                extension = "js"
                builtBy(bundleTask)
            }
        }
    }
}
