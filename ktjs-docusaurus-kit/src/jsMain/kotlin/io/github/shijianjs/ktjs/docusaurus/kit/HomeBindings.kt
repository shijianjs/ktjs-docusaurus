package io.github.shijianjs.ktjs.docusaurus.kit

import js.reflect.unsafeCast
import web.html.HTMLElement

/**
 * Binds a custom-element host to the section that renders into it.
 *
 * These are the only pieces of glue between the DOM and the kit: pick the section the host owns, then
 * let the section read its own input (`fillFrom`). The attribute names, child tags and cleanup live in
 * `HomeHero.fillFrom` / `HomeSection.fillFrom`, because they are the declarative spelling of those
 * classes' own setters.
 *
 * **The section lives on the element it renders into.** `connectedCallback` runs more than once for the
 * same host (StrictMode, re-mount, client-side navigation), and each run must find the section it built
 * the first time instead of appending a second one. That state is a property of the element itself —
 * see [HomeHostState] — so it is released together with the element. An external map keyed by host
 * would keep every host alive for the lifetime of the page.
 */
private external interface HomeHostState {
    var ktjsHero: HomeHero?

    var ktjsFeatures: FeatureSection?

    var ktjsNextSteps: NextStepSection?
}

private fun HTMLElement.state(): HomeHostState = unsafeCast(this)

/** Binds a `<ktjs-hero>` host; safe to call on every connect. */
fun bindHomeHero(host: HTMLElement): HomeHero {
    val state = host.state()
    val component = state.ktjsHero ?: HomeHero(host).also { state.ktjsHero = it }
    component.fillFrom(host)
    return component
}

/** Binds a `<ktjs-features>` host; safe to call on every connect. */
fun bindFeatureSection(host: HTMLElement): FeatureSection {
    val state = host.state()
    val component = state.ktjsFeatures
        ?: FeatureSection(host, tinted = host.hasAttribute(HomeMarkup.TINTED))
            .also { state.ktjsFeatures = it }
    component.fillFrom(host)
    return component
}

/** Binds a `<ktjs-next-steps>` host; safe to call on every connect. */
fun bindNextStepSection(host: HTMLElement): NextStepSection {
    val state = host.state()
    val component = state.ktjsNextSteps ?: NextStepSection(host).also { state.ktjsNextSteps = it }
    component.fillFrom(host)
    return component
}
