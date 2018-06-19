package com.tigerteam.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.CreateChatContact
import com.tigerteam.ui.helper.ChatOverviewRecyclerAdapter

import kotlinx.android.synthetic.main.activity_chat_overview.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_chat_overview.*
import kotlin.system.exitProcess

class ChatOverviewActivity : AppCompatActivity()
{


	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "ChatOverviewActivity"
	private val USER_NAME_REQUEST = 666
	private val CREATE_CHAT_REQUEST = 45

	private val chatServiceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(name: ComponentName?, service: IBinder?)
		{
			Log.i(TAG, "onServiceConnected")
			val binder = service as ChatService.ChatServiceBinder
			chatService = binder.getService()
			isChatServiceBound = true

			//----
			startFirstUseActivity()
		}

		override fun onServiceDisconnected(name: ComponentName)
		{
			chatService = null
			isChatServiceBound = false
		}
	}


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

	private var chatService: ChatService? = null
	private var isChatServiceBound = false


	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: RecyclerView.Adapter<*>
	private lateinit var viewManager: RecyclerView.LayoutManager

	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat_overview)
		setSupportActionBar(toolbar)




		//----
		val chatIntent = Intent(this, ChatService::class.java)

		// Keeps the service after unbind alive
		startService(chatIntent)

		// Bind to service to get the service API-Object
		bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)


		// Permissions holen
		var neededPermissions = mutableListOf<String>()

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.READ_PHONE_NUMBERS)
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.READ_CONTACTS)
		}


		if(neededPermissions.size > 0)
		{
			ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 22)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		if(requestCode == 22 && grantResults.any{it == PackageManager.PERMISSION_DENIED} )
		{
			Log.e(TAG, "Exit because not getting Permission")
			exitProcess(55);
		}
	}


	override fun onStart()
	{
		super.onStart()
	}

	override fun onResume()
	{
		super.onResume()
	}

	override fun onDestroy()
	{
		unbindService(chatServiceConnection)
		super.onDestroy()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		Log.d(TAG, "onActivityResult($requestCode, $resultCode)")

		if(requestCode == USER_NAME_REQUEST)
		{
			if(resultCode == RESULT_OK)
			{
				// User name was entered.
				val userName : String = data!!.extras.getString(Constants.EXTRA_USER_NAME)
				chatService?.firstUseCreateUser(userName)

				initCompleted()
			}
			else
			{
				// User clicked back button
				startFirstUseActivity()
			}
		}
		else if (requestCode == CREATE_CHAT_REQUEST)
		{
			if(resultCode == RESULT_OK)
			{
				val chatName = data!!.extras.getString(Constants.EXTRA_CHAT_NAME)
				val chatUsers = (data!!.extras.get(Constants.EXTRA_CHAT_USERS) as ArrayList<CreateChatContact>).toList()

				chatService?.createChat(chatName, chatUsers)

				showMyChats()
			}
		}

		super.onActivityResult(requestCode, resultCode, data)
	}


	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------
/*
	fun call_chat_service_button_clicked(view: View)
	{
		hello_world_text_view.text = chatService?.getVersion()
	}


	fun createChatButtonClicked(view : View){
		startCreateChatActivity();
	}*/




	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	fun startFirstUseActivity()
	{
		if(chatService!!.isFirstUse())
		{
			val intent = Intent(this, FirstUseActivity::class.java)
			startActivityForResult(intent, USER_NAME_REQUEST)
		}
		else
		{
			initCompleted()
		}
	}


	fun initCompleted()
	{
		Log.i(TAG, "initCompleted")

		fab.setOnClickListener { view ->
			startCreateChatActivity()
		}

		chatService!!.fillSomeTestData()

		// TODO TEST
		startChatActivity()

		showMyChats()
	}


	fun showMyChats(){
		val chatOverviewITems = chatService!!.getChatOverview()

		viewManager = LinearLayoutManager(this)

		viewAdapter = ChatOverviewRecyclerAdapter(chatOverviewITems)


		recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
			// use this setting to improve performance if you know that changes
			// in content do not change the layout size of the RecyclerView
			setHasFixedSize(true)

			// use a linear layout manager
			layoutManager = viewManager

			// specify an viewAdapter (see also next example)
			adapter = viewAdapter

			// vertical Dividing-Lines
			addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
		}
	}



	fun startCreateChatActivity() {
		val contacts = chatService!!.getContactsForCreatingChat()

		val intent = Intent(this, CreateChatActivity::class.java)
		intent.putExtra(Constants.EXTRA_CHAT_USERS, ArrayList(contacts))
		startActivityForResult(intent, CREATE_CHAT_REQUEST)
	}


	fun startChatActivity(){
		val intent = Intent(this, ChatActivity::class.java)
		startActivity(intent)
	}

}
