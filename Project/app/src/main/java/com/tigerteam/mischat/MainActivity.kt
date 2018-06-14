package com.tigerteam.mischat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.View
import com.tigerteam.ui.FirstUseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "MainActivity"
    private val USER_NAME_REQUEST = 666

	private val chatServiceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(name: ComponentName?, service: IBinder?)
		{
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


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
    {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		//----
		val chatIntent = Intent(this, ChatService::class.java)

		// Keeps the service after unbind alive
		startService(chatIntent)

		// Bind to service to get the service API-Object
		bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)
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
			}
		    else
			{
				// User clicked back button
				startFirstUseActivity()
			}
	    }

        super.onActivityResult(requestCode, resultCode, data)
    }


	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------

	fun call_chat_service_button_clicked(view: View)
	{
		hello_world_text_view.text = chatService?.getVersion()
	}


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
	}
}
