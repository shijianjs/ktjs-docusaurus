package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.cssom.BoxShadow
import web.cssom.Color
import web.cssom.CustomPropertyName
import web.cssom.rgb
import web.cssom.`var`

/**
 * The theme hooks the elements paint with, as typed custom property names.
 *
 * These are the only values that cross component borders, and they have to be able to change
 * together, so they live here rather than being respelled in each element. Every hook falls back to
 * an Infima value, which is why light and dark mode work without a `[data-theme]` selector: custom
 * properties inherit into shadow trees, the values do not.
 *
 * Each hook is declared once and referenced by name — by inline styles through the [Color] aliases
 * below, and by a component's own sheet through `var(hook, fallback)`. The type argument is the value
 * the hook carries, so `var` can type its fallback too.
 */
internal val accentHook = CustomPropertyName<Color>("--ktjs-accent")

internal val accentStrongHook = CustomPropertyName<Color>("--ktjs-accent-strong")

internal val accentSoftHook = CustomPropertyName<Color>("--ktjs-accent-soft")

internal val primaryColorHook = CustomPropertyName<Color>("--ifm-color-primary")

internal val cardBorderHook = CustomPropertyName<Color>("--ktjs-card-border")

internal val cardBackgroundHook = CustomPropertyName<Color>("--ktjs-card-bg")

internal val surfaceColorHook = CustomPropertyName<Color>("--ifm-background-surface-color")

internal val shadowHook = CustomPropertyName<BoxShadow>("--ktjs-shadow")

internal val outlineBorderHook = CustomPropertyName<Color>("--ktjs-outline-border")

internal val mutedTextHook = CustomPropertyName<Color>("--ifm-color-emphasis-700")

internal val subtleTextHook = CustomPropertyName<Color>("--ifm-color-emphasis-500")

internal val sectionTintHook = CustomPropertyName<Color>("--ktjs-section-tint")

/** The accent hue, as an inline-style value: `var(--ktjs-accent, var(--ifm-color-primary))`. */
internal val accent: Color = `var`(accentHook, `var`(primaryColorHook))

/** Chip / tinted-state background of the accent hue. */
internal val accentSoft: Color = `var`(accentSoftHook, rgb(31, 111, 235, 0.12))

/** Body copy inside cards and the hero tagline. */
internal val mutedText: Color = `var`(mutedTextHook)

/** The arrow / lowest-emphasis glyph. */
internal val subtleText: Color = `var`(subtleTextHook)

/** Background of a `tinted` section band. */
internal val sectionTint: Color = `var`(sectionTintHook, rgb(127, 127, 127, 0.05))
