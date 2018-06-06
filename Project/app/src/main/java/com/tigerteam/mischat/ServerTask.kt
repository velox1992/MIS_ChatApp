package com.tigerteam.mischat

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import java.io.IOException
import android.system.Os.accept
import android.widget.ArrayAdapter
import java.io.DataInputStream
import java.net.ServerSocket


class ServerTask(val context : Context, val receivedMsgAdapater : ArrayAdapter<String>) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void?): String? {
        try {
            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            val serverSocket = ServerSocket(8888)
            val client = serverSocket.accept()
            /**
             * If this code is reached, a client has connected and transferred data
             */
            Log.e("ServerTask ", "Client has connected")
            val DIS = DataInputStream(client.getInputStream())
            val msg_received = DIS.readUTF()
            client.close()
            serverSocket.close()
            Log.e("ServerTask","Server has received the message: $msg_received")
            return msg_received

        } catch (e : IOException) {
            Log.e("ServerTask", e.message)
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        Log.e("ServerTask", result)
        receivedMsgAdapater.add(result)
        super.onPostExecute(result)
    }
}