package io.github.shijianjs.ktjs.docusaurus.gradle

/**
 * Builds the argument list a Docusaurus command runs with.
 *
 * Three sources, in increasing precedence:
 *
 * 1. the base list the task was registered with (`start --no-open` plus
 *    [DocusaurusExtension.startArguments], which is what a site commits);
 * 2. [extra] — the `-Pdocusaurus.arguments` escape hatch for flags this plugin does not model
 *    (`--host 0.0.0.0`, `--poll 1000`, …), split on whitespace;
 * 3. the typed per-run values, `--locale` and `--port` (task options and `-Pdocusaurus.locale` /
 *    `-Pdocusaurus.port`).
 *
 * A typed value replaces whatever the base set: the CLI itself would let the last occurrence win,
 * but leaving two `--locale` flags in the command line makes the printed invocation look like it
 * contradicts itself. Both spellings of the flag are recognised (`--locale zh-Hans` and
 * `--locale=zh-Hans`).
 */
internal object CommandArguments {

    private const val LOCALE = "--locale"

    private const val PORT = "--port"

    fun resolve(base: List<String>, extra: String?, locale: String?, port: Int?): List<String> {
        val extraArgs = extra?.trim().orEmpty().split(' ').filter { it.isNotEmpty() }
        val merged = base + extraArgs

        val overrides = mutableListOf<String>()
        val replaced = mutableSetOf<String>()
        locale?.let {
            overrides += LOCALE
            overrides += it
            replaced += LOCALE
        }
        port?.let {
            overrides += PORT
            overrides += it.toString()
            replaced += PORT
        }
        if (overrides.isEmpty()) return merged
        return without(merged, replaced) + overrides
    }

    /** Drops [flags] and their values; a `--flag=value` token carries its value itself. */
    private fun without(args: List<String>, flags: Set<String>): List<String> {
        val kept = mutableListOf<String>()
        var index = 0
        while (index < args.size) {
            val token = args[index]
            val name = token.substringBefore('=')
            if (name in flags) {
                // `--locale zh-Hans` stores the value in the next token; `--locale=zh-Hans` does not.
                if (!token.contains('=')) index++
                index++
                continue
            }
            kept += token
            index++
        }
        return kept
    }
}
