package docs.e2e

import com.microsoft.playwright.Browser
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.junit.Options
import com.microsoft.playwright.junit.OptionsFactory
import java.nio.file.Path

/** Browser settings shared by the suite: desktop viewport and traces kept only for failures. */
class DocsOptions : OptionsFactory {
    override fun getOptions(): Options {
        PlaywrightAssertions.setDefaultAssertionTimeout(7_000.0)
        return Options()
            .setBaseUrl(DocsServer.baseUrl)
            .setTrace(Options.Trace.RETAIN_ON_FAILURE)
            .setOutputDir(Path.of(System.getProperty("e2e.output", "build/e2e-results")))
            .setContextOptions(
                Browser.NewContextOptions()
                    .setViewportSize(1280, 800),
            )
    }
}
