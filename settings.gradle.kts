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
        // `ktjs-dom-kit` is published from its own repository, so a change there can only be tried
        // here after it is in a repository: a SNAPSHOT comes from `mavenLocal` (that project's
        // `./gradlew publishToMavenLocal`), a released version from Central. An empty local repository
        // falls through, so CI resolves exactly the released artifacts.
        mavenLocal()
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

// Generic DOM helpers (the custom-element adapter, the attribute delegate, the typed stylesheet) live
// in their own repository, `shijianjs/ktjs-dom-kit`, and are consumed here as a Maven artifact: the
// kit's browser half is built on them, and so is every element on the site. They used to be a module
// of this build; the split is what lets the library be documented by a site that uses it without
// either side depending on the other's build.

// Browser-only half of the docs kit: every custom element the site defines, in one webpack bundle.
// Executable rather than a library, so the npm imports are inlined for the browser.
include(":ktjs-docusaurus-kit-browser")
