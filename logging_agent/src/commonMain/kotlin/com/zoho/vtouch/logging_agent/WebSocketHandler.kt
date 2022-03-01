package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.SessionDetails

expect class WebSocketHandler(sessionDetails: SessionDetails, callback: WebSocketCallback){
    var isClientConnected:Boolean
    fun handle(client : Socket)
    fun encode(message: String): ByteArray
    fun offer(message: String)
    var callback : WebSocketCallback
    fun close()

}