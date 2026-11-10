package io.github.shijianjs.ktjs.docusaurus.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
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

    /**
     * Exported Kotlin function Docusaurus should call after every route change, or empty when the
     * site has no client module.
     *
     * The generated `client-module.mjs` re-exports it; pass the file to `clientModules` in the site
     * configuration as `./client-module.mjs`.
     */
    @get:Input
    @get:Optional
    abstract val clientModuleFunction: Property<String>

    /** Bundle the client module lives in; empty means the main bundle. */
    @get:Input
    @get:Optional
    abstract val clientModuleBundle: Property<String>

    /** Browser-only bundle published under `static/ktjs-client/`; empty when the site has none. */
    @get:Input
    @get:Optional
    abstract val browserBundle: Property<String>

    /** Exported function the generated browser entry calls after importing [browserBundle]. */
    @get:Input
    @get:Optional
    abstract val browserBundleEntry: Property<String>

    /** Linked output published under `static/ktjs-client/`; kept separate from [additionalJs]. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val browserBundleFiles: ConfigurableFileCollection

    /**
     * Page file name (without extension) to exported Kotlin React component, for example
     * `index` to `homePage`.
     *
     * A `src/pages/<name>.jsx` shim is generated for every entry; it re-exports the component as
     * the module's default export, which is what the pages plugin expects.
     */
    @get:Input
    abstract val pageFunctions: MapProperty<String, String>

    /** Bundle the page components live in; empty means the main bundle. */
    @get:Input
    @get:Optional
    abstract val pageBundle: Property<String>

    /**
     * Extra compiled Kotlin/JS output copied into `<site>/generated` next to the main bundle — the
     * page-components module, whose imports only resolve inside Docusaurus' webpack build.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val additionalJs: ConfigurableFileCollection

    @TaskAction
    fun generate() {
        val site = siteDir.get().asFile
        val content = contentDir.orNull?.asFile

        site.mkdirs()
        listOf("docs", "blog", "src", "static", "i18n", "generated").forEach { name ->
            fs.delete { it.delete(File(site, name)) }
        }

        if (content != null && content.isDirectory) {
            copyContent(content, site)
        }

        val bundle = copyCompiledKotlin(site)
        writeConfigShim(site, bundle)
        writePageShims(site, bundle)
        writeClientModuleShim(site, bundle)
        writeBrowserBundle(site)
        writePackageJson(site)
    }

    /**
     * Publishes the browser-only bundle under `static/ktjs-client/` and writes an entry module that
     * imports it and calls the configured function.
     *
     * `static/` is the only directory Docusaurus copies verbatim, which is what lets `headTags` point
     * a `<script type="module">` at it — and what keeps the bundle out of the server build.
     */
    private fun writeBrowserBundle(site: File) {
        val bundle = browserBundle.orNull?.takeIf { it.isNotBlank() } ?: return
        val entry = browserBundleEntry.orNull?.takeIf { it.isNotBlank() }

        val target = File(site, "static/ktjs-client").apply { mkdirs() }
        browserBundleFiles.files
            .filter { it.isDirectory }
            .forEach { directory ->
                fs.copy { spec ->
                    spec.from(directory)
                    // Source maps would double the published size and are not served to readers.
                    spec.exclude("**/*.map")
                    spec.exclude("**/*.LICENSE.txt")
                    spec.into(target)
                }
            }

        // An executable (webpack) bundle registers itself on load; a library bundle is a set of
        // linked modules whose exported entry the page has to call.
        val body = if (entry == null) {
            "import './$bundle';"
        } else {
            """
            import { $entry } from './$bundle';

            $entry();
            """.trimIndent()
        }
        File(target, "index.mjs").writeText(
            "// Generated by the ktjs-docusaurus Gradle plugin. Do not edit.\n$body\n",
        )
    }

    private fun copyContent(content: File, site: File) {
        val mapping = mapOf(
            "docs" to "docs",
            "blog" to "blog",
            "pages" to "src/pages",
            "css" to "src/css",
            "static" to "static",
            "i18n" to "i18n",
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
        // The entry point is chosen from the main module's own output, not from everything that
        // ends up in `generated`: the page-components bundle (additionalJs) imports `@theme/…`,
        // which only resolves inside webpack, and picking it here would break the config load.
        val mainOutput = mutableSetOf<String>()
        compiledJs.files
            .filter { it.isDirectory }
            .forEach { directory ->
                mainOutput += directory.listFiles().orEmpty().map { it.name }
                fs.copy { spec ->
                    spec.from(directory)
                    spec.into(generated)
                }
            }

        additionalJs.files.filter { it.isDirectory }.forEach { directory ->
            fs.copy { spec ->
                spec.from(directory)
                spec.into(generated)
            }
        }

        val runtimePrefixes = listOf("kotlin-", "kotlin_", "kotlinx-", "seskar-")
        return mainOutput
            .filter { it.endsWith(".mjs") }
            .filterNot { name -> runtimePrefixes.any { name.startsWith(it) } }
            .minOrNull()
            ?: error(
                "No Kotlin/JS module output found for ${site.name}. " +
                    "The module must declare a `js` target and be compiled before this task runs.",
            )
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

    private fun writePageShims(site: File, bundle: String) {
        val pages = File(site, "src/pages").apply { mkdirs() }
        val pageSource = pageBundle.orNull?.takeIf { it.isNotBlank() } ?: bundle
        pageFunctions.get().forEach { (name, function) ->
            File(pages, "$name.jsx").writeText(
                """
                // Generated by the ktjs-docusaurus Gradle plugin. Do not edit.
                export { $function as default } from '../../generated/$pageSource';
                """.trimIndent() + "\n",
            )
        }
    }

    private fun writeClientModuleShim(site: File, bundle: String) {
        val function = clientModuleFunction.orNull?.takeIf { it.isNotBlank() } ?: return
        val clientSource = clientModuleBundle.orNull?.takeIf { it.isNotBlank() } ?: bundle
        File(site, "client-module.mjs").writeText(
            """
            // Generated by the ktjs-docusaurus Gradle plugin. Do not edit.
            export { $function } from './generated/$clientSource';
            """.trimIndent() + "\n",
        )
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
