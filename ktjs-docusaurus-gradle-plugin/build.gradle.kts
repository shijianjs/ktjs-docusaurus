import groovy.json.JsonSlurper
import java.util.Properties
import org.gradle.plugins.signing.SigningExtension

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
    // `java-gradle-plugin` only contributes the `pluginMaven` and plugin-marker publications; the
    // publishing tasks, the repositories and the signatures come from these two. Central itself is
    // never contacted from here: the release workflow packages the file repository below and the
    // hub merges it into a single deployment.
    `maven-publish`
    signing
}

group = "io.github.shijianjs"

// The plugin ships with the wrapper, so it follows the same upstream Docusaurus version. Both
// sources live at the repository root; this build only reads them.
@Suppress("UNCHECKED_CAST")
val docusaurusVersion = run {
    val manifest = JsonSlurper().parse(file("../package.json")) as Map<*, *>
    (manifest["dependencies"] as Map<String, Any?>)["@docusaurus/core"].toString()
}

val wrapperRevision = run {
    val properties = Properties()
    file("../wrapper-revisions.properties").inputStream().use { properties.load(it) }
    properties.getProperty("ktjs-docusaurus-gradle-plugin").trim().toInt()
}

// Unreleased, like every other artifact in a working tree. This is a build of its own, and Gradle
// does not forward `-P` properties to an included build, so a release has to pass `-Pktjs.release`
// to this build in its own invocation as well (see AGENTS.md).
val versionSuffix =
    if (providers.gradleProperty("ktjs.release").map(String::toBoolean).getOrElse(false)) "" else "-SNAPSHOT"

version = "$docusaurusVersion-kt.$wrapperRevision$versionSuffix"
description = "Gradle plugin that turns a Kotlin/JS project into a Docusaurus site."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    // Central requires both next to every jar-packaged coordinate, and the hub's dry run refuses a
    // bundle without them. The javadoc jar carries no content: the API reference is Dokka output on
    // the Pages site, exactly as for the wrapper modules.
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    plugins {
        create("docusaurus") {
            id = "io.github.shijianjs.ktjs.docusaurus"
            implementationClass = "io.github.shijianjs.ktjs.docusaurus.gradle.DocusaurusPlugin"
            displayName = "ktjs-docusaurus"
            description = "Generates and builds a Docusaurus site from a Kotlin/JS project."
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
}

// Both publications carry the same metadata: the plugin itself (`ktjs-docusaurus-gradle-plugin`) and
// the marker Gradle resolves a `plugins { id(...) }` request through
// (`io.github.shijianjs.ktjs.docusaurus:io.github.shijianjs.ktjs.docusaurus.gradle.plugin`).
publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("ktjs-docusaurus-gradle-plugin")
            description.set("Gradle plugin that turns a Kotlin/JS project into a Docusaurus site.")
            url.set("https://github.com/shijianjs/ktjs-docusaurus")
            inceptionYear.set("2026")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/shijianjs/ktjs-docusaurus/blob/master/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("shijianjs")
                    name.set("shijianjs")
                    url.set("https://github.com/shijianjs")
                }
            }

            scm {
                url.set("https://github.com/shijianjs/ktjs-docusaurus")
                connection.set("scm:git:git://github.com/shijianjs/ktjs-docusaurus.git")
                developerConnection.set("scm:git:ssh://git@github.com/shijianjs/ktjs-docusaurus.git")
            }
        }
    }

    // A plain file repository produces the Maven layout plus the signatures without contacting
    // Central; the release workflow archives it per coordinate for the hub.
    // Task name: publishAllPublicationsToHubStagingRepository
    repositories {
        maven {
            name = "hubStaging"
            url = layout.buildDirectory.dir("hub-staging").get().asFile.toURI()
        }
    }
}

// The release workflow supplies the key as an in-memory secret. A working tree has none: a SNAPSHOT
// still publishes locally (no signature to make), while a release build without a key fails loudly
// rather than shipping unsigned artifacts the hub would reject.
val signingKey: String? = providers.gradleProperty("signingInMemoryKey").orNull
extensions.configure<SigningExtension> {
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, providers.gradleProperty("signingInMemoryKeyPassword").orNull)
        sign(publishing.publications)
    }
    isRequired = signingKey != null || !version.toString().endsWith("SNAPSHOT")
}

// Same contract as the root build's task: "<module>=<version>" so the release workflow can plan
// without repeating the version rule in shell.
tasks.register("printPublishedVersions") {
    doLast {
        println("ktjs-docusaurus-gradle-plugin=$version")
    }
}
