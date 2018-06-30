package com.tigerteam.WifiP2p

import android.util.Log
import com.tigerteam.mischat.ChatService
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

class IPExchangeServer(val chatService: ChatService) : Runnable
{
	val TAG : String = "IPExchangeServer"
	val LOG : Boolean = false

	var serverSocket : ServerSocket? = null
	var socket : Socket? = null

	override fun run()
	{
		Log.d(TAG, "Start of IPExchangeServer.run().")
		while(true)
		{
			try
			{
				serverSocket = ServerSocket(8022);
				serverSocket!!.reuseAddress = true
				serverSocket!!.soTimeout = 0

				socket = serverSocket!!.accept()

				if(LOG) Log.d(TAG, "${socket}")

				// Add this own and the client address
				chatService.addPeerAddress(socket!!.localAddress)
				chatService.addPeerAddress(socket!!.inetAddress)

				// Send the peer ip addresses to the client
				var oosResponse = ObjectOutputStream(socket!!.getOutputStream())
				oosResponse.writeObject(chatService.getPeerAddresses(false))

				// Wait until the client gets the data
				Thread.sleep(1000)
			}
			catch(e : InterruptedException)
			{
				// https://docs.oracle.com/javase/tutorial/essential/concurrency/interrupt.html
				Log.e(TAG, "${e.toString()} : ${e.message}")
				break
			}
			catch(e : Exception)
			{
				Log.e(TAG, "${e.toString()} : ${e.message}")
			}
			finally
			{
				socket?.close()
				socket = null

				serverSocket?.close()
				serverSocket = null
			}
		}
		Log.d(TAG, "End of IPExchangeServer.run().")
	}
}