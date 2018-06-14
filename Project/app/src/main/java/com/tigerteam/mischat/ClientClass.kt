package com.tigerteam.mischat

import android.os.AsyncTask
import android.util.Log
import java.io.DataOutputStream
import java.io.IOException
import java.net.*

class ClientClass(val serverAddress : InetAddress) : AsyncTask<String, Void, Void>() {

    override fun doInBackground(vararg params: String?): Void? {
        var socket : Socket
        try {
            socket = Socket(serverAddress,8888)
            var DOS : DataOutputStream = DataOutputStream(socket.getOutputStream())
            DOS.writeUTF("Hey Param ;)");

            socket.close()
            Log.e("ClientClass", "Client finished sending message: "+ params[0])
        }
        catch (e : IOException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        fun getLocalIpAddress(): String? {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }

            return null
        }
    }

}