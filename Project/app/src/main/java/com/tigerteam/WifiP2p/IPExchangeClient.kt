package com.tigerteam.WifiP2p

import android.util.Log
import com.tigerteam.mischat.ChatService
import java.io.ObjectInputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class IPExchangeClient(val chatService: ChatService, val groupOwnerAddress : InetAddress) : Runnable
{
	val TAG : String = "IPExchangeClient"
	val LOG : Boolean = false

	var socket : Socket? = null

	override fun run()
	{
		Log.d(TAG, "Start of IPExchangeClient.run().")
		while(true)
		{
			try
			{
				socket = Socket()
				socket!!.connect(InetSocketAddress(groupOwnerAddress, 8022), 0)

				if(LOG) Log.d(TAG, "${socket}")

				var ois = ObjectInputStream(socket!!.getInputStream())
				var peerAddresses = ois.readObject() as List<InetAddress>

				if(LOG) Log.d(TAG, "${peerAddresses}")

				chatService.setPeerAddresses(peerAddresses, socket!!.localAddress)
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
			}

			try
			{
				Thread.sleep(3000)
			}
			catch(e : InterruptedException)
			{
				break
			}
		}
		Log.d(TAG, "End of IPExchangeClient.run().")
	}
}