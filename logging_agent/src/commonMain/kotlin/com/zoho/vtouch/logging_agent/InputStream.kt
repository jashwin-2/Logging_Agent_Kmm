package com.zoho.vtouch.logging_agent

interface InputStream {
    fun read() : String
    fun read(buffer: ByteArray) : Int
}