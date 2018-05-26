package com.tigerteam.mischat

import android.net.wifi.p2p.WifiP2pManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(private val mManager: WifiP2pManager, private val mChannel: WifiP2pManager.Channel,
                                  activity: MainActivity, peerListListener : WifiP2pManager.PeerListListener) : BroadcastReceiver() {
    private var mActivity: MainActivity
    private var myPeerListListener : WifiP2pManager.PeerListListener

    init {
        this.mActivity = activity
        this.myPeerListListener = peerListListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action


        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener)
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            // Respond to this device's wifi state changing
        }
    }
}