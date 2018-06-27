package com.tigerteam.sync

import android.util.Log
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.Socket
import java.util.*

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
			try
			{
				if(peerQueue.isEmpty())
				{
					peerQueue = chatService.getPeers()
					if(peerQueue.isEmpty())
					{
						Thread.sleep(500)
						continue
					}
				}
				var address = peerQueue.poll()

				Log.d(TAG, "Creating Socket (${address.hostName}, ${address.hostAddress})")
				socket = Socket(address, Constants.SYNC_SERVER_SOCKET_PORT)

				processUsers(socket!!)



				Log.d(TAG, "Requesting Chat.")
				oosRequest!!.writeObject(DataRequest(DataRequestType.Chat))

				Log.d(TAG, "Receiving Chat.")
				var chats = oisResponse!!.readObject() as MutableList<Chat>

				Log.d(TAG, "Requesting Quit.")
				oosRequest!!.writeObject(DataRequest(DataRequestType.Quit))
				Thread.sleep(100)
			}
			catch(e: Exception)
			{
				Log.e(TAG, "Client.run() => ${e.toString()} : ${e.message}")
				Thread.sleep(1000)
			}
			finally
			{
				oisResponse?.close()
				oisResponse = null

				oosRequest?.close()
				oosRequest = null

				Log.d(TAG, "Closing Socket.")
				socket?.close()
				socket = null
			}
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

		// TODO: Process new User
	}
}