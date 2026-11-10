package io.github.shijianjs.ktjs.docusaurus.kit.elements

import web.cssom.Color

/**
 * The theme hooks the elements paint with.
 *
 * These three are the only colours that cross component borders, and they have to be able to change
 * together, so they live here rather than being respelled in each element. Every token falls back to
 * an Infima value, which is why light and dark mode work without a `[data-theme]` selector: custom
 * properties inherit into shadow trees, the values do not.
 *
 * Rules that live in a component's own `<style>` text (`:hover`, `::before`, …) use the same
 * `var()` spelling directly in CSS instead of going through these.
 */
internal val accent: Color = Color("var(--ktjs-accent, var(--ifm-color-primary))")

/** Chip / tinted-state background of the accent hue. */
internal val accentSoft: Color = Color("var(--ktjs-accent-soft, rgba(31, 111, 235, 0.12))")

/** Body copy inside cards and the hero tagline. */
internal val mutedText: Color = Color("var(--ifm-color-emphasis-700)")

/** The arrow / lowest-emphasis glyph. */
internal val subtleText: Color = Color("var(--ifm-color-emphasis-500)")

/** Background of a `tinted` section band. */
internal val sectionTint: Color = Color("var(--ktjs-section-tint, rgba(127, 127, 127, 0.05))")
