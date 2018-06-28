package com.tigerteam.communication

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.tigerteam.mischat.Constants
import java.io.*
import java.net.*

class ClientClass(val serverAddress : InetAddress) : AsyncTask<String, Void, Void>() {

    val TAG = "ClientClass"
    var client : Socket? = null
    //var reader : BufferedReader? = null
    //var writer : PrintWriter? = null
    var writer : DataOutputStream? = null

    var clientActive = true
    var id = 0

    override fun doInBackground(vararg params: String?): Void? {
        try {
            connectToServer()

            // Behandlung der Server-Kommunikation in eigenem Thread
            /*
            var serverHandlerThread = Thread(ServerListenerTask(client!!))
            serverHandlerThread.start()
            */

            // Sende eine Nachricht an den Server damit dieser die Adresse des Clients erfährt.
            sendMessageToServer()
            /*
            while (clientActive) {
                sendMessageToServer()
            }
            */

            // Output Stream wird hier nicht mehr gebraucht
            writer!!.close()

        }
        catch (e : IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun connectToServer() {
        client = Socket(serverAddress,8888)
        //writer = PrintWriter(client!!.getOutputStream())
        writer = DataOutputStream(client!!.getOutputStream())
        //reader = BufferedReader(InputStreamReader(client!!.getInputStream()))


        Log.e(TAG, "Netzwerkverbindung hergestellt!")
    }

    fun sendMessageToServer() {
        writer!!.writeUTF(android.os.Build.MODEL + ": Hey, hier bin ich\n")
        writer!!.flush()
        //id++
        //Thread.sleep(2000)
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

    inner class ServerListenerTask(val client : Socket) : Runnable {

        val TAG = "ServerListenerTask"

        var reader : BufferedReader? = null

        init {
            reader = BufferedReader(InputStreamReader(client.getInputStream()))
        }

        var running = true

        override fun run() {
            try {
                var hNachricht : String
                var hStreamFinished = false
                //while(!hStreamFinished) {
                while(running) {
                    if (reader == null) {
                        running = false
                        break
                    }
                    else {
                        hNachricht = reader!!.readLine()
                        if (hNachricht == null) {
                            hStreamFinished = true

                        }
                        else {
                            Log.d(TAG, "Nachricht vom Server erhalten: " + hNachricht)
                        }
                    }

                }
                Log.d(TAG, "Stream zu ende")
                return
            }
            catch (e : Exception) {
                e.printStackTrace()
            }
            finally {
                Log.d(TAG, "Exception aufgetreten")
                clientActive = false
            }

        }
    }

}





