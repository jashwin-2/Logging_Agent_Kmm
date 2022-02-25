package com.zoho.vtouch.logging_agent


import android.util.Base64
import android.util.Log
import com.zoho.vtouch.logging_agent.model.JsonData
import com.zoho.vtouch.logging_agent.model.SessionDetails
import com.google.gson.Gson

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.regex.Pattern
import kotlin.experimental.and
import kotlin.experimental.xor


actual class WebSocketHandler actual constructor(var sessionDetails: SessionDetails, actual var callback: WebSocketCallback)
     {
      private  lateinit var platformSocket : PlatformSocket
    private var out: OutputStream? = null
    var messages = ArrayBlockingQueue<String>(20)
    actual var isClientConnected = false


    actual fun handle(client: PlatformSocket) {
        try {
            platformSocket = client
            val input = client.getInputStream()
            out = client.getOutputStream()
            val s = Scanner(input, "UTF-8")
            try {
                val data = s.useDelimiter("\\r\\n\\r\\n").next()
                val get = Pattern.compile("^GET").matcher(data)
                if (get.find()) {
                    val match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data)
                    match.find()
                    val key = SHA1(match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                    val response = """HTTP/1.1 101 Switching Protocols
Connection: Upgrade
Upgrade: websocket
Sec-WebSocket-Accept: $key

""".toByteArray(StandardCharsets.UTF_8)
                    out?.write(response, 0, response.size)
                    val initialData = JsonData(JsonData.INITIAL_DATA, sessionDetails, "")
                    out?.write(encode(Gson().toJson(initialData)))
                    isClientConnected = true
                    activateWriter()
                    printInputStream(input)
                }
            } finally {
                Log.d("WebSocket", "Closed")
                isClientConnected = false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callback.onError(e)
        } finally {
            Log.d("WebSocket", "Closed")
        }
    }

    @Throws(IOException::class)
    private fun printInputStream(inputStream: InputStream) {
        var b = ByteArray(8000) //incoming buffer
        var message: ByteArray? = null //buffer to assemble message in
        val masks = ByteArray(4)
        var isSplit = false //has a message been split over a read
        var length = 0 //length of message
        var totalRead = 0 //total read in message so far
        while (true) {
            var len = 0 //length of bytes read from socket
            len = try {
                inputStream.read(b)
            } catch (e: IOException) {
                break
            }
            if (len != -1) {
                var more = false
                var totalLength = 0
                do {
                    var j = 0
                    var i = 0
                    if (!isSplit) {
                        var rLength: Byte = 0
                        var rMaskIndex = 2
                        var rDataStart = 0
                        // b[0] assuming text
                        val data = b[1]
                        val op = 127.toByte()
                        rLength = (data and op) as Byte
                        length = rLength.toInt()
                        if (rLength == 126.toByte()) {
                            rMaskIndex = 4
                            length = b[2].toInt() and 0xff shl 8
                            length += b[3].toInt() and 0xff
                        } else if (rLength == 127.toByte()) rMaskIndex = 10
                        i = rMaskIndex
                        while (i < rMaskIndex + 4) {
                            masks[j] = b[i]
                            j++
                            i++
                        }
                        rDataStart = rMaskIndex + 4
                        message = ByteArray(length)
                        totalLength = length + rDataStart
                        i = rDataStart
                        totalRead = 0
                        while (i < len && i < totalLength) {
                            message[totalRead] = (b[i] xor masks[totalRead % 4]) as Byte
                            i++
                            totalRead++
                        }
                    } else {
                        i = 0
                        while (i < len && totalRead < length) {
                            message!![totalRead] = (b[i] xor masks[totalRead % 4]) as Byte
                            i++
                            totalRead++
                        }
                        totalLength = i
                    }
                    if (totalRead < length) {
                        isSplit = true
                    } else {
                        isSplit = false
                        callback.onMessageReceived(String(message!!))
                        b = ByteArray(8000)
                    }
                    if (totalLength < len) {
                        more = true
                        i = totalLength
                        j = 0
                        while (i < len) {
                            b[j] = b[i]
                            i++
                            j++
                        }
                        len = len - totalLength
                    } else more = false
                } while (more)
            } else break
        }
    }

    @Throws(IOException::class)
    actual fun encode(message: String): ByteArray {
        val rawData = message.toByteArray()
        var frameCount = 0
        val frame = ByteArray(20)
        frame[0] = 129.toByte()
        if (rawData.size <= 125) {
            frame[1] = rawData.size.toByte()
            frameCount = 2
        } else if (rawData.size >= 126 && rawData.size <= 65535) {
            frame[1] = 126.toByte()
            val len = rawData.size
            frame[2] = (len shr 8 and 255.toByte().toInt()).toByte()
            frame[3] = (len and 255.toByte().toInt()).toByte()
            frameCount = 4
        } else {
            frame[1] = 127.toByte()
            val len = rawData.size.toLong() //note an int is not big enough in java
            frame[2] = (len shr 56 and 255.toByte().toLong()).toByte()
            frame[3] = (len shr 48 and 255.toByte().toLong()).toByte()
            frame[4] = (len shr 40 and 255.toByte().toLong()).toByte()
            frame[5] = (len shr 32 and 255.toByte().toLong()).toByte()
            frame[6] = (len shr 24 and 255.toByte().toLong()).toByte()
            frame[7] = (len shr 16 and 255.toByte().toLong()).toByte()
            frame[8] = (len shr 8 and 255.toByte().toLong()).toByte()
            frame[9] = (len and 255.toByte().toLong()).toByte()
            frameCount = 10
        }
        val bLength = frameCount + rawData.size
        val reply = ByteArray(bLength)
        var bLim = 0
        for (i in 0 until frameCount) {
            reply[bLim] = frame[i]
            bLim++
        }
        for (i in rawData.indices) {
            reply[bLim] = rawData[i]
            bLim++
        }
        return reply
    }

    fun activateWriter() {
        Thread {
            try {
                while (isClientConnected) {
                    try {
                        out!!.write(encode(messages.take()))
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        callback.onError(e)
                    }
                }
            } catch (e: Exception) {
                callback.onError(e)
            }
        }.start()
    }

    companion object {
        private fun SHA1(text: String): String? {
            return try {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(text.toByteArray(StandardCharsets.ISO_8859_1), 0, text.length)
                val sha1hash = md.digest()
                Base64.encodeToString(sha1hash, Base64.NO_WRAP)
            } catch (ex: Exception) {
                null
            }
        }
    }



    actual fun offer(message: String) {
        messages.offer(message)
    }

    actual fun close() {
        platformSocket.close()
    }


}