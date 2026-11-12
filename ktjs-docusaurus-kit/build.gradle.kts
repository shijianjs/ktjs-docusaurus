import java.util.zip.ZipFile
import org.gradle.jvm.tasks.Jar

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

/**
 * Stages the browser module's webpack output under the path it has inside the klib.
 *
 * A single file is copied rather than the whole `dist/js/productionExecutable` directory: the bundle
 * is the only artifact that belongs in a published resource, and the staging directory doubles as the
 * resource root, so its layout is the klib's layout.
 *
 * Cross-project task paths cannot be spelled as strings, which is why the browser build has to be
 * evaluated here (the staging task names its task directly).
 */
evaluationDependsOn(":ktjs-docusaurus-kit-browser")

val browserBundleProject = project(":ktjs-docusaurus-kit-browser")
val browserBundleFile = browserBundleProject.layout.buildDirectory
    .file("dist/js/productionExecutable/ktjs-docusaurus-kit-browser.js")

val stageBrowserBundle = tasks.register<Sync>("stageBrowserBundle") {
    from(browserBundleFile) {
        // The klib keeps the same `ktjs-client/` prefix the site serves the file from, so extracting
        // the resource directory is all a consumer has to do to get a working `static/` layout.
        into("ktjs-client")
    }
    into(layout.buildDirectory.dir("generated/browser-bundle"))
    // A `buildDirectory.file(...)` provider says where the file is, not who produces it, so the
    // webpack step has to be named explicitly.
    dependsOn(browserBundleProject.tasks.named("jsBrowserDistribution"))
    // Copying a missing source is silently a no-op, which would publish a klib without its bundle.
    // Failing here keeps the file name in one place in practice: this one.
    doFirst {
        check(browserBundleFile.get().asFile.isFile) {
            "The browser bundle is missing: ${browserBundleFile.get().asFile}"
        }
    }
}

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
        jsMain {
            // The browser bundle ships as a resource of this module, so the published klib carries
            // it: one dependency brings both the compiled helpers and the bundle, and the two
            // versions cannot drift apart.
            resources.srcDir(stageBrowserBundle)
        }
        jsMain.dependencies {
            api("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
        }
        jsTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// `srcDir(task)` contributes the staging directory but not the task dependency, so a clean checkout
// would copy nothing. Resource processing is what reads the staging directory, so that is where the
// edge belongs.
tasks.named("jsProcessResources") {
    dependsOn(stageBrowserBundle)
}

// The resource path is part of the published contract — a site unzips the klib and copies the
// `ktjs-client/` directory — so it is checked instead of assumed. A resource that vanishes (a
// renamed bundle, a staging directory that stopped being wired) would otherwise only show up in
// someone else's checkout.
val jsKlib = tasks.named<Jar>("jsJar").flatMap { it.archiveFile }

val verifyBrowserBundleResource = tasks.register("verifyBrowserBundleResource") {
    dependsOn(tasks.named("jsJar"))
    inputs.file(jsKlib)
    doLast {
        val entry = "ktjs-client/ktjs-docusaurus-kit-browser.js"
        val size = ZipFile(jsKlib.get().asFile).use { zip -> zip.getEntry(entry)?.size ?: -1 }
        check(size > 0) { "The kit's klib does not carry $entry" }
    }
}

tasks.named("check") {
    dependsOn(verifyBrowserBundleResource)
}
