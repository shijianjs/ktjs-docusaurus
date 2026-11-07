package io.github.shijianjs.docusaurus.theme

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
    val dracula: PrismTheme get() = themes.dracula

    /** The Duotone Dark theme. */
    val duotoneDark: PrismTheme get() = themes.duotoneDark

    /** The Duotone Light theme. */
    val duotoneLight: PrismTheme get() = themes.duotoneLight

    /** GitHub's light theme. */
    val github: PrismTheme get() = themes.github

    /** The Gruvbox Material dark theme. */
    val gruvboxMaterialDark: PrismTheme get() = themes.gruvboxMaterialDark

    /** The Gruvbox Material light theme. */
    val gruvboxMaterialLight: PrismTheme get() = themes.gruvboxMaterialLight

    /** The Jettwave dark theme. */
    val jettwaveDark: PrismTheme get() = themes.jettwaveDark

    /** The Jettwave light theme. */
    val jettwaveLight: PrismTheme get() = themes.jettwaveLight

    /** The Night Owl theme. */
    val nightOwl: PrismTheme get() = themes.nightOwl

    /** The Night Owl light theme. */
    val nightOwlLight: PrismTheme get() = themes.nightOwlLight

    /** The Oceanic Next theme. */
    val oceanicNext: PrismTheme get() = themes.oceanicNext

    /** The Okaidia theme. */
    val okaidia: PrismTheme get() = themes.okaidia

    /** The One Dark theme. */
    val oneDark: PrismTheme get() = themes.oneDark

    /** The One Light theme. */
    val oneLight: PrismTheme get() = themes.oneLight

    /** The Palenight theme. */
    val palenight: PrismTheme get() = themes.palenight

    /** The Shades of Purple theme. */
    val shadesOfPurple: PrismTheme get() = themes.shadesOfPurple

    /** The Synthwave '84 theme. */
    val synthwave84: PrismTheme get() = themes.synthwave84

    /** The Ultramin theme. */
    val ultramin: PrismTheme get() = themes.ultramin

    /** Visual Studio dark. */
    val vsDark: PrismTheme get() = themes.vsDark

    /** Visual Studio light. */
    val vsLight: PrismTheme get() = themes.vsLight
}
