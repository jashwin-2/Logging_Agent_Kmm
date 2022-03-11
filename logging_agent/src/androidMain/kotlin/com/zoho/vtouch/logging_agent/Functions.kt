package com.zoho.vtouch.logging_agent

import com.zoho.vtouch.logging_agent.model.JsonData.Companion.TABLE_DATA
import java.io.ByteArrayOutputStream
import java.io.InputStream

actual typealias JSONObject =  org.json.JSONObject

actual fun loadContent(fileName: String?, assetManager: Assets): ByteArray? {
    var input: InputStream? = null
    return try {
        val output = ByteArrayOutputStream()
        input = assetManager.open(fileName!!)
        val buffer = ByteArray(1024)
        var size: Int
        while (-1 != input.read(buffer).also { size = it }) {
            output.write(buffer, 0, size)
        }
        output.flush()
        output.toByteArray()
    } catch (e: Exception) {
        null
    } finally {
        try {
            input?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun createJSON(json: JSONObject, id: String): String {
    val obj = JSONObject()
    obj.put("type", TABLE_DATA)
    obj.put("json", json)
    obj.put("id", id)
    return obj.toString()
}

actual fun createJSON(json : String,id : String):String = createJSON(JSONObject(json),id)