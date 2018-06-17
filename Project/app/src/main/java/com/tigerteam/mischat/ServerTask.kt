package com.tigerteam.mischat

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.system.Os.accept
import android.widget.ArrayAdapter
import java.io.*
import java.net.ServerSocket
import java.net.Socket


class ServerTask(val context : Context, val receivedMsgAdapater : ArrayAdapter<String>) : AsyncTask<Void, Void, Void?>() {

    val TAG = "ServerTask"
    var serverSocket : ServerSocket? = null
    var clientWriterList = ArrayList<PrintWriter>()

    override fun doInBackground(vararg params: Void?): Void? {
        try {

            runServer()

            listenToClients()

            closeClients()
            stopServer()

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

    fun listenToClients() {
        // Immer auf Client-Verbindungen warten
        while (true) {
            val client = serverSocket!!.accept()
            Log.e(TAG, "Ein neuer Client hat sich verbunden")

            // Merken des Output Streams um später noch Nachrichten an die Clients zu senden
            clientWriterList.add(PrintWriter(client.getOutputStream()))

            // Behandlung der Client-Kommunikation in eigenem Thread
            var clientHandlerTask : ClientHandlerTask = ClientHandlerTask(client)
            clientHandlerTask.run()
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                clientHandlerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            else
                clientHandlerTask.execute()
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
                    // Die empfangene Nachricht
                    sendToAllClients(hNachricht)
                }
            }
            return
        }


        fun sendToAllClients(_message : String){
            for (clientWriter in clientWriterList) {
                clientWriter.print(_message)
                clientWriter.flush()
            }
        }
    }
}

