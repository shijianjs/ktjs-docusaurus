package io.github.shijianjs.ktjs.docusaurus.kit

/**
 * The stylesheet every home section injects once.
 *
 * Everything is scoped by the `ktjs-` class prefix because the sections live in the page's own DOM,
 * and colours are read from inherited custom properties: a site overrides `--ktjs-accent` and the
 * rest of the palette follows, in both colour modes.
 */
internal const val HOME_CSS: String = """
.ktjs-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4.5rem 1.5rem 3.5rem;
  text-align: center;
  background:
    radial-gradient(60% 60% at 50% 0%, var(--ktjs-hero-glow, rgba(31, 111, 235, 0.16)) 0%, transparent 70%);
}

.ktjs-hero-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 46rem;
  gap: 1rem;
}

.ktjs-hero-logo {
  width: 7.5rem;
  height: 7.5rem;
  object-fit: contain;
}

.ktjs-hero-title {
  margin: 0;
  font-size: clamp(2.4rem, 5vw, 3.4rem);
  line-height: 1.1;
  letter-spacing: -0.02em;
  background: linear-gradient(120deg, var(--ktjs-accent, #1f6feb), var(--ktjs-accent-alt, #6cb6ff));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.ktjs-hero-tagline {
  margin: 0;
  max-width: 34rem;
  font-size: 1.15rem;
  line-height: 1.6;
  color: var(--ktjs-muted, var(--ifm-color-emphasis-700));
}

.ktjs-hero-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.ktjs-button {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.65rem 1.4rem;
  border-radius: 999px;
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.15s ease, box-shadow 0.15s ease, background-color 0.15s ease;
}

.ktjs-button:hover {
  text-decoration: none;
  transform: translateY(-1px);
}

.ktjs-button-primary {
  color: #fff;
  background: var(--ktjs-accent, #1f6feb);
  box-shadow: 0 6px 18px var(--ktjs-accent-shadow, rgba(31, 111, 235, 0.28));
}

.ktjs-button-primary:hover {
  color: #fff;
  background: var(--ktjs-accent-strong, #1a5fd0);
}

.ktjs-button-secondary {
  color: var(--ktjs-text, inherit);
  background: var(--ktjs-button-secondary-bg, rgba(127, 127, 127, 0.12));
}

.ktjs-button-secondary:hover {
  background: var(--ktjs-button-secondary-bg-hover, rgba(127, 127, 127, 0.2));
}

.ktjs-button-icon {
  font-size: 1.05em;
}

.ktjs-hero-badges {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.ktjs-badge-image {
  height: 1.35rem;
  display: block;
}

.ktjs-section {
  padding: 3.5rem 1.5rem;
}

.ktjs-section-tint {
  background: var(--ktjs-section-tint, rgba(127, 127, 127, 0.06));
}

.ktjs-section-inner {
  max-width: 68rem;
  margin: 0 auto;
}

.ktjs-section-title {
  margin: 0 0 2rem;
  font-size: 1.9rem;
  text-align: center;
  letter-spacing: -0.01em;
}

.ktjs-feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr));
  gap: 1rem;
}

.ktjs-feature-card {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  padding: 1.4rem;
  border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.22));
  border-radius: 14px;
  background: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
}

.ktjs-feature-icon-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 10px;
  color: var(--ktjs-accent, #1f6feb);
  background: var(--ktjs-chip-bg, rgba(31, 111, 235, 0.12));
  font-size: 1.3rem;
}

.ktjs-feature-title {
  margin: 0;
  font-size: 1.05rem;
}

.ktjs-feature-details {
  margin: 0;
  line-height: 1.6;
  color: var(--ktjs-muted, var(--ifm-color-emphasis-700));
}

.ktjs-next-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(16rem, 1fr));
  gap: 1rem;
}

.ktjs-next-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.2rem 1.4rem;
  border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.22));
  border-radius: 14px;
  color: inherit;
  background: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
  transition: border-color 0.15s ease, transform 0.15s ease;
}

.ktjs-next-card:hover {
  color: inherit;
  text-decoration: none;
  transform: translateY(-1px);
  border-color: var(--ktjs-accent, #1f6feb);
}

.ktjs-next-card-body {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.ktjs-next-card-title {
  font-weight: 600;
}

.ktjs-next-card-details {
  font-size: 0.92rem;
  line-height: 1.5;
  color: var(--ktjs-muted, var(--ifm-color-emphasis-700));
}

.ktjs-next-card-arrow {
  flex: none;
  color: var(--ktjs-accent, #1f6feb);
}
"""

/**
 * Injects [HOME_CSS] into the document once.
 *
 * Idempotent: every section calls it, and a site with three sections must not get three copies.
 */
fun ensureHomeStyles() {
    if (HomeStyleSheet.injected) return
    HomeStyleSheet.injected = true
    appendStyle(web.dom.document.head, HOME_CSS)
}

/** Owns the once-only flag, so the module itself keeps no mutable state. */
private object HomeStyleSheet {
    var injected: Boolean = false
}
