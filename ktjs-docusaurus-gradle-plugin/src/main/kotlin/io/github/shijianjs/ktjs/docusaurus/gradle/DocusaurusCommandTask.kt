package io.github.shijianjs.ktjs.docusaurus.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Runs a `@docusaurus/core` CLI command in the generated site directory.
 *
 * The CLI is invoked through Node directly instead of `npx` / `npm run`: the npm workspace is
 * installed by Gradle, so there is no `package-lock.json` of its own to install from and no shell
 * to go through.
 *
 * Which command runs, and its committed flags, come from the plugin; the two things that change
 * between machines and between runs are options and properties instead of edit-before-run:
 * `--locale` / `--port` on the command line, or `-Pdocusaurus.locale` / `-Pdocusaurus.port` when the
 * choice should apply to every invocation from this shell (see [CommandArguments]).
 */
// The command runs an external tool in place and declares no outputs; caching would only claim a
// result it cannot reproduce (a dev server never finishes, a build writes outside the build cache).
@DisableCachingByDefault(because = "Runs the Docusaurus CLI in place and declares no outputs")
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

    /**
     * UI locale for this run, e.g. `zh-Hans`.
     *
     * Overrides a `--locale` the site committed in `startArguments`; unset keeps the committed
     * choice, and a site that commits nothing serves its `defaultLocale`.
     */
    @get:Option(option = "locale", description = "UI locale for this run, e.g. zh-Hans.")
    @get:Optional
    @get:Input
    abstract val locale: Property<String>

    /** Port for `start` / `serve`; unset keeps Docusaurus' own default (3000). */
    @get:Option(option = "port", description = "Port for the dev server or the preview server.")
    @get:Optional
    @get:Input
    abstract val port: Property<Int>

    /**
     * `-Pdocusaurus.arguments`: flags this plugin does not model, e.g. `--host 0.0.0.0 --poll 1000`.
     *
     * Deliberately a property and not an `--option`: it carries a whole flag list, and a task option
     * that takes "one string of arguments" is what a shell alias is for.
     */
    @get:Optional
    @get:Input
    abstract val extraArguments: Property<String>

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

        val args = CommandArguments.resolve(
            base = arguments.get(),
            extra = extraArguments.orNull,
            locale = locale.orNull,
            port = port.orNull,
        )
        logger.info("Docusaurus: {} {}", bin, args.joinToString(" "))

        exec.exec { spec ->
            spec.workingDir = site
            spec.executable = nodeExecutable.get()
            spec.args(bin)
            spec.args(args)
        }
    }
}
