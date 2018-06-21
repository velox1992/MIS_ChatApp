package com.tigerteam.ui

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.helper.ChatRecyclerAdapter
import java.util.*
import kotlinx.android.synthetic.main.activity_chat.*
import android.widget.Toast
import com.tigerteam.intent.UpdateUIIntent


class ChatActivity : AppCompatActivity()
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "ChatActivity"


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

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

			//----
			updateViewAdapter()
		}

		override fun onServiceDisconnected(name: ComponentName)
		{
			Log.i(TAG, "onServiceDisconnected")
			chatService = null
			isChatServiceBound = false
		}
	}

	private val broadcastReceiver = object : BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent)
		{
			Log.d(TAG, "onReceive")

			if(intent is UpdateUIIntent)
			{
				updateViewAdapter()
			}
		}
	}
	private val intentFilter : IntentFilter = IntentFilter()

	private lateinit var chatId : String

	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: ChatRecyclerAdapter


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat)

		// Bind to service to get the service API-Object
		val chatIntent = Intent(this, ChatService::class.java)
		bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)

		//----
		intentFilter.addAction(UpdateUIIntent().action)

		//----
		val extras = intent.extras
		val ownUserId = extras.getString(Constants.EXTRA_OWN_USER_ID)
		this.title = extras.getString(Constants.EXTRA_CHAT_TITLE)
		this.chatId = extras.getString(Constants.EXTRA_CHAT_ID)

		//----
		val viewManager = LinearLayoutManager(this)
		viewAdapter = ChatRecyclerAdapter(ownUserId)
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

	override fun onResume()
	{
		super.onResume()

		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
	}

	override fun onPause()
	{
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

		super.onPause()
	}


	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------

	fun button_chatbox_send_clicked(view : View)
	{
		val message : String = edittext_chatbox.text.toString()

		chatService!!.sendMessage(chatId, message)
		edittext_chatbox.setText("")
	}


	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	fun updateViewAdapter()
	{
		val chatItems = chatService!!.getChatItems(chatId)

		(viewAdapter as ChatRecyclerAdapter).updateData(chatItems)
		viewAdapter.notifyDataSetChanged()
	}
}
