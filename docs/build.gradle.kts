plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

// npm package versions live in the root `package.json`; every Docusaurus package is published in
// lockstep, so `@docusaurus/core` is the anchor — the same one the wrapper module uses.
@Suppress("UNCHECKED_CAST")
val docusaurusVersion =
    (rootProject.extra["npmVersions"] as Map<String, String>).getValue("@docusaurus/core")

/**
 * `docs` is this project's own documentation site, not a published module: Markdown lives in
 * `src/jsMain/resources`, configuration is Kotlin, and `./gradlew :docs:docusaurusBuild` turns both
 * into the static site GitHub Pages serves.
 *
 * This module is loaded by Node (Docusaurus imports `docusaurus.config.mjs` from its bundle), so it
 * must not depend on `ktjs-docusaurus-kit-browser` — those classes evaluate `HTMLElement` at module load.
 * That package's linked output is published under `static/ktjs-client/` instead and loaded by the
 * browser after hydration (see `ClientModule.kt`).
 */
kotlin {
    js {
        useEsModules()
        nodejs()
        binaries.library()
    }

    // The JVM target carries only the Playwright end-to-end suite in `src/jvmTest`; the site itself
    // stays a Kotlin/JS module, so the site remains one KMP project.
    jvm()

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":ktjs-docusaurus"))
            implementation(project(":ktjs-docusaurus-kit"))

            // The mermaid theme is resolved *by name* from the generated configuration, so npm has
            // to install it next to the site: no Kotlin code imports it, and the wrapper does not
            // bring it along with the Docusaurus packages it declares. Version from the same
            // lockstep anchor the wrapper uses — every `@docusaurus/*` package moves together.
            implementation(npm("@docusaurus/theme-mermaid", docusaurusVersion))
        }
        jvmTest.dependencies {
            // Playwright's official Java library already includes the JUnit 5 integration; the
            // version has to match the npm Playwright bundled with the Kotlin Gradle Plugin so both
            // reuse the Chromium installed by `kotlinInstallPlaywrightBrowsers`.
            implementation(libs.playwright)
            implementation(libs.junit.jupiter)
            implementation(libs.junit.pioneer)
            implementation(kotlin("test-junit5"))
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
}

docusaurus {
    sidebarsFunction = "sidebars"

    // TOC toggle plus the browser bundle loader; runs after hydration on every route change.
    clientModuleFunction = "onRouteDidUpdate"

    // Browser-only bundle: the webpack output of `ktjs-docusaurus-kit-browser` already contains the
    // elements and their npm dependencies, so the plugin just copies it and imports it for its
    // registration side effect.
    browserBundle = "ktjs-docusaurus-kit-browser.js"
    browserBundleFiles.from(
        project(":ktjs-docusaurus-kit-browser")
            .layout.buildDirectory.dir("dist/js/productionExecutable"),
    )
    // Chinese by default for this site's dev server. It is only the default: `--locale en` (or
    // `-Pdocusaurus.locale=en`) overrides it for one run without touching this file.
    startArguments = listOf("--locale", "zh-Hans")
}

// A file collection cannot carry a task dependency across projects, so build the browser bundle
// before the site is assembled.
tasks.named("docusaurusSite") {
    dependsOn(":ktjs-docusaurus-kit-browser:jsBrowserDistribution")
}

// ---------------------------------------------------------------------------------------------
// End-to-end suite
//
// `src/jvmTest` runs Playwright's Java API against the *built site*, not against a development
// server. The test JVM serves the Docusaurus output itself (see `DocsServer.kt`), rewriting the
// configured `/ktjs-docusaurus/` baseUrl. `jvmTest` is disabled to keep `build`/`check`
// browser-free — this dedicated task runs the same test compilation on demand.
// ---------------------------------------------------------------------------------------------
val jvmE2eCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Runs the Kotlin/JVM Playwright suite against the built Docusaurus site."
    useJUnitPlatform()
    dependsOn("docusaurusBuild", ":ktjs-docusaurus-kit-browser:kotlinInstallPlaywrightBrowsers")
    testClassesDirs = jvmE2eCompilation.output.classesDirs
    classpath = files(jvmE2eCompilation.output.allOutputs, jvmE2eCompilation.runtimeDependencyFiles)
    systemProperty(
        "docs.dist",
        rootProject.layout.buildDirectory.dir("js/docusaurus/docs/build").get().asFile.absolutePath,
    )
    systemProperty("e2e.output", layout.buildDirectory.dir("e2e-results").get().asFile.absolutePath)
    systemProperty("junit.jupiter.execution.timeout.default", "60 s")
    outputs.upToDateWhen { false }
}

tasks.named<Test>("jvmTest") { enabled = false }
