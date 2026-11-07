package io.github.shijianjs.docusaurus.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Runs a `@docusaurus/core` CLI command in the generated site directory.
 *
 * The CLI is invoked through Node directly instead of `npx` / `npm run`: the npm workspace is
 * installed by Gradle, so there is no `package-lock.json` of its own to install from and no shell
 * to go through.
 */
abstract class DocusaurusCommandTask @Inject constructor(
    private val exec: ExecOperations,
) : DefaultTask() {

    /** Node executable, absolute or on `PATH`. */
    @get:Input
    abstract val nodeExecutable: Property<String>

    /** Absolute path of `@docusaurus/core`'s `bin/docusaurus.mjs`. */
    @get:Input
    abstract val docusaurusBin: Property<String>

    /** Arguments passed after the CLI path, e.g. `build` or `start --no-open`. */
    @get:Input
    abstract val arguments: ListProperty<String>

    /** Directory the command runs in; the site directory. */
    @get:Internal
    abstract val siteDir: DirectoryProperty

    @TaskAction
    fun run() {
        val site = siteDir.get().asFile
        check(site.isDirectory) {
            "Site directory $site does not exist. Run `docusaurusSite` first."
        }
        val bin = docusaurusBin.get()
        check(java.io.File(bin).isFile) {
            "Docusaurus CLI not found at $bin. Run the Kotlin/JS npm install (for example " +
                "`./gradlew kotlinNpmInstall`) or set `docusaurus.docusaurusBin`."
        }

        exec.exec { spec ->
            spec.workingDir = site
            spec.executable = nodeExecutable.get()
            spec.args(bin)
            spec.args(arguments.get())
        }
    }
}
