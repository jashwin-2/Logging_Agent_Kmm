package com.zoho.vtouch.logging_agent

import com.soywiz.korio.lang.ISO_8859_1
import com.soywiz.korio.lang.toByteArray
import com.soywiz.krypto.sha1


object Utils {

     fun detectMimeType(fileName: String): String? {
        return when {
            fileName.isEmpty() -> {
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

     fun toSHA1(text: String): String {
        return text.toByteArray(ISO_8859_1).sha1().base64
    }
}