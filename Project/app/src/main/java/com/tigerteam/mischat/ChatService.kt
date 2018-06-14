package com.tigerteam.mischat

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*
import com.tigerteam.database.*
import com.tigerteam.database.DbObjects.*

class ChatService : Service()
{
	inner class ChatServiceBinder : Binder()
	{
		fun getService() : ChatService
		{
			return this@ChatService
		}
	}

	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "ChatService"
	private val chatServiceBinder = ChatServiceBinder()


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

	private var db : IChatDbHelper = ChatDbHelper(this)


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onBind(intent: Intent): IBinder?
	{
		Log.i(TAG, "ChatService onBind")
		return chatServiceBinder
	}

	override fun onCreate()
	{
		Log.i(TAG, "ChatService onCreate")
	}

	override fun onDestroy()
	{
		Log.i(TAG, "ChatService onDestroy")
	}


	//----------------------------------------------------------------------------------------------
	// API Methods
	//----------------------------------------------------------------------------------------------

	public fun getVersion() : String
	{
		return "1.0";
	}

	//
	// First use of the application?
	//
	public fun isFirstUse() : Boolean
	{
		val ownUserId = db.getParameter(Constants.PARAM_OWN_USER_ID)

		if(ownUserId == null)
		{
			Log.d(TAG, "isFirstUse() : true")
			return true
		}
		else
		{
			Log.d(TAG, "isFirstUse() : false")
			return false
		}
	}

	//
	// Create a user account for the first time use.
	//
	public fun firstUseCreateUser(nickName : String)
	{
		val uuidNewUser = UUID.randomUUID().toString()
		val newUser = User(uuidNewUser, nickName)
		val ownUserId = Parameter(Constants.PARAM_OWN_USER_ID, "String", uuidNewUser)

		try
		{
			Log.d(TAG, "firstUseCreateUser: UserId=${newUser.id}, UserName=${newUser.name}")

			db.upsertUser(newUser)
			db.upsertParameter(ownUserId)
		}
		catch (e: Exception)
		{
			Log.e(TAG, "firstUseCreateUser($nickName) => " + e.toString())

		}
	}
}
