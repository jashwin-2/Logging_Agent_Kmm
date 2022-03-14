package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object LoggingAgent {
    private const val DEFAULT_SERVER_PORT = 8000
    private const val WEB_SOCKET_PORT = 8001
    const val USED_MEMORY = "Used memory in MB"
    const val AVAILABLE_MEMORY = "Available Heap size in MB"

    private var clientServer: ClientServer? = null
    private var websocketsServer: WebSocketServer? = null
    private var addresses = mutableListOf<String>()



    fun initialize(
        serverPort: Int,
        sessionDetails: SessionDetails,
        socketCallback: WebSocketCallback,
        context: PlatformContext
    ): WebSocketServer? {
        val portNumber: Int = if (serverPort == WEB_SOCKET_PORT) DEFAULT_SERVER_PORT else serverPort
        clientServer = ClientServer(portNumber, context)
        clientServer?.start(socketCallback)
        addMemoryGraphs(sessionDetails)
        websocketsServer = WebSocketServer(WEB_SOCKET_PORT, sessionDetails, socketCallback)
        websocketsServer?.start()
        addresses = NetworkUtils.getAddress(serverPort)
        sendMemoryStats(websocketsServer,context)
        return websocketsServer
    }


    private fun addMemoryGraphs(sessionDetails: SessionDetails){
        val graphs = mutableListOf(USED_MEMORY, AVAILABLE_MEMORY)
        graphs.addAll(sessionDetails.graphs)
        sessionDetails.graphs = graphs
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