package com.zoho.vtouch.logging_agent.model

import kotlinx.serialization.Serializable

@Serializable
data class GraphData(val id:String, val value: Float, val timestamp: Long)

