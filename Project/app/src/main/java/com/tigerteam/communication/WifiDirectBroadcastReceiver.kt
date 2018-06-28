package com.tigerteam.communication

import android.net.wifi.p2p.WifiP2pManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pInfo
import android.os.AsyncTask
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.widget.ArrayAdapter
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.MainActivity


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
class WiFiDirectBroadcastReceiver(private val mManager: WifiP2pManager, private val mChannel: WifiP2pManager.Channel,
                                  activity: MainActivity, peerListListener : WifiP2pManager.PeerListListener, val receivedMsgAdapater : ArrayAdapter<String>) : BroadcastReceiver() {
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
            if (mManager == null) {
                return
            }

            val netwinfo = intent.getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

            if (netwinfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, object : WifiP2pManager.ConnectionInfoListener{
                    override fun onConnectionInfoAvailable(info: WifiP2pInfo?) {
                        // InetAddress from WifiP2pInfo struct.
                        val groupOwnerAddress = info!!.groupOwnerAddress

                        // After the group negotiation, we can determine the group owner
                        // (server).
                        if (info.groupFormed && info.isGroupOwner) {
                            // Do whatever tasks are specific to the group owner.
                            // One common case is creating a group owner thread and accepting
                            // incoming connections.
                            Log.e("WifiBroadcastReceiver", "Starting the server thread")
                            var server : ServerTask = ServerTask(context, receivedMsgAdapater)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                                server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            else
                                server.execute()
                            Log.e("WifiBroadcastReceiver:", "Server is running")

                        } else if (info.groupFormed) {
                            // The other device acts as the peer (client). In this case,
                            // you'll want to create a peer thread that connects
                            // to the group owner.
                            Log.e("WifiBroadcastReceiver:", "Starting client thread")
                            var clientIp : String? = ClientClass.getLocalIpAddress()
                            val sender = ClientClass(groupOwnerAddress)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                sender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, clientIp)
                            } else {
                                sender.execute(clientIp)
                            }

                        }
                    }
                })
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            // Respond to this device's wifi state changing
        }
    }



}