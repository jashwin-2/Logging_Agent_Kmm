package com.zoho.vtouch.logging_agent

expect class  Server(port: Int){
    fun accept() : Socket
    fun close() : Unit
}