package com.zoho.vtouch.logging_agent

import android.content.res.AssetManager
import android.text.TextUtils
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


actual object Utils {

    actual fun detectMimeType(fileName: String): String? {
        return when {
            TextUtils.isEmpty(fileName) -> {
                null
            }
            fileName.endsWith(".html") -> {
                "text/html"
            }
            fileName.endsWith(".js") -> {
                "application/javascript"
            }
            fileName.endsWith(".css") -> {
                "text/css"
            }
            else -> {
                "application/octet-stream"
            }
        }
    }

    actual fun loadContent(fileName: String?, assetManager: AssetManager): ByteArray? {
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
        } catch (e: FileNotFoundException) {
            null
        } finally {
            try {
                input?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    actual fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return sdf.format(Date())
    }

}