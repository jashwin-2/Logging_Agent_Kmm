package com.zoho.vtouch.logging_agent.model

import kotlinx.serialization.Serializable

@Serializable
data class LogMessage(
    val logLevel: String,
    val logMessage: String,
    val timeStamp: Long
) {
    companion object {
        const val INFO = "Info"
        const val WARN = "Warning"
        const val ERROR = "Error"
    }
}