package com.zoho.vtouch.logging_agent
expect fun loadContent(fileName: String?, context: PlatformContext): ByteArray?
expect fun createJSON(json : JSONObject, id :String) : String
expect fun createJSON(json : String,id : String):String
expect fun getApplicationIcon(context : PlatformContext):ByteArray?
expect fun sendMemoryStats(webSocket: WebSocketServer?,context: PlatformContext)
