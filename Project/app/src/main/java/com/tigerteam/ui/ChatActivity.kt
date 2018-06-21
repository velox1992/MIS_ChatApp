package com.tigerteam.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.helper.ChatRecyclerAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val TAG = "ChatActivity"

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var chatItems : List<ChatItem>
    private lateinit var ownUserId : String
    private lateinit var chatId : String


    private var chatService: ChatService? = null
    private var isChatServiceBound = false


    private val chatServiceConnection = object : ServiceConnection
    {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?)
        {
            Log.i(TAG, "onServiceConnected")
            val binder = service as ChatService.ChatServiceBinder
            chatService = binder.getService()
            isChatServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName)
        {
            chatService = null
            isChatServiceBound = false
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Bind to service to get the service API-Object
        val chatIntent = Intent(this, ChatService::class.java)
        bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)


        val extras = intent.extras

        if(extras == null)
        {
            Log.e(TAG, "Error: Started without Extras in Intent! ")
            return
        }

        val obj = extras.get(Constants.EXTRA_CHAT_ITEMS)
        if(obj == null)
        {
            Log.e(TAG, "Error: Missing Data in Extras (obj)! ")
        }

        ownUserId = extras.getString(Constants.EXTRA_OWN_USER_ID)
        if(ownUserId == null)
        {
            Log.e(TAG, "Error: Missing Data in Extras (ownUserId)! ")
        }

        chatId = extras.getString(Constants.EXTRA_CHAT_ID)
        if(chatId == null)
        {
            Log.e(TAG, "Error: Missing Data in Extras (chatId)! ")
        }

        val title = extras.getString(Constants.EXTRA_CHAT_TITLE)
        if(title != null) {
            this.title = title
        }

        /*
        val demoData = mutableListOf<ChatItem>()
        demoData.add(ChatItem("456", "Peter", "String", "Du Nudel!", Date()))
        demoData.add(ChatItem("12", "Ich", "String", "NEIN!", Date()))
        demoData.add(ChatItem("456", "Klaus", "String", "Haha lol!", Date()))
        demoData.add(ChatItem("12", "Ich", "String", "Selber Handtuch!", Date()))
        */

        chatItems = (obj as ArrayList<ChatItem>).toList()


        viewManager = LinearLayoutManager(this)
        //Scroll zum letzten
        (viewManager as LinearLayoutManager).stackFromEnd = true

        viewAdapter = ChatRecyclerAdapter(chatItems, ownUserId)

        val context = this

        recyclerView = findViewById<RecyclerView>(R.id.reyclerview_message_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            // vertical Dividing-Lines
            //addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }


    override fun onDestroy()
    {
        unbindService(chatServiceConnection)
        super.onDestroy()
    }



    fun sendMessageButtonClick(view : View) {
        val message = edittext_chatbox.text.toString()

        if(!message.isNullOrBlank()) {
            if (isChatServiceBound) {
                chatService!!.sendChatTextMessage(chatId, message)
                edittext_chatbox.text.clear()
            } else {
                Log.e(TAG, "sendMessageButtonClick -> ChatService is not bound")
                Toast.makeText(this, "ChatService nicht erreichbar!", Toast.LENGTH_LONG).show()
            }
        }
    }

}
