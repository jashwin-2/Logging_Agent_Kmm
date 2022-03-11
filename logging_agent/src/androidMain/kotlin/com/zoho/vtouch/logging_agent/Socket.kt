package com.zoho.vtouch.logging_agent

import java.io.PrintStream
import java.util.*
import java.util.concurrent.ArrayBlockingQueue


actual class ConcurrentQueue<String> actual constructor(size: Int) {
    private val queue = ArrayBlockingQueue<String>(size)
    actual fun take(): String {
        return queue.take()
    }

    actual fun offer(string: String) {
        queue.offer(string)
    }

}

actual class Socket(private val platformSocket: java.net.Socket) {
    private val platformInputStream = platformSocket.getInputStream()
    private val platformOutputStream = platformSocket.getOutputStream()
    private val printStream = PrintStream(platformOutputStream)

    actual val inputStream = object : InputStream {
        override fun read(): String {
            val s = Scanner(platformInputStream, "UTF-8")
            return s.useDelimiter("\\r\\n\\r\\n").next()
        }

        override fun read(buffer: ByteArray): Int {
            return platformInputStream.read(buffer)
        }

    }


    actual fun close() {
        platformSocket.close()
    }

    actual val outputStream = object : OutputStream{
        override fun println(string: String) {
            printStream.apply {
                if (string.isBlank())
                    println()
                else
                    println(string)
            }
        }

        override fun write(byteArray: ByteArray) {
            printStream.write(byteArray)
        }

        override fun flush() {
            printStream.flush()
        }
    }
}