package com.zoho.vtouch.logging_agent

import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


actual object NetworkUtils{


    actual fun getAddress(port: Int): MutableList<String> {
        val list = mutableListOf<String>()

        try {
            val networkInterfaceEnumeration: Enumeration<NetworkInterface> =
                NetworkInterface.getNetworkInterfaces()
            while (networkInterfaceEnumeration.hasMoreElements()) {
                for (interfaceAddress in networkInterfaceEnumeration.nextElement()
                    .interfaceAddresses)
                    if (interfaceAddress.address.isSiteLocalAddress)
                        list.add( "http://${interfaceAddress.address.hostAddress}:$port\n")
            }
        } catch (e: SocketException) {
            e.printStackTrace()

        }
        return list
    }
}