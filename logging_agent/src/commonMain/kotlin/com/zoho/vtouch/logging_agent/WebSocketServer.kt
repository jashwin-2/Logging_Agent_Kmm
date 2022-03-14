package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.GraphData
import com.zoho.vtouch.logging_agent.model.JsonData
import com.zoho.vtouch.logging_agent.model.LogMessage
import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.Synchronized


expect class JSONObject
expect class PlatformContext


class WebSocketServer(
    port: Int,
    private val sessionDetails: SessionDetails,
    private val socketCallback: WebSocketCallback
) {
    private val mPort: Int = port
    var mRequestHandler: WebSocketHandler? = null
    var isRunning = false
        private set
    private var webSocket: Server? = null

    fun start() {
        isRunning = true
        CoroutineScope(Dispatchers.Default).launch {
            run()
        }
    }

    fun stop() {
        try {
            isRunning = false
            if (null != webSocket) {
                mRequestHandler!!.isClientConnected = false
                webSocket!!.close()
                webSocket = null
            }
        } catch (e: Exception) {
            socketCallback.onError(e)
        }
    }

    private fun run() {
        try {
            webSocket = Server(mPort)
            while (isRunning) {
                val socket = webSocket!!.accept()
                mRequestHandler =
                    WebSocketHandler(sessionDetails, socketCallback)
                mRequestHandler!!.handle(socket)
                mRequestHandler!!.close()

            }
        } catch (e: Exception) {
            socketCallback.onError(e)
        }
    }

    fun sendStatsToClient(jsonObject: JSONObject, id: String) {
        val json = createJSON(jsonObject, id)
        sendJsonToClient(json)
    }

    fun sendStatsToClient(json: String, id: String) {
        val data = createJSON(json, id)
        sendJsonToClient(data)
    }

    fun sendJsonToClient(json: String) {
        mRequestHandler?.apply {
            if (isClientConnected) {
                offer(json)
            } else
                callback.onError(Exception("Client not connected"))
        }
    }


    fun sendJsonToClient(json: JsonData) {
        mRequestHandler?.apply {
            if (isClientConnected) {
                offer(json.toJson())
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendLogMessage(logMessage: LogMessage, id: String) {

        JsonData(JsonData.LOG_MESSAGE, logMessage, id).also {
            sendJsonToClient(it)
        }

    }

    fun sendGraphData(list: List<GraphData>) {
        JsonData(JsonData.GRAPH_DATA, list, "").also {
            sendJsonToClient(it)
        }
    }

    val isClientConnected: Boolean
        get() {
            return mRequestHandler?.isClientConnected ?: false
        }

}