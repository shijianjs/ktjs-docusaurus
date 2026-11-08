package io.github.shijianjs.ktjs.docusaurus.kit

import kotlin.js.JsAny

/** Registers `<iconify-icon>` once; every section that renders an icon calls it. */
fun ensureIconifyRegistered() {
    IconifyIconRegistration.module = IconifyIconModule
}

/** Owns the reference that keeps the side-effect import alive. */
private object IconifyIconRegistration {
    var module: JsAny? = null
}
