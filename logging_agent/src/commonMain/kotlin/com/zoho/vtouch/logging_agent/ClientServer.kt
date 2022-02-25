package com.zoho.vtouch.logging_agent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


expect class  Server(port: Int){
    fun accept() : PlatformSocket
    fun close() : Unit
}
expect class PlatformSocket

class ClientServer(port: Int,val assets: com.zoho.vtouch.logging_agent.Assets)  {
    private val mPort: Int = port
    private var socketCallBack : WebSocketCallback? = null
    private lateinit var mRequestHandler: RequestHandler

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
               mRequestHandler = RequestHandler(socket, assets)
               mRequestHandler.handle()

               mRequestHandler.close()
            }
        } catch (e: Exception) {
            socketCallBack?.onError(e)

        }
    }

}