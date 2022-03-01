package com.zoho.vtouch.logging_agent


expect class RequestHandler(socket: Socket, assets: Assets) {
fun handle() : Unit
fun close()
}