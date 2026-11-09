package io.github.shijianjs.ktjs.docusaurus.gradle

/**
 * Configuration of the `docusaurus` extension.
 *
 * Every property has a convention, so a module only sets what differs from the layout below.
 * Defaults assume a Kotlin/JS module with `src/jsMain/resources/{docs,blog,pages,static,css}`:
 *
 * ```
 * <module>/src/jsMain/resources/docs      -> <site>/docs
 * <module>/src/jsMain/resources/blog      -> <site>/blog
 * <module>/src/jsMain/resources/pages     -> <site>/src/pages
 * <module>/src/jsMain/resources/css       -> <site>/src/css
 * <module>/src/jsMain/resources/static    -> <site>/static
 * ```
 */
open class DocusaurusExtension @javax.inject.Inject constructor(
    objects: org.gradle.api.model.ObjectFactory,
) {
    /**
     * Directory holding the Markdown content and the static assets, relative to the module.
     */
    var contentDir: String = "src/jsMain/resources"

    /**
     * Name of the Kotlin function exported from the module that returns the site configuration.
     *
     * The function must be `@JsExport`ed and return a `Config`; the generated
     * `docusaurus.config.mjs` imports it and re-exports the object as its default export.
     */
    var configFunction: String = "docusaurusConfig"

    /**
     * Name of the exported Kotlin function returning `SidebarsConfig`, or `null` when the site has
     * no sidebars. When set, a `sidebars.mjs` is generated next to the config.
     */
    var sidebarsFunction: String? = null

    /**
     * Name of the exported Kotlin function Docusaurus calls after every route change, or `null`.
     *
     * When set, `<site>/client-module.mjs` re-exports it; list `./client-module.mjs` in the site's
     * `clientModules`. This is how browser-only behaviour (the TOC toggle) reaches a site whose
     * configuration and content are Kotlin and Markdown.
     */
    var clientModuleFunction: String? = null

    /**
     * File name of the bundle the client module lives in, relative to the generated directory.
     *
     * A client module that registers custom elements (`class X : HTMLElement()`) cannot share the
     * configuration's bundle: the generated class reads `HTMLElement` at module load, and Node — which
     * loads the configuration — has none. Keep those classes in a second Kotlin/JS module, add its
     * output through [additionalJs] and name its bundle here.
     */
    var clientModuleBundle: String? = null

    /**
     * File name of a browser-only bundle to publish under `<site>/static/ktjs-client/`, or `null`.
     *
     * Docusaurus puts `clientModules` into the server bundle as well, and a module that defines
     * custom elements (`class X : HTMLElement()`) reads `HTMLElement` when it loads — Node would crash
     * before the site is even built. Such a bundle has to be loaded by the browser alone, so it is
     * copied into `static/` and referenced with a `<script type="module">` tag in `headTags`.
     */
    var browserBundle: String? = null

    /**
     * Exported function the generated `static/ktjs-client/index.mjs` calls after importing
     * [browserBundle]. Required when [browserBundle] is set.
     */
    var browserBundleEntry: String? = null

    /**
     * Page file name (without extension) to exported Kotlin React component, for example
     * `mapOf("index" to "homePage")`.
     *
     * Each entry becomes `<site>/src/pages/<name>.jsx`, re-exporting the component as the default
     * export the pages plugin expects. The component itself is written in Kotlin (kotlin-react), so
     * a page never has to be hand-written JSX.
     */
    var pageFunctions: Map<String, String> = emptyMap()

    /**
     * File name of the bundle the page components live in, relative to the generated directory.
     *
     * Page components import `@theme/…`, which only exists inside Docusaurus' webpack build, so
     * they cannot live in the same bundle as the configuration: the configuration is loaded by
     * Node, where those aliases do not resolve. Keep the pages in a second Kotlin/JS module, add
     * its compiled output through [additionalJs] and name its bundle here.
     */
    var pageBundle: String? = null

    /**
     * Extra Kotlin/JS output copied into `<site>/generated` next to the main bundle.
     *
     * Use it for the page-components module; the caller is responsible for a task dependency on
     * that module's link task, because a file collection cannot carry one across projects.
     */
    val additionalJs: org.gradle.api.file.ConfigurableFileCollection = objects.fileCollection()

    /**
     * Directory the site is assembled into. Defaults to `<root>/build/js/docusaurus/<module>`.
     *
     * It deliberately lives under the root build's `js` directory: Node resolves the Docusaurus
     * packages from the `node_modules` the Kotlin/JS npm workspace installs there, without a second
     * install and without symlinks.
     */
    var siteDir: String? = null

    /**
     * Node executable. Defaults to the runtime Kotlin/JS installed, then to `node` on `PATH`.
     */
    var nodeExecutable: String? = null

    /**
     * `@docusaurus/core`'s CLI entry point. Defaults to the npm workspace copy under the root build.
     */
    var docusaurusBin: String? = null

    /**
     * Arguments passed to `docusaurus start`, e.g. `listOf("--port", "3001")`.
     */
    var startArguments: List<String> = emptyList()
}
