package io.github.shijianjs.docusaurus.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Assembles the Docusaurus site directory:
 *
 * - copies the Markdown and static assets out of the module's resources,
 * - copies the compiled Kotlin/JS module and its runtime next to them,
 * - writes the `docusaurus.config.mjs` / `sidebars.mjs` shims that import the Kotlin functions,
 * - writes the minimal `package.json` Docusaurus expects.
 *
 * The shims contain no bare imports: the Kotlin output imports its own runtime through relative
 * paths, and the Docusaurus packages used by the configuration are resolved by Docusaurus from the
 * npm workspace's `node_modules` above the site directory.
 */
abstract class DocusaurusSiteTask @Inject constructor(
    private val fs: FileSystemOperations,
) : DefaultTask() {

    /** Directory holding `docs/`, `blog/`, `pages/`, `css/` and `static/`. */
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val contentDir: DirectoryProperty

    /**
     * Compiled Kotlin/JS output. Every directory that exists is scanned for the module's `.mjs`
     * entry point; the Kotlin/JS task output location differs between library and executable
     * binaries, so the task accepts a collection instead of one path.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val compiledJs: ConfigurableFileCollection

    /**
     * Where the site is assembled.
     *
     * Deliberately `@Internal` instead of `@OutputDirectory`: the directory lives inside the
     * Kotlin/JS npm workspace (`build/js`), and declaring it as an output makes Gradle report the
     * workspace's own task outputs in that tree as overlapping locations. The task is cheap (a few
     * file copies), so it re-runs on every build instead of being cached.
     */
    @get:Internal
    abstract val siteDir: DirectoryProperty

    /** Exported Kotlin function returning the site configuration. */
    @get:Input
    abstract val configFunction: Property<String>

    /** Exported Kotlin function returning the sidebars; empty when the site has none. */
    @get:Input
    @get:Optional
    abstract val sidebarsFunction: Property<String>

    @TaskAction
    fun generate() {
        val site = siteDir.get().asFile
        val content = contentDir.orNull?.asFile

        site.mkdirs()
        listOf("docs", "blog", "src", "static", "generated").forEach { name ->
            fs.delete { it.delete(File(site, name)) }
        }

        if (content != null && content.isDirectory) {
            copyContent(content, site)
        }

        val bundle = copyCompiledKotlin(site)
        writeConfigShim(site, bundle)
        writePackageJson(site)
    }

    private fun copyContent(content: File, site: File) {
        val mapping = mapOf(
            "docs" to "docs",
            "blog" to "blog",
            "pages" to "src/pages",
            "css" to "src/css",
            "static" to "static",
        )
        mapping.forEach { (from, to) ->
            val source = File(content, from)
            if (source.isDirectory) {
                fs.copy { spec ->
                    spec.from(source)
                    spec.into(File(site, to))
                }
            }
        }
    }

    /**
     * Copies every directory of [compiledJs] into `<site>/generated` and returns the file name of
     * the module's own entry point.
     */
    private fun copyCompiledKotlin(site: File): String {
        val generated = File(site, "generated").apply { mkdirs() }
        compiledJs.files
            .filter { it.isDirectory }
            .forEach { directory ->
                fs.copy { spec ->
                    spec.from(directory)
                    spec.into(generated)
                }
            }

        val runtimePrefixes = listOf("kotlin-", "kotlin_", "kotlinx-", "seskar-")
        val entry = generated.listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.endsWith(".mjs") }
            .filterNot { file -> runtimePrefixes.any { file.name.startsWith(it) } }
            .minByOrNull { it.name }
            ?: error(
                "No Kotlin/JS module output found in $generated. " +
                    "The module must declare a `js` target and be compiled before this task runs.",
            )
        return entry.name
    }

    private fun writeConfigShim(site: File, bundle: String) {
        val function = configFunction.get()
        File(site, "docusaurus.config.mjs").writeText(
            """
            // Generated by the ktjs-docusaurus Gradle plugin. Do not edit.
            import { $function } from './generated/$bundle';

            export default $function();
            """.trimIndent() + "\n",
        )

        sidebarsFunction.orNull?.takeIf { it.isNotBlank() }?.let { sidebars ->
            File(site, "sidebars.mjs").writeText(
                """
                // Generated by the ktjs-docusaurus Gradle plugin. Do not edit.
                import { $sidebars } from './generated/$bundle';

                export default $sidebars();
                """.trimIndent() + "\n",
            )
        }
    }

    private fun writePackageJson(site: File) {
        // No `"type": "module"`: Docusaurus writes CommonJS bundles into `.docusaurus/` and its SSR
        // step loads them as CJS. The config and sidebars shims use the `.mjs` extension, so they
        // stay ES modules without the package-wide switch.
        File(site, "package.json").writeText(
            """
            {
              "name": "ktjs-docusaurus-site",
              "private": true
            }
            """.trimIndent() + "\n",
        )
    }
}
