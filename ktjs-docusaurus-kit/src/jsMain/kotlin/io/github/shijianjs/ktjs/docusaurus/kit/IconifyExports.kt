@file:JsModule("iconify-icon")

package io.github.shijianjs.ktjs.docusaurus.kit

/**
 * The official `iconify-icon` package, imported for its side effect: the module registers the
 * `<iconify-icon>` custom element.
 *
 * The kit creates those elements and sets their `icon` attribute; the glyph itself is fetched from
 * the public Iconify API at runtime, so no icon data ships with the kit. Read it through
 * [ensureIconifyRegistered] — a `JsModule` file may only contain external declarations.
 */
internal external object IconifyIconModule
