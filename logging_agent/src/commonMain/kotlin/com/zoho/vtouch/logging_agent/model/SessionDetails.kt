package com.zoho.vtouch.logging_agent.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionDetails(
    val tables: List<String> = listOf(),
    val logs: List<String> = listOf(),
    var graphs: List<String> = listOf(),
)