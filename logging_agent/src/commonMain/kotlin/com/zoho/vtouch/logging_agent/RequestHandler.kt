package com.zoho.vtouch.logging_agent


class RequestHandler(private val socket: Socket, var assets: Assets) {
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

        if (route == null || route.isEmpty()) {
            route = "home.html"
        }
        val bytes: ByteArray = loadContent(route, assets) ?: return
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