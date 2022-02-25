package com.zoho.vtouch.logging_agent.model

data class SessionDetails(
    val tables: List<String> = listOf(),
    val logs: List<String> = listOf(),
    val graphs: List<String> = listOf(),
)