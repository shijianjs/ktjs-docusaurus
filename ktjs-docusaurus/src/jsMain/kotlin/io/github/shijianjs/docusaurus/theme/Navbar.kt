package io.github.shijianjs.docusaurus.theme

import io.github.shijianjs.docusaurus.config.StringOrDouble
import kotlin.js.JsAny
import js.reflect.unsafeCast

/**
 * Upstream `Navbar` from `@docusaurus/theme-common`.
 *
 * @see <a href="https://docusaurus.io/docs/api/themes/configuration#navbar">Navbar configuration</a>
 */
external interface Navbar {
    /** `'dark'` or `'primary'`. */
    var style: NavbarStyle?

    /** Slide the navbar away when scrolling down, bring it back when scrolling up. */
    var hideOnScroll: Boolean?

    /** Site title shown next to the logo. Defaults to `themeConfig`'s site title. */
    var title: String?

    /** Navbar entries, rendered left to right unless they declare a [NavbarItem.position]. */
    var items: Array<NavbarItem>?

    /** Logo shown at the left end of the navbar. */
    var logo: NavbarLogo?
}

/** Upstream `'dark' | 'primary'`. */
sealed external interface NavbarStyle

/** Wraps a value coming from JavaScript. Prefer [NavbarStyles]. */
fun NavbarStyle(value: String): NavbarStyle = unsafeCast<NavbarStyle>(value)

/** The literals of [NavbarStyle]. */
object NavbarStyles {
    /** Dark background. */
    inline val dark: NavbarStyle get() = NavbarStyle("dark")

    /** Primary (brand) background. */
    inline val primary: NavbarStyle get() = NavbarStyle("primary")
}

/** Upstream `NavbarLogo`. */
external interface NavbarLogo {
    /** Alt text, read by screen readers. */
    var alt: String?

    /** Image URL, resolved against a static directory. */
    var src: String

    /** Image URL used in dark mode. */
    var srcDark: String?

    /** Where the logo links to. Defaults to the site root. */
    var href: String?

    /** Rendered width. `string | number`. */
    var width: StringOrDouble?

    /** Rendered height. `string | number`. */
    var height: StringOrDouble?

    /** Link target, e.g. `_self`. */
    var target: String?

    /** Inline style object. Upstream type is `object`. */
    var style: js.objects.Record<JsAny, String>?

    /** Extra class name for the logo wrapper. */
    var className: String?
}

/** Upstream `'left' | 'right'`. */
sealed external interface NavbarItemPosition

/** Wraps a value coming from JavaScript. Prefer [NavbarItemPositions]. */
fun NavbarItemPosition(value: String): NavbarItemPosition = unsafeCast<NavbarItemPosition>(value)

/** The literals of [NavbarItemPosition]. */
object NavbarItemPositions {
    /** Left group, the default. */
    inline val left: NavbarItemPosition get() = NavbarItemPosition("left")

    /** Right group. */
    inline val right: NavbarItemPosition get() = NavbarItemPosition("right")
}

/**
 * Upstream `NavbarItem`.
 *
 * Docusaurus declares it as `{type?: string; items?: NavbarItem[]; label?: string;
 * position?: 'left' | 'right'} & {[key: string]: unknown}` and lets every item component read its
 * own fields. This declaration narrows [type] to the built-in components and lists their fields, so
 * a misspelt key is a compile error; an item whose `type` is a custom component path still uses
 * [NavbarItemType] with the path.
 */
external interface NavbarItem {
    /** Built-in item type, or the module path of a custom navbar item component. */
    var type: NavbarItemType?

    /** Children of a [NavbarItemTypes.dropdown] item. */
    var items: Array<NavbarItem>?

    /** Visible text. */
    var label: String?

    /** Which end of the navbar the item is rendered at. */
    var position: NavbarItemPosition?

    /** Extra class name. */
    var className: String?

    /** `doc` / `docSidebar`: the id of the doc to link to. */
    var docId: String?

    /** `docSidebar`: the id of the sidebar to link to. */
    var sidebarId: String?

    /** `docsVersionDropdown`: which docs plugin instance to read versions from. */
    var docsPluginId: String?

    /** `docsVersionDropdown`: restrict the dropdown to these versions. */
    var versions: Array<String>?

    /** `link` / `doc`: an internal path. One of [to] and [href] is required. */
    var to: String?

    /** `link`: an external URL. */
    var href: String?

    /** `html`: raw markup. */
    var value: String?

    /** `link` / `doc`: a regexp that marks the item active on extra routes. */
    var activeBaseRegex: String?

    /** `link`: prefix `href` with the site's `baseUrl`. */
    var prependBaseUrlToHref: Boolean?

    /** `dropdown` / `docsVersionDropdown`: entries before the generated ones. */
    var dropdownItemsBefore: Array<NavbarItem>?

    /** `dropdown` / `docsVersionDropdown`: entries after the generated ones. */
    var dropdownItemsAfter: Array<NavbarItem>?

    /** `dropdown` / `docsVersionDropdown`: do not mark the dropdown active. */
    var dropdownActiveClassDisabled: Boolean?
}

/**
 * Upstream `string`: `'doc' | 'docSidebar' | 'docsVersionDropdown' | 'link' | 'dropdown' | 'html' |
 * 'search' | 'localeDropdown'`, or the module path of a custom item.
 *
 * Prefer [NavbarItemTypes]; the [String] factory exists for custom item paths and values read back
 * from JavaScript.
 */
sealed external interface NavbarItemType

/** Wraps a value coming from JavaScript, e.g. a custom item's module path. */
fun NavbarItemType(value: String): NavbarItemType = unsafeCast<NavbarItemType>(value)

/**
 * The built-in navbar item types.
 *
 * There is no `link` and no `default` type: a plain link (internal `to`, external `href`) is an item
 * **without** a [NavbarItem.type], and that is the only spelling Docusaurus accepts. Any other type
 * string must match a built-in item or a custom component path, otherwise the build fails with
 * `Bad navbar item type`.
 */
object NavbarItemTypes {
    /** Links to a document. */
    inline val doc: NavbarItemType get() = NavbarItemType("doc")

    /** Links to a sidebar's first document. */
    inline val docSidebar: NavbarItemType get() = NavbarItemType("docSidebar")

    /** Shows one specific docs version. */
    inline val docsVersion: NavbarItemType get() = NavbarItemType("docsVersion")

    /** A dropdown listing the docs versions. */
    inline val docsVersionDropdown: NavbarItemType get() = NavbarItemType("docsVersionDropdown")

    /** A dropdown with custom entries. */
    inline val dropdown: NavbarItemType get() = NavbarItemType("dropdown")

    /** Raw markup. */
    inline val html: NavbarItemType get() = NavbarItemType("html")

    /** The search bar of a search theme. */
    inline val search: NavbarItemType get() = NavbarItemType("search")

    /** A dropdown switching the locale. */
    inline val localeDropdown: NavbarItemType get() = NavbarItemType("localeDropdown")
}
