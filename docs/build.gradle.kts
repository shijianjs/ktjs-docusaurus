plugins {
    kotlin("multiplatform")
    id("io.github.shijianjs.ktjs.docusaurus")
}

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
        }
        jvmTest.dependencies {
            // Playwright's official Java library already includes the JUnit 5 integration; the
            // version has to match the npm Playwright bundled with the Kotlin Gradle Plugin so both
            // reuse the Chromium installed by `kotlinInstallPlaywrightBrowsers`.
            implementation("com.microsoft.playwright:playwright:1.61.0")
            implementation("org.junit.jupiter:junit-jupiter:6.1.3")
            implementation("org.junit-pioneer:junit-pioneer:2.3.0")
            implementation(kotlin("test-junit5"))
            runtimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
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
    // --locale zh-Hans
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
    dependsOn("docusaurusBuild", ":ktjs-dom-kit:kotlinInstallPlaywrightBrowsers")
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
