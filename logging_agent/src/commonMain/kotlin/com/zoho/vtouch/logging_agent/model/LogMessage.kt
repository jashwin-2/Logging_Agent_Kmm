package com.zoho.vtouch.logging_agent.model

data class LogMessage(
    val logLevel: String,
    val logMessage: String,
    private val time: String = com.zoho.vtouch.logging_agent.Utils.getCurrentTime()
) {
    companion object {
        const val INFO = "Info"
        const val WARN = "Warning"
        const val ERROR = "Error"
    }
}