package io.github.shijianjs.ktjs.docusaurus.kit.elements

/**
 * The stylesheet every home element injects into its own shadow root.
 *
 * Shadow scoping means the class names cannot leak, but they keep the `ktjs-` prefix so the trees
 * stay greppable. Theme colours cross the boundary through inheritance: `--ktjs-*` tokens (a site can
 * override them) fall back to Infima's `--ifm-*` values, so light and dark mode work without a single
 * `[data-theme]` selector here.
 */
// language=CSS
internal const val HOME_CSS: String = """
:host {
  display: block;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

/* ------------------------------------------------------------------ hero */

.ktjs-hero {
  position: relative;
  isolation: isolate;
  /* Span the viewport so the glow below is clipped at the window edge, not by the framework's
     content container; the clip keeps the 100vw from adding a horizontal scrollbar. */
  width: 100vw;
  margin-inline: calc(50% - 50vw);
  overflow-x: clip;
  padding: 4.5rem 1.5rem 4rem;
  text-align: center;
}

.ktjs-hero::before,
.ktjs-hero::after {
  content: '';
  position: absolute;
  z-index: -1;
  border-radius: 50%;
  pointer-events: none;
}

.ktjs-hero::before {
  top: -18rem;
  left: 50%;
  width: 52rem;
  height: 52rem;
  transform: translateX(-50%);
  background: radial-gradient(circle, var(--ktjs-glow, rgba(31, 111, 235, 0.16)), transparent 70%);
}

.ktjs-hero::after {
  right: -10rem;
  bottom: -14rem;
  width: 34rem;
  height: 34rem;
  background: radial-gradient(circle, var(--ktjs-glow-warm, rgba(255, 193, 7, 0.10)), transparent 70%);
}

.ktjs-hero-inner {
  max-width: 46rem;
  margin: 0 auto;
}

.ktjs-logo-stage {
  display: inline-block;
  line-height: 0;
}

.ktjs-logo {
  display: block;
  width: clamp(7rem, 16vw, 10rem);
  height: auto;
  margin: 0 auto;
  filter: drop-shadow(0 14px 28px var(--ktjs-glow, rgba(31, 111, 235, 0.16)));
}

.ktjs-title {
  margin: 1.5rem 0 0;
  font-size: clamp(2.25rem, 5vw, 3.25rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.1;
  color: var(--ktjs-accent, var(--ifm-color-primary));
}

@supports (-webkit-background-clip: text) or (background-clip: text) {
  .ktjs-title {
    background-image: linear-gradient(120deg, var(--ktjs-accent-deep, #1a5fd0), var(--ktjs-accent-bright, #6cb6ff));
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    /* `background-clip: text` only paints inside the padding box, so the tight line box would leave
       the tail of "g" transparent; 0.06em was not enough at the clamped font size. */
    padding-bottom: 0.14em;
  }
}

.ktjs-tagline {
  max-width: 34rem;
  margin: 1.25rem auto 0;
  font-size: clamp(1.05rem, 2.2vw, 1.25rem);
  line-height: 1.65;
  color: var(--ifm-color-emphasis-700);
}

.ktjs-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.75rem 1rem;
  margin-top: 2.25rem;
}

.ktjs-badges {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 2rem;
}

/* --------------------------------------------------------------- buttons */

.ktjs-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.7rem 1.6rem;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 1rem;
  font-weight: 600;
  line-height: 1.4;
  text-decoration: none;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.ktjs-button-primary {
  background-color: var(--ktjs-accent, var(--ifm-color-primary));
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  color: #fff;
}

.ktjs-button-primary:hover,
.ktjs-button-primary:focus-visible {
  background-color: var(--ktjs-accent-strong, #1a5fd0);
  border-color: var(--ktjs-accent-strong, #1a5fd0);
  color: #fff;
  text-decoration: none;
  box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.24));
}

.ktjs-button-secondary {
  background-color: transparent;
  border-color: var(--ktjs-outline-border, rgba(127, 127, 127, 0.35));
  color: var(--ktjs-accent, var(--ifm-color-primary));
}

.ktjs-button-secondary:hover,
.ktjs-button-secondary:focus-visible {
  background-color: var(--ktjs-accent-soft, rgba(31, 111, 235, 0.10));
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  color: var(--ktjs-accent, var(--ifm-color-primary));
  text-decoration: none;
}

.ktjs-button-icon {
  font-size: 1.125rem;
}

/* ---------------------------------------------------------------- badges */

.ktjs-badge {
  display: inline-flex;
}

.ktjs-badge-image {
  display: block;
  width: auto;
  height: 20px;
}

/* -------------------------------------------------------------- sections */

.ktjs-section {
  /* Same viewport-spanning rule as the hero: the tinted band has to reach the window edge. */
  width: 100vw;
  margin-inline: calc(50% - 50vw);
  padding: 4.5rem 0;
}

.ktjs-section-tint {
  background-color: var(--ktjs-section-tint, rgba(127, 127, 127, 0.05));
}

.ktjs-section-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.ktjs-section-title {
  margin: 0;
  text-align: center;
  font-size: clamp(1.5rem, 3vw, 2rem);
  letter-spacing: -0.01em;
}

/* -------------------------------------------------------------- features */

.ktjs-feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(17rem, 100%), 1fr));
  gap: 1.25rem;
  margin-top: 2.75rem;
}

.ktjs-feature-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 1.5rem;
  border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.24));
  border-radius: 1rem;
  background-color: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.ktjs-feature-card:hover {
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));
}

.ktjs-feature-icon-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  margin-bottom: 1rem;
  border-radius: 0.75rem;
  background-color: var(--ktjs-accent-soft, rgba(31, 111, 235, 0.12));
  color: var(--ktjs-accent, var(--ifm-color-primary));
}

.ktjs-feature-icon {
  font-size: 1.375rem;
}

.ktjs-feature-title {
  margin: 0 0 0.5rem;
  font-size: 1.0625rem;
  font-weight: 600;
}

.ktjs-feature-details {
  margin: 0;
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--ifm-color-emphasis-700);
}

/* ------------------------------------------------------------ next steps */

.ktjs-next-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(20rem, 100%), 1fr));
  gap: 1rem;
  max-width: 54rem;
  margin: 2.75rem auto 0;
}

.ktjs-next-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  height: 100%;
  padding: 1.25rem 1.375rem;
  border: 1px solid var(--ktjs-card-border, rgba(127, 127, 127, 0.24));
  border-radius: 0.875rem;
  background-color: var(--ktjs-card-bg, var(--ifm-background-surface-color, #fff));
  color: inherit;
  text-decoration: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.ktjs-next-card:hover,
.ktjs-next-card:focus-visible {
  border-color: var(--ktjs-accent, var(--ifm-color-primary));
  box-shadow: var(--ktjs-shadow, 0 10px 24px rgba(31, 111, 235, 0.18));
  color: inherit;
  text-decoration: none;
}

.ktjs-next-card-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 0;
}

.ktjs-next-card-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--ktjs-accent, var(--ifm-color-primary));
}

.ktjs-next-card-details {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--ifm-color-emphasis-700);
}

.ktjs-next-card-arrow {
  flex-shrink: 0;
  font-size: 1.25rem;
  color: var(--ifm-color-emphasis-500);
}

/* ----------------------------------------------------------- breakpoints */

@media screen and (max-width: 996px) {
  .ktjs-hero {
    padding: 3rem 1.5rem;
  }

  .ktjs-section {
    padding: 3.5rem 0;
  }
}

@media screen and (max-width: 576px) {
  .ktjs-section-inner {
    padding: 0 1rem;
  }

  .ktjs-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
"""
