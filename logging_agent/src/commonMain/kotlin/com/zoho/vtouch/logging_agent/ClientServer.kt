package com.zoho.vtouch.logging_agent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


expect class Socket{
    val inputStream : InputStream
    val outputStream : OutputStream
    fun close()
}


expect class ConcurrentQueue<String>(size : Int){
    fun take() : String
    fun offer(string: String)
}

expect class Assets

class ClientServer(port: Int, private val assets: Assets) {
    private val mPort: Int = port
    private var socketCallBack: WebSocketCallback? = null
    private lateinit var requestHandler: RequestHandler

    var isRunning = false
    private var mServer: Server? = null

    fun start(onError: WebSocketCallback) {
        isRunning = true
        this.socketCallBack = onError
        CoroutineScope(Dispatchers.Default).launch {
            startTheServer()
        }
    }

    fun stop() {
        try {
            isRunning = false
            if (null != mServer) {
                mServer!!.close()
                mServer = null
            }
        } catch (e: Exception) {
            socketCallBack?.onError(e)
        }
    }

    private fun startTheServer() {
        try {
            mServer = Server(mPort)
            while (isRunning) {
                val socket = mServer!!.accept()
                requestHandler = RequestHandler(socket, assets)
                requestHandler.handle()
                requestHandler.close()
            }
        } catch (e: Exception) {
            socketCallBack?.onError(e)
        }
    }

}