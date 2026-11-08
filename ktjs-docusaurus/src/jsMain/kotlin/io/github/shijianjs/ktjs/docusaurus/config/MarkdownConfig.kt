package io.github.shijianjs.ktjs.docusaurus.config

import js.objects.Record
import kotlin.js.JsAny
import kotlin.js.Promise
import js.reflect.unsafeCast

/**
 * Upstream `MarkdownConfig`: site-wide Markdown / MDX processing.
 *
 * Plugin-level Markdown options (remark and rehype plugins) live in `MdxOptions` of the docs, blog
 * and pages plugins instead; this type is what `config.markdown` accepts.
 */
external interface MarkdownConfig {
    /** Default format of a file. `null` means Docusaurus' `mdx`. */
    var format: MarkdownFormat?

    /** Replaces the built-in front matter parser. */
    var parseFrontMatter: ParseFrontMatter?

    /** Render ` ```mermaid ` fences as diagrams. Off by default. */
    var mermaid: Boolean?

    /** Convert emoji shortcodes such as `:smile:`. On by default. */
    var emoji: Boolean?

    /** Preprocess the raw Markdown before it is compiled. */
    var preprocessor: MarkdownPreprocessor?

    /** Flags that make migrating from MDX 1 easier. */
    var mdx1Compat: Mdx1CompatOptions?

    /**
     * Upstream `ProcessorOptions['remarkRehypeOptions']` from `@mdx-js/mdx`.
     *
     * The type belongs to MDX, not to Docusaurus, so it stays opaque here rather than being
     * re-declared. Build it as an object literal and pass it through.
     */
    var remarkRehypeOptions: JsAny?

    /** Control over the heading anchors Docusaurus generates. */
    var anchors: MarkdownAnchorsConfig?

    /** What to do about broken Markdown links and images. */
    var hooks: MarkdownHooks?
}

/** Upstream `(args: {filePath: string; fileContent: string}) => string`. */
external interface MarkdownPreprocessor {
    /** Receives the file path and its content, returns the content to compile. */
    operator fun invoke(args: MarkdownPreprocessorArgs): String
}

/** The argument object of [MarkdownPreprocessor]. */
external interface MarkdownPreprocessorArgs {
    /** POSIX path of the file, relative to the site directory. */
    var filePath: String

    /** The raw file content. */
    var fileContent: String
}

/** Upstream `MDX1CompatOptions`. */
external interface Mdx1CompatOptions {
    /** Keep MDX 1 comments. */
    var comments: Boolean?

    /** Keep MDX 1 admonitions. */
    var admonitions: Boolean?

    /** Keep MDX 1 heading ids. */
    var headingIds: Boolean?
}

/** Upstream `MarkdownAnchorsConfig`. */
external interface MarkdownAnchorsConfig {
    /** Preserve the case of the heading text when generating anchor ids. */
    var maintainCase: Boolean?
}

/** Upstream `MarkdownHooks`. */
external interface MarkdownHooks {
    /** `ReportingSeverity | OnBrokenMarkdownLinksFunction`. */
    var onBrokenMarkdownLinks: ReportingSeverityOrOnBrokenMarkdownLinks?

    /** `ReportingSeverity | OnBrokenMarkdownImagesFunction`. */
    var onBrokenMarkdownImages: ReportingSeverityOrOnBrokenMarkdownImages?
}

/** Upstream `ParseFrontMatter`. */
external interface ParseFrontMatter {
    /** Parses one file's front matter. */
    operator fun invoke(params: ParseFrontMatterParams): Promise<ParseFrontMatterResult>
}

/** The argument object of [ParseFrontMatter]. */
external interface ParseFrontMatterParams {
    /** POSIX path of the file, relative to the site directory. */
    var filePath: String

    /** The raw file content. */
    var fileContent: String

    /** The built-in parser, for front matter that still lives in the file. */
    var defaultParseFrontMatter: DefaultParseFrontMatter
}

/** Upstream `DefaultParseFrontMatter`. */
external interface DefaultParseFrontMatter {
    /** Runs the built-in parser on the same file. */
    operator fun invoke(params: ParseFrontMatterParams): Promise<ParseFrontMatterResult>
}

/**
 * Upstream `{frontMatter: {[key: string]: unknown}; content: string}`.
 *
 * The front matter keeps its raw shape: every field is unknown until a page reads it.
 */
external interface ParseFrontMatterResult {
    /** Parsed front matter. */
    var frontMatter: Record<JsAny, JsAny>

    /** The Markdown body left after the front matter was removed. */
    var content: String
}

/** Upstream `ReportingSeverity | OnBrokenMarkdownLinksFunction`. */
external interface ReportingSeverityOrOnBrokenMarkdownLinks

/** Wraps the [ReportingSeverity] branch. */
fun ReportingSeverityOrOnBrokenMarkdownLinks(
    value: ReportingSeverity,
): ReportingSeverityOrOnBrokenMarkdownLinks = unsafeCast<ReportingSeverityOrOnBrokenMarkdownLinks>(value)

/** Wraps the callback branch. */
fun ReportingSeverityOrOnBrokenMarkdownLinks(
    value: (OnBrokenMarkdownLinkParams) -> String,
): ReportingSeverityOrOnBrokenMarkdownLinks = unsafeCast<ReportingSeverityOrOnBrokenMarkdownLinks>(value)

/** Upstream `ReportingSeverity | OnBrokenMarkdownImagesFunction`. */
external interface ReportingSeverityOrOnBrokenMarkdownImages

/** Wraps the [ReportingSeverity] branch. */
fun ReportingSeverityOrOnBrokenMarkdownImages(
    value: ReportingSeverity,
): ReportingSeverityOrOnBrokenMarkdownImages = unsafeCast<ReportingSeverityOrOnBrokenMarkdownImages>(value)

/** Wraps the callback branch. */
fun ReportingSeverityOrOnBrokenMarkdownImages(
    value: (OnBrokenMarkdownImageParams) -> String,
): ReportingSeverityOrOnBrokenMarkdownImages = unsafeCast<ReportingSeverityOrOnBrokenMarkdownImages>(value)

/** The argument object of `OnBrokenMarkdownLinksFunction`. */
external interface OnBrokenMarkdownLinkParams {
    /** Path of the file the link was found in, relative to the site directory. */
    var sourceFilePath: String

    /** The unresolved link URL. */
    var url: String

    /** The Markdown AST node. Upstream type is a mdast `Link | Definition`. */
    var node: JsAny
}

/** The argument object of `OnBrokenMarkdownImagesFunction`. */
external interface OnBrokenMarkdownImageParams {
    /** Path of the file the image was found in, relative to the site directory. */
    var sourceFilePath: String

    /** The unresolved image URL. */
    var url: String

    /** The Markdown AST node. Upstream type is a mdast `Image`. */
    var node: JsAny
}
