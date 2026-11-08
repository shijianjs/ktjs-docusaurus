package io.github.shijianjs.ktjs.docusaurus.kit

/**
 * Value types the home sections accept.
 *
 * Every string is already resolved for the current locale: the sections are plain DOM, they cannot
 * render Docusaurus' React `<Translate>`. The caller resolves the copy and passes plain strings;
 * internal `href`s must go through `useBaseUrl` first, because a raw `<a href>` gets no base URL
 * prefixing.
 *
 * `icon` fields are Iconify icon names (for example `lucide:sparkles`), rendered by the official
 * `<iconify-icon>` element, which fetches the glyph from the public Iconify API. The kit ships no
 * icon data of its own.
 */

/** A hero call to action. */
data class HeroLink(
    val label: String,
    val href: String,
)

/** The secondary hero action, which also carries a glyph. */
data class HeroAction(
    val label: String,
    val href: String,
    /** Iconify icon name, for example `simple-icons:github`. */
    val icon: String,
)

/** A shields.io-style badge in the hero row. Always an external link. */
data class HeroBadge(
    val href: String,
    val src: String,
    val alt: String,
)

/** One card of the feature grid. */
data class FeatureItem(
    /** Iconify icon name, for example `lucide:sparkles`. */
    val icon: String,
    val title: String,
    val details: String,
)

/** One card of the "where to go next" row. */
data class NextStepItem(
    /** Already base-URL resolved. */
    val href: String,
    val title: String,
    val details: String,
)
