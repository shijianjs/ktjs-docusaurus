import groovy.json.JsonSlurper
import java.util.Properties

plugins {
    kotlin("jvm") version "2.4.21"
    `java-gradle-plugin`
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

version = "$docusaurusVersion-kt.$wrapperRevision"
description = "Gradle plugin that turns a Kotlin/JS project into a Docusaurus site."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

gradlePlugin {
    plugins {
        create("docusaurus") {
            id = "io.github.shijianjs.docusaurus"
            implementationClass = "io.github.shijianjs.docusaurus.gradle.DocusaurusPlugin"
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
