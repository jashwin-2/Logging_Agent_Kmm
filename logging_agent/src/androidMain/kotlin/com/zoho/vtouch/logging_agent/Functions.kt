package com.zoho.vtouch.logging_agent

import com.google.gson.Gson
import com.zoho.vtouch.logging_agent.model.JsonData
import org.json.JSONObject

actual fun JsonData.toJson() : String{
    return if (json is JSONObject) {
        val obj = JSONObject()
        obj.put("type", type)
        obj.put("id", id)
        obj.put("json", json)
        obj.toString()
    } else {
        val gson = Gson()
        gson.toJson(this)
    }
}