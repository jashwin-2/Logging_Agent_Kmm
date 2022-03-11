package com.zoho.vtouch.logging_agent

interface OutputStream {
    fun println(string: String)
    fun write(byteArray: ByteArray)
    fun flush()
}