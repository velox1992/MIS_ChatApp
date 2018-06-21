package com.tigerteam.mischat

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import android.system.Os.accept
import android.widget.ArrayAdapter
import java.io.*
import java.net.ServerSocket
import java.net.Socket


class ServerTask(val context : Context, val receivedMsgAdapater : ArrayAdapter<String>, val mUIHandler: Handler) : AsyncTask<Void, Void, Void?>() {

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

    fun listenToClients() {
        // Immer auf Client-Verbindungen warten
        while (true) {
            val client = serverSocket!!.accept()
            Log.e(TAG, "Ein neuer Client hat sich verbunden")

            // Merken des Output Streams um später noch Nachrichten an die Clients zu senden
            clientWriterList.add(DataOutputStream(client.getOutputStream()))
            clients.add(client)

            // Behandlung der Client-Kommunikation in eigenem Thread
            var clientHandlerThread = Thread(ClientHandlerTask(client, mUIHandler))
            clientHandlerThread.start()
            while (!clientHandlerThread.isAlive) {

            }
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                clientHandlerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            else
                clientHandlerTask.execute()
                */
        }
    }



    inner class ClientHandlerTask(val client : Socket, val mUIHandler: Handler) : Runnable {

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

                    sendMessageToUi(hNachricht)

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

        fun sendMessageToUi(_message : String){
            // This is a worker thread, so we cannot access any UI objects directly. But since we are
            // using a handler object and this handler is running inside the main loop, it will be able to access those UI objects with no problem.
            var hHandlerMessage = mUIHandler.obtainMessage()
            hHandlerMessage.what = Constants.HANDLER_CODE_NEW_CLIENT_MESSAGE    // Message-Code: kann selbst definiert werden
            var hBundle = Bundle()
            hBundle.putString("MessageKey", _message)
            hHandlerMessage.data = hBundle
            mUIHandler.sendMessage(hHandlerMessage)
        }
    }
}

