package com.zoho.vtouch.logging_agent

expect class Assets
expect class RequestHandler(platformSocket: PlatformSocket, assets: Assets) {
fun handle() : Unit
fun close()
}