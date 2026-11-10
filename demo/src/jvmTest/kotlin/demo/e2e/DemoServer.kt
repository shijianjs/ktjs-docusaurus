package demo.e2e

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path

/**
 * Serves the built Docusaurus site from the test JVM.
 *
 * The JDK's `HttpServer` is used instead of a generic static-file helper for one reason: Docusaurus
 * loads the custom elements through an ES module (`ktjs-client/index.mjs`), and browsers enforce a
 * JavaScript MIME type for module scripts. General-purpose servers tend to return
 * `application/octet-stream` for `.mjs`, which silently stops the whole element bundle from running.
 *
 * The site is built with `baseUrl = /ktjs-docusaurus/`, so that prefix is stripped here and the
 * files are read straight from the Docusaurus output directory — no staging copy needed.
 */
object DemoServer {
    private const val BASE_PATH = "/ktjs-docusaurus/"

    private val root: Path = Path.of(
        System.getProperty("demo.dist")
            ?: Path.of("build", "e2e-site").toString(),
    ).toAbsolutePath().normalize()

    private val server: HttpServer by lazy {
        HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/", ::serve)
            start()
            Runtime.getRuntime().addShutdownHook(Thread { stop(0) })
        }
    }

    val baseUrl: String
        get() = "http://127.0.0.1:${server.address.port}/"

    val homeUrl: String
        get() = baseUrl.trimEnd('/') + BASE_PATH

    val zhHomeUrl: String
        get() = homeUrl + "zh-Hans/"

    private fun serve(exchange: HttpExchange) {
        try {
            val requested = exchange.requestURI.path
            if (!requested.startsWith(BASE_PATH)) {
                notFound(exchange)
                return
            }

            var file = root.resolve(requested.removePrefix(BASE_PATH)).normalize()
            if (!file.startsWith(root)) {
                notFound(exchange)
                return
            }
            if (Files.isDirectory(file)) file = file.resolve("index.html")
            if (!Files.isRegularFile(file)) {
                notFound(exchange)
                return
            }

            val bytes = Files.readAllBytes(file)
            exchange.responseHeaders.set("Content-Type", contentType(file.fileName.toString()))
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        } finally {
            exchange.close()
        }
    }

    private fun notFound(exchange: HttpExchange) {
        exchange.sendResponseHeaders(404, -1)
    }

    private fun contentType(fileName: String): String = when (fileName.substringAfterLast('.')) {
        "html" -> "text/html; charset=utf-8"
        "js", "mjs" -> "text/javascript; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "json", "map" -> "application/json; charset=utf-8"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "ico" -> "image/x-icon"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        "ttf" -> "font/ttf"
        "xml", "txt" -> "text/plain; charset=utf-8"
        else -> "application/octet-stream"
    }
}
