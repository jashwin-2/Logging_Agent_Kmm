package com.zoho.vtouch.logging_agent


import com.soywiz.korio.lang.toByteArray
import com.zoho.vtouch.logging_agent.Utils.toSHA1
import com.zoho.vtouch.logging_agent.model.JsonData
import com.zoho.vtouch.logging_agent.model.SessionDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.experimental.and
import kotlin.experimental.xor


class WebSocketHandler(
    var sessionDetails: SessionDetails,
    var callback: WebSocketCallback
) {
    private lateinit var socket: Socket
    private lateinit var socketInputStream: InputStream
    private lateinit var socketOutputStream: OutputStream

    var messages = ConcurrentQueue<String>(20)
    var isClientConnected = false


    fun handle(client: Socket) {
        try {
            socket = client
            socketInputStream = socket.inputStream
            socketOutputStream = socket.outputStream
            try {
                val data = socketInputStream.read()
                val pattern = Regex("^GET")
                if (pattern.containsMatchIn(data)) {
                    val pattern1 = Regex("Sec-WebSocket-Key: (.*)")
                    val match = pattern1.find(data)
                    val key =
                        toSHA1((match?.groupValues?.get(1)) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                    val response = """HTTP/1.1 101 Switching Protocols
Connection: Upgrade
Upgrade: websocket
Sec-WebSocket-Accept: $key

""".toByteArray()
                    socketOutputStream.write(response)
                    val initialData = JsonData(JsonData.INITIAL_DATA, sessionDetails, "")
                    socketOutputStream.write(encode(initialData.toJson()))
                    isClientConnected = true
                    activateWriter()
                    printInputStream()
                }
            } finally {
                isClientConnected = false
                callback.onError(Exception("Client Disconnected"))
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError(e)
        }
    }

    @Throws(Exception::class)
    private fun printInputStream() {
        var b = ByteArray(8000) //incoming buffer
        var message: ByteArray? = null //buffer to assemble message in
        val masks = ByteArray(4)
        var isSplit = false //has a message been split over a read
        var length = 0 //length of message
        var totalRead = 0 //total read in message so far
        while (true) {
            var len = 0 //length of bytes read from socket
            len = try {
                socketInputStream.read(b)
            } catch (e: Exception) {
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
                        rLength = (data and op)
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
                            message[totalRead] = (b[i] xor masks[totalRead % 4])
                            i++
                            totalRead++
                        }
                    } else {
                        i = 0
                        while (i < len && totalRead < length) {
                            message!![totalRead] = (b[i] xor masks[totalRead % 4])
                            i++
                            totalRead++
                        }
                        totalLength = i
                    }
                    if (totalRead < length) {
                        isSplit = true
                    } else {
                        isSplit = false
                        callback.onMessageReceived(message!!.decodeToString())
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
                        len -= totalLength
                    } else more = false
                } while (more)
            } else break
        }
    }

    @Throws(Exception::class)
    fun encode(message: String): ByteArray {
        val rawData = message.toByteArray()
        var frameCount = 0
        val frame = ByteArray(20)
        frame[0] = 129.toByte()
        if (rawData.size <= 125) {
            frame[1] = rawData.size.toByte()
            frameCount = 2
        } else if (rawData.size in 126..65535) {
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

    private fun activateWriter() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                while (isClientConnected) {
                    try {
                        socketOutputStream.write(encode(messages.take()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onError(e)
                    }
                }
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    fun offer(message: String) {
        messages.offer(message)
    }

    fun close() {
        socket.close()
    }

}