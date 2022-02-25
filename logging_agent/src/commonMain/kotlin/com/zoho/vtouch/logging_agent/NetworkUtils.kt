package com.zoho.vtouch.logging_agent


expect object NetworkUtils {
    fun getAddress(port: Int): MutableList<String>
}