package com.zoho.vtouch.logging_agent

import android.content.res.AssetManager


expect object Utils {

    fun detectMimeType(fileName: String): String?

    fun loadContent(fileName: String?, assetManager: AssetManager): ByteArray?


    fun getCurrentTime(): String

}