package com.tigerteam.mischat

import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context.WIFI_P2P_SERVICE
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.util.Log
import android.widget.Adapter
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private var mManager : WifiP2pManager? = null
    private var mChannel : WifiP2pManager.Channel? = null
    private var mReceiver : BroadcastReceiver? = null
    private var mIntentFilter : IntentFilter? = null

    var peers = ArrayList<WifiP2pDevice>()
    var peersAdapater : ArrayAdapter<WifiP2pDevice>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        BtnDiscover.setOnClickListener { discover() }
        BtnConnect.setOnClickListener{ connect() }

        // Listen Adapter
        peersAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                peers)
        ListViewPeers.adapter = peersAdapater

        // Register App with Wi-Fi P2P Framework and create Channel to connect the app with the Wi-Fi P2P Framework
        // Außerdem den BroadcastReceiver erstellen um von Änderungen zu erfahren
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager?.initialize(this, mainLooper, null)
        mReceiver = WiFiDirectBroadcastReceiver(mManager!!, mChannel!!, this, peerListListener)

        mIntentFilter = IntentFilter()
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

    }

    /* register the broadcast receiver with the intent values to be matched */
    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(mReceiver)
    }



    fun discover() {
        mManager?.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("MainActivity", "Discover Peers onSuccess")
            }

            override fun onFailure(reason: Int) {
                Log.d("MainActivity", "Discover Peers onFailure")
            }
        })


    }

    var peerListListener = object : WifiP2pManager.PeerListListener{
        override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
            val refreshedPeers = peerList?.getDeviceList()

            if (refreshedPeers != peers) {
                peersAdapater?.clear()
                peersAdapater?.addAll(refreshedPeers!!.toList())

            }

            if (peers.size == 0) {
                Log.d("MainActivity", "No peers found")

                // no peers found
            }
        }
    }

    fun connect() {
        // Picking the first device found on the network.
        val device = peers[0]

        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        mManager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // WiFiDirectBroadcastReceiver notifies zus. Ignore for now
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(this@MainActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show()
            }


        })
    }

}
