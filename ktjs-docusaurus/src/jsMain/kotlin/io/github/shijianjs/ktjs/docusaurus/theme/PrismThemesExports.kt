@file:JsModule("prism-react-renderer")

package io.github.shijianjs.ktjs.docusaurus.theme

/**
 * The `themes` named export of `prism-react-renderer`.
 *
 * Declared as a top-level `external val` so the compiler emits a named import; an `external object`
 * would compile to `import { Something } from ...` and the package has no such export. Read it
 * through [PrismThemes].
 */
@PublishedApi
internal external val themes: PrismThemeCollection

/** The members of `prism-react-renderer`'s `themes` export. */
external interface PrismThemeCollection {
    /** The Dracula theme. */
    val dracula: PrismTheme

    /** The Duotone Dark theme. */
    val duotoneDark: PrismTheme

    /** The Duotone Light theme. */
    val duotoneLight: PrismTheme

    /** GitHub's light theme. */
    val github: PrismTheme

    /** The Gruvbox Material dark theme. */
    val gruvboxMaterialDark: PrismTheme

    /** The Gruvbox Material light theme. */
    val gruvboxMaterialLight: PrismTheme

    /** The Jettwave dark theme. */
    val jettwaveDark: PrismTheme

    /** The Jettwave light theme. */
    val jettwaveLight: PrismTheme

    /** The Night Owl theme. */
    val nightOwl: PrismTheme

    /** The Night Owl light theme. */
    val nightOwlLight: PrismTheme

    /** The Oceanic Next theme. */
    val oceanicNext: PrismTheme

    /** The Okaidia theme. */
    val okaidia: PrismTheme

    /** The One Dark theme. */
    val oneDark: PrismTheme

    /** The One Light theme. */
    val oneLight: PrismTheme

    /** The Palenight theme. */
    val palenight: PrismTheme

    /** The Shades of Purple theme. */
    val shadesOfPurple: PrismTheme

    /** The Synthwave '84 theme. */
    val synthwave84: PrismTheme

    /** The Ultramin theme. */
    val ultramin: PrismTheme

    /** Visual Studio dark. */
    val vsDark: PrismTheme

    /** Visual Studio light. */
    val vsLight: PrismTheme
}
