pluginManagement {
    // The Gradle plugin is a build of its own: the root build consumes it through this include, so
    // `demo` applies it like any published plugin without a local publish step. Keeping it out of
    // the root project also keeps `./gradlew build` from compiling a JVM project that only the demo
    // needs; CI builds and publishes it separately.
    includeBuild("ktjs-docusaurus-gradle-plugin")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        // Maven Central is the source of truth for kotlin-wrappers: the Aliyun mirror lags behind
        // and sometimes serves metadata without the corresponding klibs. Gradle does not fall back
        // to the next repository once metadata is found, so pin these modules to Central even when
        // a local mirror is active.
        exclusiveContent {
            forRepository { mavenCentral() }
            filter {
                includeGroup("org.jetbrains.kotlin-wrappers")
                includeModule("org.jetbrains.kotlin", "kotlin-js-plain-objects")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "ktjs-docusaurus"

// Kotlin/JS wrapper for the Docusaurus packages. One module for every upstream package: Docusaurus
// publishes all of them in lockstep, so splitting them would only produce redundant Maven versions.
include(":ktjs-docusaurus")

// Example documentation site: a Kotlin/JS project whose Markdown lives in `src/jsMain/resources`.
include(":demo")


// Reusable Kotlin/JS building blocks (home sections, TOC toggle). Deliberately a separate module
// from the wrapper: the wrapper only maps npm packages, the kit contains logic of its own.
include(":ktjs-docusaurus-kit")
