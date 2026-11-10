import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import dev.detekt.gradle.extensions.DetektExtension
import groovy.json.JsonSlurper
import org.gradle.api.publish.PublishingExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension

plugins {
    kotlin("multiplatform") version "2.4.21" apply false
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
    id("dev.detekt") version "2.0.0-alpha.6" apply false
    id("org.jetbrains.dokka") version "2.2.0"
}

// Every publishable module shares the prefix, which is also how publishing, Dokka and the version
// report find them. Keep it in sync with the module directories and settings.gradle.kts. The prefix
// deliberately has no trailing dash: `ktjs-docusaurus-gradle-plugin` is a separate build (see
// settings.gradle.kts) but still follows the same upstream version.
val publishedPrefix = "ktjs-docusaurus"

// Modules that carry the prefix but are not an API surface. The home elements are this site's UI, not
// a reusable contract, and the bundle only exists to inline them for one site; neither is released or
// documented. What other projects consume is the bundle, distributed as an artifact of the kit.
val unpublishedModules = setOf(
    "ktjs-docusaurus-kit-bundle",
    "ktjs-docusaurus-kit-elements",
)

// Only the wrapper follows an upstream npm version. The kit is original code with its own semantic
// version, so it is not looked up in wrapper-revisions.properties.
val wrapperModules = setOf("ktjs-docusaurus")

// Dokka aggregates every published module into one API reference. `demo` is not part of the
// published API surface, so it is excluded by the prefix filter.
dependencies {
    subprojects
        .filter { it.name.startsWith(publishedPrefix) && it.name !in unpublishedModules }
        .forEach { dokka(project(it.path)) }
}

dokka {
    dokkaPublications.html {
        moduleName.set("ktjs-docusaurus")
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
}

// GitHub-backed namespace; verify it once in the Central Portal. All modules share it.
// Setting the root `group` alone does not propagate to subprojects.
val publishedGroup = "io.github.shijianjs"

// npm package versions live in package.json so that Dependabot can update them.
// Subprojects read this map to declare npm(...) dependencies and derive their version.
@Suppress("UNCHECKED_CAST")
val npmVersions: Map<String, String> = run {
    val manifest = JsonSlurper().parse(file("package.json")) as Map<*, *>
    (manifest["dependencies"] as Map<String, Any?>).mapValues { it.value.toString() }
}
extra["npmVersions"] = npmVersions

// Wrapper revisions are per module: a release only creates new versions for the modules whose
// revision was bumped, so unchanged modules are not re-uploaded (file/size quota and version
// noise). Release Count is per Central deployment, not per module: publishing every pending
// module in one Gradle invocation makes the plugin upload a single deployment.
@Suppress("UNCHECKED_CAST")
val wrapperRevisions: Map<String, Int> = run {
    val properties = java.util.Properties()
    file("wrapper-revisions.properties").inputStream().use { properties.load(it) }
    properties.stringPropertyNames().associate { name ->
        name to properties.getProperty(name).trim().toInt()
    }
}
extra["wrapperRevisions"] = wrapperRevisions

// Keep a single webpack instance across the npm workspace: npm otherwise installs a second copy
// for source-map-loader's peer dependency, and the test bundle fails with
// "The 'compilation' argument must be an instance of Compilation".
plugins.withType<NodeJsRootPlugin> {
    extensions.getByType(NpmExtension::class.java).override("webpack", "5.108.1")
}

// The public API must use kotlin-browser's web.* types. Kotlin/JS's own org.w3c.dom.* and
// kotlinx.browser.* compile and run against the same objects, but the compiler treats them as
// unrelated types, which breaks callers that use kotlin-browser. detekt.yml forbids the imports.
// Kotlin/JS links every library module with IR dead-code elimination on by default, keeping only
// what is reachable from that module's own `@JsExport` roots. A wrapper module has no such roots, so
// its `.mjs` output would be empty and any cross-module reference would resolve to `undefined`.
// Turning it off keeps the public API of each module in its output. It is applied to every KMP
// module — including the demo — because each module is linked separately.
subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            compilerOptions {
                freeCompilerArgs.add("-Xir-dce=false")
            }

            // Custom elements need a real `class X extends HTMLElement`. At the default ES5 target
            // Kotlin emits `function X() { HTMLElement.call(this) }`, and a DOM constructor may only
            // be invoked with `new` — the browser throws "Please use the 'new' operator". ES2015 is
            // the first target that can express a class.
            //
            // It has to be set on the js target's compiler options; `-target es2015` through
            // `freeCompilerArgs` on the extension has no effect.
            targets
                .withType(org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl::class.java)
                .configureEach {
                    compilerOptions {
                        target = "es2015"
                    }
                }
        }
    }
}

