plugins {
    kotlin("multiplatform")
}

/**
 * A verbatim copy of `kotlin-wrappers/examples/web-components`, kept here as a runnable probe.
 *
 * It answers one question: does the declarative `CustomElement` / `CustomElementReference` API
 * actually construct an element at runtime under Kotlin 2.4.21? The wrappers repository ships no test
 * for that example, so compiling is all that has been verified there.
 *
 *     ./gradlew :probes-web-components:jsBrowserDevelopmentRun
 *
 * then open the dev server (KGP prints the port, usually 8080) and watch the console.
 */
kotlin {
    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
        }
    }
}
