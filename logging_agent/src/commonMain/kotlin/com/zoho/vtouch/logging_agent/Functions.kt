package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.JsonData

expect fun log(string: String) : Unit
expect fun JsonData.ToJson() : String