subprojects {
    apply(plugin = "dev.detekt")

    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.files("detekt.yml"))
        // The plain detekt task is wired into check; point it at the multiplatform source tree.
        source.setFrom(files("src"))
    }

    // Only wrapper modules are published and documented; `demo` is the GitHub Pages site.
    if (name.startsWith(publishedPrefix) && name !in unpublishedModules) {
        apply(plugin = "org.jetbrains.dokka")
    }
}

// Publishing is configured once for every module so that coordinates, POM metadata, signing and
// the Maven Central target cannot drift apart.
//
// klibs.io discovers libraries automatically: it reads the POM (url / scm.url must point at the
// GitHub repository) and requires the kotlin-tooling-metadata.json produced by the Kotlin
// Multiplatform plugin. Keep both intact when changing this block.
subprojects {
    if (!name.startsWith(publishedPrefix) || name in unpublishedModules) return@subprojects

    if (name in wrapperModules) {
        check(wrapperRevisions.containsKey(name)) {
            "Missing '$name' in wrapper-revisions.properties"
        }
    }

    val module = this

    group = publishedGroup

    apply(plugin = "com.vanniktech.maven.publish")

    // The subproject's Kotlin Multiplatform plugin is applied after this root script runs.
    // Choosing the javadoc jar earlier would hit the "no compatible plugin" branch and the
    // plugin's afterEvaluate default (Dokka HTML) would win.
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<MavenPublishBaseExtension> {
            // Maven Central requires a javadoc jar, but Dokka's HTML output duplicates its static
            // assets into every module's jar (~1.1 MB each). The API reference is deployed to the
            // Pages site, so publish the required empty jar instead of the HTML copy.
            configureBasedOnAppliedPlugins(javadocJar = JavadocJar.Empty())
        }
    }

    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral(automaticRelease = true)
        signAllPublications()

        // Gradle writes .md5/.sha1 next to every .asc signature. Maven Central does not need
        // them and they count toward the monthly file quota (KTC-5453, gradle/gradle#20232).
        excludeSignatureChecksums()

        pom {
            name.set(module.name)
            description.set(
                module.provider {
                    module.description ?: "Kotlin/JS wrapper for ktjs-docusaurus."
                }
            )
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

    // A plain file repository produces the Maven repository layout — POMs, artifacts, sources,
    // javadoc and the `.asc` signatures — without contacting Central. The release workflow packages
    // one archive per coordinate from it and registers them with the hub.
    // Task name: publishAllPublicationsToHubStagingRepository
    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "hubStaging"
                url = module.layout.buildDirectory.dir("hub-staging").get().asFile.toURI()
            }
        }
    }

    // The kit ships the home UI as data: the webpack bundle is attached to the kit's own coordinate
    // with a `browser` classifier, so a site depends on the kit and copies the artifact into
    // `static/` — no second library coordinate, and the version cannot drift.
    if (name == "ktjs-docusaurus-kit") {
        // The bundle project has to be evaluated before its task can be wired here.
        evaluationDependsOn(":ktjs-docusaurus-kit-bundle")
        afterEvaluate {
            val bundleProject = project(":ktjs-docusaurus-kit-bundle")
            val bundleTask = bundleProject.tasks.named("jsBrowserDistribution")
            val bundleFile = bundleProject.layout.buildDirectory
                .file("dist/js/productionExecutable/ktjs-docusaurus-kit-bundle.js")
                .get()
                .asFile
            extensions.configure<PublishingExtension> {
                publications.named<MavenPublication>("kotlinMultiplatform") {
                    artifact(bundleFile) {
                        classifier = "browser"
                        extension = "js"
                        builtBy(bundleTask)
                    }
                }
            }
        }
    }
}

// Prints "<module>=<version>" for every publishable module. Use it to see which versions a release
// would upload before running publishToMavenCentral, so version derivation stays in the module
// scripts rather than being duplicated in shell.
tasks.register("printPublishedVersions") {
    val publishedModules =
        subprojects.filter { it.name.startsWith(publishedPrefix) && it.name !in unpublishedModules }
    doLast {
        publishedModules
            .associate { it.name to it.version.toString() }
            .toSortedMap()
            .forEach { (name, version) -> println("$name=$version") }
    }
}

// Assembles the GitHub Pages site: the demo Docusaurus build at the root and the API reference
// under /api/. The site is assembled inside the npm workspace (see the Gradle plugin), which is why
// the built pages live under `build/js/docusaurus/<module>/build`.
tasks.register<Sync>("pagesSite") {
    dependsOn(":demo:docusaurusBuild", "dokkaGeneratePublicationHtml")
    into(layout.buildDirectory.dir("pages"))
    from(layout.buildDirectory.dir("js/docusaurus/demo/build"))
    from(layout.buildDirectory.dir("dokka/html")) {
        into("api")
    }
}
