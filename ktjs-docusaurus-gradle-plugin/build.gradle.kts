import groovy.json.JsonSlurper
import java.util.Properties

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
    // `java-gradle-plugin` only contributes the `pluginMaven` and plugin-marker publications; the
    // `publishToMavenLocal` task that a consuming build resolves the plugin from comes from
    // `maven-publish`. Publishing to Maven Central (signing plus the hub registration) is not wired
    // yet — see the roadmap.
    `maven-publish`
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
