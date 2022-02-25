package com.zoho.vtouch.logging_agent

interface WebSocketCallback {
    fun onError(ex: Exception)
    fun onMessageReceived(message: String?)
}