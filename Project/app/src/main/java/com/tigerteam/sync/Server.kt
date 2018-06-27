package com.tigerteam.sync

import android.util.Log
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class Server(val chatService : ChatService) : Runnable
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	val TAG : String = "Sync.Server"


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

	var serverSocket : ServerSocket? = null
	var socket : Socket? = null
	var oisRequest : ObjectInputStream? = null
	var oosResponse : ObjectOutputStream? = null


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun run()
	{
		while(true)
		{
			try
			{
				Log.d(TAG, "Creating ServerSocket.")
				serverSocket = ServerSocket(Constants.SYNC_SERVER_SOCKET_PORT)

				Log.d(TAG, "Wait for client.")
				socket = serverSocket!!.accept()

				while(receiveRequest(socket!!)) { }
			}
			catch(e: Exception)
			{
				Log.e(TAG, "Server.run() => ${e.toString()} : ${e.message}")
			}
			finally
			{
				oosResponse?.close()
				oosResponse = null

				oisRequest?.close()
				oisRequest = null

				Log.d(TAG, "Closing Socket.")
				socket?.close()
				socket = null

				Log.d(TAG, "Closing ServerSocket.")
				serverSocket?.close()
				serverSocket = null
			}
		}
	}


	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	private fun receiveRequest(socket : Socket) : Boolean
	{
		var result = true

		if(oisRequest == null)
			oisRequest = ObjectInputStream(socket.getInputStream())

		Log.d(TAG, "Waiting for DataRequest.")
		var dataRequest = oisRequest!!.readObject() as DataRequest

		when(dataRequest.request)
		{
			DataRequestType.User -> sendUser(socket)
			DataRequestType.Chat -> sendChat(socket)
			DataRequestType.ChatUser -> sendChatUser(socket)
			DataRequestType.Messages -> sendMessages(socket)
			else -> result = false
		}

		return result
	}

	private fun sendUser(socket : Socket)
	{
		if(oosResponse == null)
			oosResponse = ObjectOutputStream(socket.getOutputStream())

		var users = mutableListOf<User>()
		for(user : com.tigerteam.database.DbObjects.User in chatService.getAllUsers())
			users.add(User(user))

		Log.d(TAG, "Sending Users(${users.size}).")
		oosResponse!!.writeObject(users)
	}

	private fun sendChat(socket : Socket)
	{
		if(oosResponse == null)
			oosResponse = ObjectOutputStream(socket.getOutputStream())

		var chats = mutableListOf<Chat>()
		for(chat : com.tigerteam.database.DbObjects.Chat in chatService.getAllChats())
			chats.add(Chat(chat))

		Log.d(TAG, "Sending Chats(${chats.size}).")
		oosResponse?.writeObject(chats)
	}

	private fun sendChatUser(socket : Socket)
	{
		if(oosResponse == null)
			oosResponse = ObjectOutputStream(socket.getOutputStream())

		var chatUsers = mutableListOf<ChatUser>()
		for(chatUser : com.tigerteam.database.DbObjects.ChatUser in chatService.getAllChatUsers())
			chatUsers.add(ChatUser(chatUser))

		Log.d(TAG, "Sending ChatUsers(${chatUsers.size}).")
		oosResponse?.writeObject(chatUsers)
	}

	private fun sendMessages(socket : Socket)
	{
		if(oosResponse == null)
			oosResponse = ObjectOutputStream(socket.getOutputStream())

		var chatMessages = mutableListOf<ChatMessage>()
		for(chatMessage : com.tigerteam.database.DbObjects.ChatMessage in chatService.getAllChatMessages())
			chatMessages.add(ChatMessage(chatMessage))

		Log.d(TAG, "Sending ChatMessages(${chatMessages.size}).")
		oosResponse?.writeObject(chatMessages)
	}
}