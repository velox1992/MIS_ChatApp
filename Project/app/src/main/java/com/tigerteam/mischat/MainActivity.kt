package com.tigerteam.mischat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var chatService: ChatService? = null
    private var isChatServiceBound = false

    private val chatServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChatService.ChatServiceBinder
            chatService = binder.getService()
            isChatServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            chatService = null
            isChatServiceBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //----
        val chatIntent = Intent(this, ChatService::class.java)

        // Keeps the service after unbind alive
        startService(chatIntent)

        // Bind to service to get the service API-Object
        bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)
    }

    fun call_chat_service_button_clicked(view: View) {
        hello_world_text_view.text = chatService?.getVersion()
    }
}
