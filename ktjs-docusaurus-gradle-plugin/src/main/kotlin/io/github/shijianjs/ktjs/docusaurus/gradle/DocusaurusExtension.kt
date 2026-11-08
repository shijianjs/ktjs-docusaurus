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
open class DocusaurusExtension {
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
