package com.tigerteam.communication

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket


class ServerTask(val context : Context, val mChatService : ChatService) : AsyncTask<Void, Void, Void?>() {

    val TAG = "ServerTask"
    var serverSocket : ServerSocket? = null
    var clientWriterList = ArrayList<DataOutputStream>()
    var clients = ArrayList<Socket>()

    override fun doInBackground(vararg params: Void?): Void? {
        try {

            runServer()

            listenToClients()

            //closeClients()
            //stopServer()

            Log.e(TAG, "Der Server wurde beendet")
            return null

        } catch (e : IOException) {
            Log.e("ServerTask", e.message)
        }
        return null
    }

    fun runServer() {
        serverSocket = ServerSocket(8888)

        Log.d(TAG, "Server started")
    }

    fun stopServer() {
        serverSocket!!.close()
    }

    fun closeClients() {
        for (clientWriter in clientWriterList) {
            clientWriter.close()
        }
    }

    /**
     * In einer Endlossschleife wird auf neue Clientverbinden gewartet. Die Kommunikation mit den Clients erfolgt dann in einem eigenen Thread
     */
    fun listenToClients() {
        var hPeerAddresses : MutableList<InetAddress> = arrayListOf()

        // Immer auf Client-Verbindungen warten.
        while (true) {
            val client = serverSocket!!.accept()
            Log.d(TAG, "Ein neuer Client hat sich verbunden")


            /*
            // Merken des Output Streams um später noch Nachrichten an die Clients zu senden
            clientWriterList.add(DataOutputStream(client.getOutputStream()))
            if (!clients.contains(client)) {
                clients.add(client)
            }
            */

            // ToDo Es kann sein dass noch alte Clients (welche nicht mehr verfügbar sind) noch in der Liste sind
            // ToDo Die Meldung rausgeben, dass sich ein neuer Client verfügbar ist.
            if (!hPeerAddresses.contains(client.inetAddress)) {
                hPeerAddresses.add(client.inetAddress)
                mChatService.setPeers(hPeerAddresses)
            }






            // Behandlung der Client-Kommunikation in eigenem Thread
            /*
            var clientHandlerThread = Thread(ClientHandlerTask(client))
            clientHandlerThread.start()
            */

        }
    }



    inner class ClientHandlerTask(val client : Socket) : Runnable {

        val TAG = "ClientHandlerTask"

        var reader : BufferedReader? = null

        init {
            reader = BufferedReader(InputStreamReader(client.getInputStream()))
        }

        override fun run() {
            var hNachricht : String

            var hStreamFinished = false
            while(!hStreamFinished) {
                hNachricht = reader!!.readLine()
                if (hNachricht == null) {
                    hStreamFinished = true

                }
                else {
                    Log.d(TAG, "Nachricht erhalten: " + hNachricht)

                    // Die empfangene Nachricht wieder an alle Clients verteilen
                    sendToAllClients(hNachricht)
                }
            }
            return
        }


        var id = 0
        fun sendToAllClients(_message : String){
            for (client in clients) {
                var writer = DataOutputStream(client!!.getOutputStream())
                writer!!.writeUTF("[Server]: " + _message + "\n")
                writer!!.flush()

                id++
            }

        }
    }
}

