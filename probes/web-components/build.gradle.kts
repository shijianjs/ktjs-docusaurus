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
 *
 * The build settings mirror `examplesbuild.kotlin-conventions.gradle.kts` where they matter:
 * `target = "es2020"` is set on the **js target's** compiler options (a string), not through
 * `-target` on the multiplatform extension.
 */
kotlin {
    js {
        browser()
        binaries.executable()

        compilerOptions {
            // Kotlin/JS accepts es5 or es2015 only — `es2020` fails with "Unsupported ECMA version".
            // es2015 is the first target that can express a real class, which custom elements need.
            target = "es2015"
        }
    }

    compilerOptions {
        optIn.add("kotlin.js.ExperimentalWasmJsInterop")
        // The DOM interfaces are marked internal-API for implementations. only the probe is allowed
        // to re-declare them.
        optIn.add("js.internal.InternalApi")
    }

    sourceSets {
        jsMain.dependencies {
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.0")
        }
    }
}
