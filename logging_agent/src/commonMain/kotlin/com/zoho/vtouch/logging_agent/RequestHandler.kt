package com.zoho.vtouch.logging_agent


class RequestHandler(private val socket: Socket,private var context: PlatformContext) {
    fun handle() {
        var route: String? = null
        val inputStream = socket.inputStream
        val outPutStream = socket.outputStream
        val data = inputStream.read()
        var line: String
        while ((data.also { line = it }).isNotEmpty()) {
            if (line.startsWith("GET /")) {
                val start = line.indexOf('/') + 1
                val end = line.indexOf(' ', start)
                route = line.substring(start, end)
                break
            }
        }
        var bytes: ByteArray? = null

        if (route == null || route.isEmpty()) {
            route = "home.html"
        } else if (route == "appIcon.ico") {
            bytes = getApplicationIcon(context)
            if (bytes == null)
                route = "defaultIcon.ico"
        }

        if (bytes == null)
            bytes = loadContent(route, context) ?: return

        outPutStream.apply {
            println("HTTP/1.0 200 OK")
            println("Content-Type: " + Utils.detectMimeType(route))
            println("Content-Length: " + bytes.size)
            println("")
            write(bytes)
            flush()
        }


    }

    fun close() {
        socket.close()
    }
}