package io.github.shijianjs.ktjs.docusaurus.plugins.idealimage

import io.github.shijianjs.ktjs.docusaurus.config.PluginOptions

/**
 * Options of `@docusaurus/plugin-ideal-image`: images below `<img ideal>` become lazy, responsive
 * pictures generated at build time.
 *
 * Widths come either from [sizes] or from [min] / [max] / [steps]; when a width is larger than the
 * source image, the source width is used instead (images are never scaled up).
 *
 * @see <a href="https://docusaurus.io/docs/api/plugins/@docusaurus/plugin-ideal-image">Ideal image plugin</a>
 */
external interface IdealImageOptions : PluginOptions {
    /** Filename template for the generated files, e.g. `[name]-[width].[ext]`. */
    var name: String?

    /** Every width to generate, in pixels. */
    var sizes: Array<Double>?

    /** A single width to generate, in pixels. */
    var size: Double?

    /** Smallest width generated when [sizes] is omitted. */
    var min: Double?

    /** Largest width generated when [sizes] is omitted. */
    var max: Double?

    /** Number of widths generated between [min] and [max], inclusive. */
    var steps: Double?

    /** JPEG compression quality, `0`–`100`. */
    var quality: Double?

    /** Generate real images in dev mode too; `true` skips the work for faster rebuilds. */
    var disableInDev: Boolean?
}
