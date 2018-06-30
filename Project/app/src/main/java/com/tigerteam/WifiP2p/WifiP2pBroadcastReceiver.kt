package com.tigerteam.WifiP2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.util.Log
import com.tigerteam.mischat.ChatService

class WifiP2pBroadcastReceiver(var chatService: ChatService) : BroadcastReceiver()
{
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	val TAG = "WifiP2pBrdcstReceiver"


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

		chatService.wifiP2pConnectionChanged(wifiP2pInfo, wifiP2pGroup, networkInfo)
	}

	//
	// Broadcast intent action indicating that peer discovery has either started or stopped.
	// One extra EXTRA_DISCOVERY_STATE indicates whether discovery has started or stopped.
	//
	// Note that discovery will be stopped during a connection setup. If the application tries
	// to re-initiate discovery during this time, it can fail.
	//
	// The lookup key for an int that indicates whether p2p discovery has started or stopped.
	//
	private fun WIFI_P2P_DISCOVERY_CHANGED_ACTION(context: Context?, intent: Intent?)
	{
		var discoveryState : Int = intent!!.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1)
		chatService.wifiP2pDiscoveryChanged(discoveryState)
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
		chatService.wifiP2pPeersChanged(wifiP2pDeviceList)
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
}