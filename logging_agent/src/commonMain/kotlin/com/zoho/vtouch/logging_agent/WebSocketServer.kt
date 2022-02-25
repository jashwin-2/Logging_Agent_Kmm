package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.GraphData
import com.zoho.vtouch.logging_agent.model.JsonData
import com.zoho.vtouch.logging_agent.model.LogMessage
import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

expect class WebSocketHandler(sessionDetails: SessionDetails, callback: WebSocketCallback){
    var isClientConnected:Boolean
    fun handle(client : PlatformSocket)
    fun encode(message: String): ByteArray
    fun offer(message: String)
    var callback : WebSocketCallback
    fun close()

}

class WebSocketServer(
    port: Int,
    val sessionDetails: SessionDetails,
    private val socketCallback: WebSocketCallback
) {
    private val mPort: Int = port
     var mRequestHandler: WebSocketHandler? = null
    private var isRunning = false
    private var web: Server? = null

    fun start() {
        isRunning = true
       CoroutineScope(Dispatchers.Default).launch {
           run()
       }
    }

    fun stop() {
        try {
            isRunning = false
            if (null != web) {
                mRequestHandler!!.isClientConnected = false
                web!!.close()
                web = null
            }
        } catch (e: Exception) {

        }
    }

     fun run() {
        try {
            web = Server(mPort)
            while (isRunning) {
                val socket = web!!.accept()
                mRequestHandler =
                    WebSocketHandler(sessionDetails, socketCallback)
                mRequestHandler!!.handle(socket)
                mRequestHandler!!.close()

            }
        } catch (e: Exception) {
            socketCallback.onError(e)
        }
    }

    fun sendStatsToClient(statistics: JsonData) {
        sendJsonToClient(statistics)
    }

    fun  sendJsonToClient(json : String){
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
                offer(json.ToJson())
            } else
                callback.onError(Exception("Client not connected"))
        }
    }

    fun sendLogMessage(logMessage: LogMessage, id: String) {

        JsonData(JsonData.LOG_MESSAGE, logMessage, id).also {
            sendJsonToClient(it)
        }

    }

    fun sendGraphData(list: MutableList<GraphData>) {
        JsonData(JsonData.GRAPH_DATA, list,"").also {
            sendJsonToClient(it)
        }
    }

    fun isClientConnected(): Boolean = mRequestHandler?.isClientConnected ?: false



}