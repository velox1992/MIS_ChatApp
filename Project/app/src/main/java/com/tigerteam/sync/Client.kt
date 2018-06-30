package com.tigerteam.sync

import android.util.Log
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket
import java.util.*

//
// Android Client-Server Using Sockets – Client Implementation
// http://androidsrc.net/android-client-server-using-sockets-client-implementation/
//
class Client(val chatService : ChatService) : Runnable
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	val TAG : String = "Sync.Client"


	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	var peerQueue : Queue<InetAddress> = ArrayDeque<InetAddress>()
	var socket : Socket? = null
	var oosRequest : ObjectOutputStream? = null
	var oisResponse : ObjectInputStream? = null


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun run()
	{
		while(true)
		{
			var address : InetAddress? = null
			try
			{
				if(peerQueue.isEmpty())
				{
					peerQueue.addAll(chatService.getPeerAddresses(true))
					if(peerQueue.isEmpty())
					{
						Thread.sleep(randomTime(500, 1500).toLong())
						continue
					}
				}
				address = peerQueue.poll()

				Log.d(TAG, "Creating Socket (${address.hostName}, ${address.hostAddress})")
				socket = Socket(address, Constants.SYNC_SERVER_SOCKET_PORT)
				socket!!.soTimeout = Constants.SYNC_CLIENT_SOCKET_TIMEOUT

				processUsers(socket!!)
				processChats(socket!!)
				processChatUsers(socket!!)
				processMessages(socket!!)

				Log.d(TAG, "Requesting Quit.")
				oosRequest!!.writeObject(DataRequest(DataRequestType.Quit))
				Thread.sleep(500)
			}
			catch(e : ConnectException)
			{
				Log.e(TAG, "${e.toString()} : ${e.message}")
				if(address != null)
					chatService.removePeerAddress(address)
			}
			catch(e: Exception)
			{
				Log.e(TAG, "Client.run() => ${e.toString()} : ${e.message}")
				Thread.sleep(randomTime(500, 1500).toLong())
			}
			finally
			{
				oisResponse = null
				oosRequest = null

				Log.d(TAG, "Closing Socket.")
				socket?.close()
				socket = null
			}

			Thread.sleep(2500)
		}
	}


	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	private fun processUsers(socket : Socket)
	{
		Log.d(TAG, "Requesting User.")
		if(oosRequest == null)
			oosRequest = ObjectOutputStream(socket.getOutputStream())
		oosRequest!!.writeObject(DataRequest(DataRequestType.User))

		Log.d(TAG, "Receiving User.")
		if(oisResponse == null)
			oisResponse = ObjectInputStream(socket!!.getInputStream())
		var users = oisResponse!!.readObject() as MutableList<User>

		if(users.isEmpty())
			return

		var dbUsers = mutableListOf<com.tigerteam.database.DbObjects.User>()
		for(user in users)
		{
			dbUsers.add(com.tigerteam.database.DbObjects.User(user))
		}
		chatService.addUsers(dbUsers)
	}

	private fun processChats(socket : Socket)
	{
		Log.d(TAG, "Requesting Chat.")
		if(oosRequest == null)
			oosRequest = ObjectOutputStream(socket.getOutputStream())
		oosRequest!!.writeObject(DataRequest(DataRequestType.Chat))

		Log.d(TAG, "Receiving Chat.")
		if(oisResponse == null)
			oisResponse = ObjectInputStream(socket!!.getInputStream())
		var chats = oisResponse!!.readObject() as MutableList<Chat>

		if(chats.isEmpty())
			return

		var dbChats = mutableListOf<com.tigerteam.database.DbObjects.Chat>()
		for(chat in chats)
		{
			dbChats.add(com.tigerteam.database.DbObjects.Chat(chat))
		}
		chatService.addChats(dbChats)
	}

	private fun processChatUsers(socket : Socket)
	{
		Log.d(TAG, "Requesting ChatUsers.")
		if(oosRequest == null)
			oosRequest = ObjectOutputStream(socket.getOutputStream())
		oosRequest!!.writeObject(DataRequest(DataRequestType.ChatUser))

		Log.d(TAG, "Receiving ChatUsers.")
		if(oisResponse == null)
			oisResponse = ObjectInputStream(socket!!.getInputStream())
		var chatUsers = oisResponse!!.readObject() as MutableList<ChatUser>

		if(chatUsers.isEmpty())
			return

		var dbChatUsers = mutableListOf<com.tigerteam.database.DbObjects.ChatUser>()
		for(chatUser in chatUsers)
		{
			dbChatUsers.add(com.tigerteam.database.DbObjects.ChatUser(chatUser))
		}
		chatService.addChatUsers(dbChatUsers)
	}

	private fun processMessages(socket : Socket)
	{
		Log.d(TAG, "Requesting Messsages.")
		if(oosRequest == null)
			oosRequest = ObjectOutputStream(socket.getOutputStream())
		oosRequest!!.writeObject(DataRequest(DataRequestType.Messages))

		Log.d(TAG, "Receiving Messages.")
		if(oisResponse == null)
			oisResponse = ObjectInputStream(socket!!.getInputStream())
		var chatMessages = oisResponse!!.readObject() as MutableList<ChatMessage>

		if(chatMessages.isEmpty())
			return

		var dbChatMessages = mutableListOf<com.tigerteam.database.DbObjects.ChatMessage>()
		for(chatMessage in chatMessages)
		{
			dbChatMessages.add(com.tigerteam.database.DbObjects.ChatMessage(chatMessage))
		}
		chatService.addChatMessages(dbChatMessages)
	}

	private fun randomTime(range_begin : Int, range_end : Int) : Int
	{
		return Random().nextInt((range_end + 1) - range_begin) +  range_begin
	}
}