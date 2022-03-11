package com.zoho.vtouch.logging_agent

import android.content.res.AssetManager
import java.net.ServerSocket

actual class Server actual constructor(port: Int) {
    private val serverSocket: ServerSocket = ServerSocket(port)


    actual fun accept(): Socket {
        return Socket(serverSocket.accept())
    }


    actual fun close() {
        serverSocket.close()
    }

}

actual typealias Assets = AssetManager