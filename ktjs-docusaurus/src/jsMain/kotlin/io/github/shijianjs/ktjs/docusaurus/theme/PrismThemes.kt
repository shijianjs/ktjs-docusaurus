package io.github.shijianjs.ktjs.docusaurus.theme

/**
 * The bundled `prism-react-renderer` themes, ready to pass to [PrismConfig.theme] or
 * [PrismConfig.darkTheme].
 *
 * Only the data half of the package is wrapped: Docusaurus uses the themes, while the `Highlight`
 * React component belongs to that package's own API surface and stays out of this wrapper.
 *
 * ```kotlin
 * prism = unsafeJso<PrismConfig> {
 *     theme = PrismThemes.github
 *     darkTheme = PrismThemes.dracula
 *     additionalLanguages = arrayOf("bash", "rust", "sql")
 * }
 * ```
 */
object PrismThemes {
    /** The Dracula theme. */
    inline val dracula: PrismTheme get() = themes.dracula

    /** The Duotone Dark theme. */
    inline val duotoneDark: PrismTheme get() = themes.duotoneDark

    /** The Duotone Light theme. */
    inline val duotoneLight: PrismTheme get() = themes.duotoneLight

    /** GitHub's light theme. */
    inline val github: PrismTheme get() = themes.github

    /** The Gruvbox Material dark theme. */
    inline val gruvboxMaterialDark: PrismTheme get() = themes.gruvboxMaterialDark

    /** The Gruvbox Material light theme. */
    inline val gruvboxMaterialLight: PrismTheme get() = themes.gruvboxMaterialLight

    /** The Jettwave dark theme. */
    inline val jettwaveDark: PrismTheme get() = themes.jettwaveDark

    /** The Jettwave light theme. */
    inline val jettwaveLight: PrismTheme get() = themes.jettwaveLight

    /** The Night Owl theme. */
    inline val nightOwl: PrismTheme get() = themes.nightOwl

    /** The Night Owl light theme. */
    inline val nightOwlLight: PrismTheme get() = themes.nightOwlLight

    /** The Oceanic Next theme. */
    inline val oceanicNext: PrismTheme get() = themes.oceanicNext

    /** The Okaidia theme. */
    inline val okaidia: PrismTheme get() = themes.okaidia

    /** The One Dark theme. */
    inline val oneDark: PrismTheme get() = themes.oneDark

    /** The One Light theme. */
    inline val oneLight: PrismTheme get() = themes.oneLight

    /** The Palenight theme. */
    inline val palenight: PrismTheme get() = themes.palenight

    /** The Shades of Purple theme. */
    inline val shadesOfPurple: PrismTheme get() = themes.shadesOfPurple

    /** The Synthwave '84 theme. */
    inline val synthwave84: PrismTheme get() = themes.synthwave84

    /** The Ultramin theme. */
    inline val ultramin: PrismTheme get() = themes.ultramin

    /** Visual Studio dark. */
    inline val vsDark: PrismTheme get() = themes.vsDark

    /** Visual Studio light. */
    inline val vsLight: PrismTheme get() = themes.vsLight
}
