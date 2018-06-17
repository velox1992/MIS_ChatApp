package com.tigerteam.mischat

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import java.io.*
import java.net.*

class ClientClass(val serverAddress : InetAddress) : AsyncTask<String, Void, Void>() {

    val TAG = "ClientClass"
    var client : Socket? = null
    var reader : BufferedReader? = null
    var writer : PrintWriter? = null

    var clientActive = true
    var id = 0

    override fun doInBackground(vararg params: String?): Void? {
        try {
            connectToServer()

            // Behandlung der Server-Kommunikation in eigenem Thread
            var serverHandlerTask : ClientClass.ServerListenerTask = ServerListenerTask(client!!)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                serverHandlerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            else
                serverHandlerTask.execute()



            while (clientActive) {
                sendMessageToServer()
            }

        }
        catch (e : IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun connectToServer() {
        client = Socket(serverAddress,8888)
        reader = BufferedReader(InputStreamReader(client!!.getInputStream()))
        writer = PrintWriter(client!!.getOutputStream())

        Log.e(TAG, "Netzwerkverbindung hergestellt!")
    }

    fun sendMessageToServer() {
        writer!!.println("Eine Nachricht" + id + "\n")
        writer!!.flush()
        id++
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

    inner class ServerListenerTask(val client : Socket) : AsyncTask<Void, Void, Void?>() {

        val TAG = "ServerListenerTask"

        var reader : BufferedReader? = null

        init {
            reader = BufferedReader(InputStreamReader(client.getInputStream()))
        }

        override fun doInBackground(vararg params: Void?): Void? {
            var hNachricht : String

            var hStreamFinished = false
            while(!hStreamFinished) {
                hNachricht = reader!!.readLine()
                if (hNachricht == null) {
                    hStreamFinished = true

                }
                else {
                    Log.d(TAG, "Nachricht erhalten: " + hNachricht)
                }
            }
            Log.d(TAG, "Stream zu ende")
            return null
        }


    }

}




