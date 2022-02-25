package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object LoggingAgent {
    private val DEFAULT_PORT = 8000
    private val WEB_SOCKET_PORT = 8001
    private var clientServer: ClientServer? = null
    private var websocketsServer: WebSocketServer? = null
    private var addresses = mutableListOf<String>()


    fun initialize(
        serverPort: Int,
        data: SessionDetails,
        socketCallback: WebSocketCallback,
        assets: com.zoho.vtouch.logging_agent.Assets
    ): WebSocketServer {


        val portNumber: Int = try {
            serverPort
        } catch (ex: NumberFormatException) {
            DEFAULT_PORT
        }
        clientServer = ClientServer(portNumber,assets)
        clientServer!!.start(socketCallback)

        websocketsServer = WebSocketServer(WEB_SOCKET_PORT, data, socketCallback)
        websocketsServer!!.start()
       addresses = NetworkUtils.getAddress(serverPort)
        return websocketsServer!!
    }


    fun shutDown() {
        if (clientServer != null) {
            clientServer!!.stop()
            clientServer = null
        }
        if (websocketsServer != null) {
            websocketsServer!!.stop()
            websocketsServer = null
        }
    }

    val isServerRunning: Boolean
        get() = clientServer != null && clientServer!!.isRunning



    fun getAddress(): String {
        return if (addresses.isNotEmpty()) {
            var string = "Server Addresses : \n"
            for (i in addresses)
                string += i
            string
        } else
            "Device not connected to a Private network Please turn on WIFI or Hotspot and launch the app"

    }

}