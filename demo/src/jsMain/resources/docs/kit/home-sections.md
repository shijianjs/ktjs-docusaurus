---
sidebar_position: 1
---

# Home sections

Three classes, one shape: construct with the mount point, then hand over content through named
setters. Construction builds the subtree; the setters only mutate the nodes they own.

```kotlin
val hero = HomeHero(host)
hero.setLogo(useBaseUrl("img/favicon.svg"))
hero.setTitle("ktjs-docusaurus")
hero.setTagline("Documentation sites as Kotlin/JS projects")
hero.setPrimaryAction(HeroLink("Get started", useBaseUrl("/docs/intro")))
hero.setSecondaryAction(HeroAction("GitHub", REPOSITORY, "simple-icons:github"))
```

| Class | Setter | Data |
| --- | --- | --- |
| `HomeHero` | `setLogo` / `setTitle` / `setTagline` / `setPrimaryAction` / `setSecondaryAction` / `setBadges` | `HeroLink`, `HeroAction`, `HeroBadge` |
| `FeatureSection` | `setSectionTitle` / `setFeatures` | `FeatureItem` |
| `NextStepSection` | `setSectionTitle` / `setSteps` | `NextStepItem` |

Both list sections take a `tinted` flag, which switches the background of the section.

## Mounting from a React page

The page renders the mount points, then fills them once:

```kotlin
val heroRef = useRef<HTMLDivElement>()

Layout {
    title = siteConfig.title
    main { div { ref = heroRef } }
}

useLayoutEffect {
    heroRef.current?.let { host ->
        if (host.childElementCount == 0) {
            HomeHero(host).setTitle(siteConfig.title)
        }
    }
}
```

The guard makes the mount idempotent, which matters: StrictMode calls the effect twice, and a
navigated-away-and-back page re-runs it. Both must leave one hero, not two.

## Arrays grow and shrink

`setFeatures` and `setSteps` resize the existing list instead of clearing the grid: cards are reused
while the new list is at least as long, and only the surplus is removed.

## Copy and links

The sections are plain DOM, so they cannot render `<Translate>` and they do not know `baseUrl`.
Resolve both before calling a setter — `translate()` for the string, `useBaseUrl()` for any internal
`href`.
