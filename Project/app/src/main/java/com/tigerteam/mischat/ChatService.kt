package com.tigerteam.mischat

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class ChatService : Service() {

    inner class ChatServiceBinder : Binder() {
        fun getService() : ChatService {
            return this@ChatService
        }
    }


    private val chatServiceBinder = ChatServiceBinder()
    private val TAG = "ChatService"


    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "ChatService onBind")
        return chatServiceBinder
    }

    override fun onCreate() {
        Log.i(TAG, "ChatService onCreate")
    }

    override fun onDestroy() {
        Log.i(TAG, "ChatService onDestroy")
    }


    //----------------------------------------------------------------------------------------------
    // API Methods
    //----------------------------------------------------------------------------------------------

    public fun getVersion() : String
    {
        return "1.0";
    }
}
