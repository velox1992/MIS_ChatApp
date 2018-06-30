package com.tigerteam.mischat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiP2pBroadcastReceiver(
	var wifiP2pManager : WifiP2pManager,
	var wifiP2pChannel : WifiP2pManager.Channel,
	var chatService: ChatService)
	:
	BroadcastReceiver(), WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	val TAG = "WifiP2pBrdcstReceiver"

	var ipExchangeServerThread : Thread? = null
	var ipExchangeClientThread : Thread? = null


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onReceive(context: Context?, intent: Intent?)
	{
		Log.d(TAG, "Intent Action=${intent!!.action}")

		when(intent!!.action)
		{
			WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> WIFI_P2P_CONNECTION_CHANGED_ACTION(context, intent)
			WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> WIFI_P2P_DISCOVERY_CHANGED_ACTION(context, intent)
			WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> WIFI_P2P_PEERS_CHANGED_ACTION(context, intent)
			WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> WIFI_P2P_STATE_CHANGED_ACTION(context, intent)
			WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION  -> WIFI_P2P_THIS_DEVICE_CHANGED_ACTION(context, intent)
		}
	}

	override fun onPeersAvailable(peers: WifiP2pDeviceList?)
	{
		for(device in peers!!.getDeviceList() as Iterable<WifiP2pDevice>)
		{
			Log.d(TAG, device.toString())
		}
		/*
		Log.d(TAG, "onPeersAvailable");
        // Out with the old, in with the new.
        mPeers.clear();
        mPeers.addAll(peerList.getDeviceList());
        mDevicesAdapter.notifyDataSetChanged();

        Log.d(TAG, "----------------------------------");
        for (WifiP2pDevice device : mPeers){
            Log.d(TAG, "Device found: "+ device.deviceName +" ("+ device.deviceAddress +")");
        }

        if (mPeers.size() == 0) {
            Log.d(TAG, "No devices found");
            showToast(R.string.msg_no_devices_found);
        }
		 */
	}

	override fun onConnectionInfoAvailable(info: WifiP2pInfo?)
	{
		/*
		Log.d(TAG, "onConnectionInfoAvailable");
        // InetAddress from WifiP2pInfo struct.
        String groupOwnerAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        Log.d(TAG, "address: " + groupOwnerAddress);

        showToast(R.string.msg_connection_available);
        // After the group negotiation, we can determine the group owner.
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
            Log.d(TAG, "I AM THE OWNER");
            Intent it = new Intent(this, ChatService.class);
            it.putExtra(ChatService.EXTRA_SERVER_CLIENT, ChatService.TYPE_SERVER);
            startService(it);

        } else if (wifiP2pInfo.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
            Log.d(TAG, "I AM ON THE GROUP");
            Intent it = new Intent(this, ChatService.class);
            it.putExtra(ChatService.EXTRA_SERVER_CLIENT, ChatService.TYPE_CLIENT);
            it.putExtra(ChatService.EXTRA_IP_ADDRESS, groupOwnerAddress);
            startService(it);
        }
		 */
	}


	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	//
	// Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
	// One extra EXTRA_WIFI_P2P_INFO provides the p2p connection info in the form of a WifiP2pInfo
	// object. Another extra EXTRA_NETWORK_INFO provides the network info in the form of a
	// NetworkInfo. A third extra provides the details of the group.
	//
	// See also: EXTRA_WIFI_P2P_INFO, EXTRA_NETWORK_INFO, EXTRA_WIFI_P2P_GROUP
	//
	private fun WIFI_P2P_CONNECTION_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		var wifiP2pInfo : WifiP2pInfo? = null
		var wifiP2pGroup : WifiP2pGroup? = null
		var networkInfo : NetworkInfo? = null

		wifiP2pInfo = intent!!.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)
		Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION: WifiP2pInfo=${wifiP2pInfo}")

		wifiP2pGroup = intent!!.getParcelableExtra<WifiP2pGroup>(WifiP2pManager.EXTRA_WIFI_P2P_GROUP)
		Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION: WifiP2pGroup=${wifiP2pGroup}")

		networkInfo = intent!!.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
		Log.d(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION: NetworkInfo=${networkInfo}")

		if(wifiP2pInfo.isGroupOwner)
		{
			if(ipExchangeClientThread != null)
			{
				ipExchangeClientThread!!.interrupt()
				ipExchangeClientThread = null
			}

			if(ipExchangeServerThread == null)
			{
				ipExchangeServerThread = Thread(IPExchangeServer(chatService))
				ipExchangeServerThread!!.start()
			}
		}
		else
		{
			if(ipExchangeServerThread != null)
			{
				ipExchangeServerThread!!.interrupt()
				ipExchangeServerThread = null
			}

			if(ipExchangeClientThread == null)
			{
				ipExchangeClientThread = Thread(IPExchangeClient(chatService, wifiP2pInfo.groupOwnerAddress))
				ipExchangeClientThread !!.start()
			}
		}
	}

	//
	// Broadcast intent action indicating that peer discovery has either started or stopped.
	// One extra EXTRA_DISCOVERY_STATE indicates whether discovery has started or stopped.
	//
	// Note that discovery will be stopped during a connection setup. If the application tries
	// to re-initiate discovery during this time, it can fail.
	//
	private fun WIFI_P2P_DISCOVERY_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		// The lookup key for an int that indicates whether p2p discovery has started or stopped.

		var discoveryState : Int = intent!!.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1)
		when(discoveryState)
		{
			WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED -> Log.d(TAG, "Wi-Fi p2p discovery has started.")
			WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED ->
			{
				Log.d(TAG, "Wi-Fi p2p discovery has stopped.")
				Thread({
					Log.d(TAG, "Wi-Fi p2p discovery has stopped, so we start the next discovery.")
					wifiP2pManager!!.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener
					{
						override fun onSuccess()
						{
							Log.d(TAG, "WifiP2pManager.discoverPeers() => OnSuccess")
						}

						override fun onFailure(reasonCode: Int)
						{
							Log.d(TAG, "WifiP2pManager.discoverPeers() => onFailure($reasonCode)")
						}
					})
				}).start()
			}
		}
	}

	//
	// Broadcast intent action indicating that the available peer list has changed. This can
	// be sent as a result of peers being found, lost or updated.
	//
	// An extra EXTRA_P2P_DEVICE_LIST provides the full list of current peers. The full list of
	// peers can also be obtained any time with
	// requestPeers(WifiP2pManager.Channel, WifiP2pManager.PeerListListener).
	//
	// See also: EXTRA_P2P_DEVICE_LIST
	//
	private fun WIFI_P2P_PEERS_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		var wifiP2pDeviceList : WifiP2pDeviceList? = null

		wifiP2pDeviceList = intent!!.getParcelableExtra<WifiP2pDeviceList>(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
		for(device in wifiP2pDeviceList.deviceList)
		{
			Log.d(TAG, device.toString())
		}

		Thread({
			for(device in wifiP2pDeviceList.deviceList)
			{
				if(device.status == WifiP2pDevice.CONNECTED)
				{
					Log.d(TAG, "Already connected to device: ${device.deviceName}.")
					continue
				}

				if(device.deviceName.startsWith("[TV] Samsung 5 Series", true))
				{
					Log.d(TAG, "We do not want to connect to a TV device: ${device.deviceName}.")
					continue
				}

				Log.d(TAG, "Try to connect to device: ${device.deviceName}.")

				val wifiP2pConfig : WifiP2pConfig = WifiP2pConfig()
				wifiP2pConfig.deviceAddress = device.deviceAddress
				wifiP2pConfig.wps.setup = WpsInfo.PBC;

				wifiP2pManager.connect(wifiP2pChannel, wifiP2pConfig, object : WifiP2pManager.ActionListener
                {
                    override fun onSuccess()
                    {
                        Log.d(TAG, "WifiP2pManager.connect -> onSuccess().")
                    }

                    override fun onFailure(reason: Int)
                    {
                        Log.d(TAG, "WifiP2pManager.connect -> onFailure(${reason}.")
                    }
                })
			}
		}).start()
	}

	//
	// Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
	// An extra EXTRA_WIFI_STATE provides the state information as int.
	//
	// See also:EXTRA_WIFI_STATE
	//
	private fun WIFI_P2P_STATE_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		// The lookup key for an int that indicates whether Wi-Fi p2p is enabled or disabled.

		var wifiState : Int = intent!!.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
		when(wifiState)
		{
			WifiP2pManager.WIFI_P2P_STATE_ENABLED -> Log.d(TAG, "Wi-Fi p2p is now enabled.")
			WifiP2pManager.WIFI_P2P_STATE_DISABLED -> Log.d(TAG, "Wi-Fi p2p is now disabled.")
		}
	}

	//
	// Broadcast intent action indicating that this device details have changed.
	//
	private fun WIFI_P2P_THIS_DEVICE_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		var thisDevice : WifiP2pDevice ? = null

		thisDevice = intent!!.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
		Log.d(TAG, "This device=$thisDevice")
	}




	inner class IPExchangeServer(val chatService: ChatService) : Runnable
	{
		val TAG = "IPExchangeServer"

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
					Log.d(TAG, "${socket}")

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

	inner class IPExchangeClient(
			val chatService: ChatService, val serverAddress : InetAddress) : Runnable
	{
		val TAG = "IPExchangeClient"

		var socket : Socket? = null

		override fun run()
		{
			Log.d(TAG, "Start of IPExchangeClient.run().")
			while(true)
			{
				try
				{
					socket = Socket()
					socket!!.connect(InetSocketAddress(serverAddress, 8022), 0)
					//Log.d(TAG, "${socket}")

					var ois = ObjectInputStream(socket!!.getInputStream())
					var peerAddresses = ois.readObject() as List<InetAddress>

					//Log.d(TAG, "${peerAddresses}")

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

				Thread.sleep(3000)
			}
			Log.d(TAG, "End of IPExchangeClient.run().")
		}
	}
}