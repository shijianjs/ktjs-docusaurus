pluginManagement {
    // The Gradle plugin is a build of its own: the root build consumes it through this include, so
    // `docs` applies it like any published plugin without a local publish step. Keeping it out of
    // the root project also keeps `./gradlew build` from compiling a JVM project that only the docs
    // site needs; CI builds and publishes it separately.
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

// This project's documentation site: a Kotlin/JS project whose Markdown lives in
// `src/jsMain/resources`. Published as the GitHub Pages site, never to Maven.
include(":docs")


// Runnable copy of kotlin-wrappers' web-components example (verification only, not published).
include(":probes-web-components")
project(":probes-web-components").projectDir = file("probes/web-components")



// Reusable Kotlin/JS building blocks (home sections, TOC toggle). Deliberately a separate module
// from the wrapper: the wrapper only maps npm packages, the kit contains logic of its own.
include(":ktjs-docusaurus-kit")

// Browser-only half of the docs kit: custom elements built on the official HTMLElement API.
// Published as a package too, but its bundle is loaded by the browser alone (never by Node).
include(":ktjs-docusaurus-kit-elements")

// Generic DOM helpers (the custom-element adapter, the attribute delegate, the typed stylesheet),
// independent of Docusaurus. It is hosted in this build rather than a repository of its own on
// purpose: its documentation is written with the very framework it helps build, so splitting the two
// would make the site depend on the library while the library's docs depend on the site. The
// `ktjs-dom-kit` name keeps it out of the published prefix; the docs site's `dom-kit` section is its
// manual.
include(":ktjs-dom-kit")

// Webpack bundle of the elements library for the browser. Not published: it exists so the site loads
// one file with npm dependencies inlined, while `ktjs-docusaurus-kit-elements` stays a library.
include(":ktjs-docusaurus-kit-bundle")
