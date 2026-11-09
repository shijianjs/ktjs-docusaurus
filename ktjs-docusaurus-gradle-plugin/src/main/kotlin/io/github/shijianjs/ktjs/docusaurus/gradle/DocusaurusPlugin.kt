package io.github.shijianjs.ktjs.docusaurus.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * Turns a Kotlin/JS module into a Docusaurus site.
 *
 * ```kotlin
 * plugins {
 *     kotlin("multiplatform")
 *     id("io.github.shijianjs.ktjs.docusaurus")
 * }
 *
 * docusaurus {
 *     // All optional; the defaults match `src/jsMain/resources`.
 *     configFunction = "docusaurusConfig"
 *     sidebarsFunction = "sidebars"
 * }
 * ```
 *
 * Tasks:
 *
 * - `docusaurusSite` assembles the site directory,
 * - `docusaurusBuild` produces the static site in `<site>/build`,
 * - `docusaurusStart` runs the dev server,
 * - `docusaurusServe` serves an existing build,
 * - `docusaurusClear` drops Docusaurus' caches.
 */
class DocusaurusPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("docusaurus", DocusaurusExtension::class.java)

        // The site directory must sit inside the npm workspace the Kotlin/JS plugin installs,
        // because that is what Node resolves `@docusaurus/*` and the theme packages from.
        val npmWorkspace = project.rootProject.layout.buildDirectory.get().asFile.resolve("js")
        val defaultSiteDir = npmWorkspace.resolve("docusaurus/${project.name}")

        val siteDir: Provider<File> = project.provider {
            File(extension.siteDir ?: defaultSiteDir.path)
        }
        val siteDirectory: DirectoryProperty =
            project.objects.directoryProperty().fileProvider(siteDir)

        val contentDir = project.layout.dir(
            project.provider { File(project.projectDir, extension.contentDir) },
        )

        // Kotlin/JS writes the linked `.mjs` files under one of these, depending on the binary kind.
        // The task filters the directories that actually exist. Only production outputs are listed:
        // pulling in the development link as well makes the Kotlin plugin report its own
        // `jsNodeProductionLibraryDistribution` as reading another task's output.
        val compiledJs = project.files(
            project.layout.buildDirectory.dir("compileSync/js/main/productionLibrary/kotlin"),
            project.layout.buildDirectory.dir("compileSync/js/main/productionExecutable/kotlin"),
            // Webpack output of an `executable` binary: one file that already contains the
            // dependency modules. A `library` binary is not usable here — its output is
            // dead-code-eliminated down to nothing when the module has no `@JsExport` entry point.
            project.layout.buildDirectory.dir("dist/js/productionExecutable"),
        )

        val siteTask = project.tasks.register("docusaurusSite", DocusaurusSiteTask::class.java) {
            it.group = GROUP
            it.description =
                "Assembles the Docusaurus site from the module's resources and compiled Kotlin/JS."
            it.contentDir.set(contentDir)
            it.compiledJs.from(compiledJs)
            it.siteDir.set(siteDirectory)
            it.configFunction.set(project.provider { extension.configFunction })
            it.sidebarsFunction.set(project.provider { extension.sidebarsFunction ?: "" })
            it.clientModuleFunction.set(project.provider { extension.clientModuleFunction ?: "" })
            it.clientModuleBundle.set(project.provider { extension.clientModuleBundle ?: "" })
            it.browserBundle.set(project.provider { extension.browserBundle ?: "" })
            it.browserBundleEntry.set(project.provider { extension.browserBundleEntry ?: "" })
            it.pageFunctions.set(project.provider { extension.pageFunctions })
            it.pageBundle.set(project.provider { extension.pageBundle ?: "" })
            it.additionalJs.from(extension.additionalJs)

            // `jsProductionLibraryCompileSync` is the task that links the js target's main library
            // into `.mjs` files; `compileKotlinJs` alone only produces the klib. A live collection
            // keeps this lazy: configuring the compiler here would mutate the task container from
            // inside another task's configuration.
            it.dependsOn(
                project.tasks.matching { task ->
                    task.name == "jsProductionLibraryCompileSync" ||
                        task.name == "jsProductionExecutableCompileSync"
                },
            )
        }

        val nodeExecutable = project.providers.provider {
            extension.nodeExecutable ?: detectNode(npmWorkspace)
        }
        val docusaurusBin = project.providers.provider {
            extension.docusaurusBin
                ?: npmWorkspace.resolve("node_modules/@docusaurus/core/bin/docusaurus.mjs").path
        }

        registerCommand(
            project, "docusaurusBuild", "Builds the Docusaurus static site.",
            siteDirectory, siteTask, nodeExecutable, docusaurusBin,
        ) { listOf("build") }

        registerCommand(
            project, "docusaurusStart", "Starts the Docusaurus dev server.",
            siteDirectory, siteTask, nodeExecutable, docusaurusBin,
        ) { listOf("start", "--no-open") + extension.startArguments }

        registerCommand(
            project, "docusaurusServe", "Serves the last Docusaurus build.",
            siteDirectory, null, nodeExecutable, docusaurusBin,
        ) { listOf("serve") }

        registerCommand(
            project, "docusaurusClear", "Clears the Docusaurus caches.",
            siteDirectory, null, nodeExecutable, docusaurusBin,
        ) { listOf("clear") }
    }

    private fun registerCommand(
        project: Project,
        name: String,
        description: String,
        siteDirectory: DirectoryProperty,
        siteTask: TaskProvider<DocusaurusSiteTask>?,
        nodeExecutable: Provider<String>,
        docusaurusBin: Provider<String>,
        arguments: () -> List<String>,
    ) {
        project.tasks.register(name, DocusaurusCommandTask::class.java) { task ->
            task.group = GROUP
            task.description = description
            task.siteDir.set(siteDirectory)
            task.nodeExecutable.set(nodeExecutable)
            task.docusaurusBin.set(docusaurusBin)
            task.arguments.set(project.provider { arguments() })
            siteTask?.let { task.dependsOn(it) }
        }
    }

    /**
     * The Node runtime Kotlin/JS installed, falling back to whatever `node` is on `PATH`.
     *
     * Older KGP releases unpacked Node under the npm workspace (`build/js/node`); current ones keep
     * it in the Gradle user home (`nodejs/node-v<version>-<platform>`), so both are checked before
     * giving up on `PATH`.
     */
    private fun detectNode(npmWorkspace: File): String {
        val inWorkspace = npmWorkspace.resolve("node")
        listOf(
            inWorkspace.resolve("node.exe"),
            inWorkspace.resolve("bin/node"),
            inWorkspace.resolve("node"),
        ).firstOrNull { it.isFile }?.let { return it.path }

        val gradleUserHome = System.getProperty("gradle.user.home")
            ?.let(::File)
            ?: File(System.getProperty("user.home"), ".gradle")
        File(gradleUserHome, "nodejs")
            .listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .sortedByDescending { it.name }
            .map { directory ->
                listOf(
                    File(directory, "node.exe"),
                    File(directory, "bin/node"),
                    File(directory, "node"),
                )
            }
            .flatten()
            .firstOrNull { it.isFile }
            ?.let { return it.path }

        return "node"
    }

    private companion object {
        const val GROUP = "documentation"
    }
}
