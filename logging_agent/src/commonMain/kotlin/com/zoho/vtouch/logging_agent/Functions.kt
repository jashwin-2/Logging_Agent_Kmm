package com.zoho.vtouch.logging_agent

expect fun loadContent(fileName: String?, assetManager: Assets): ByteArray?
expect fun createJSON(json : JSONObject, id :String) : String
expect fun createJSON(json : String,id : String):String