package com.tigerteam.mischat

import android.content.BroadcastReceiver
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.support.v7.app.AppCompatActivity
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
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.*
import android.view.View
import android.widget.AdapterView
import java.lang.reflect.Array.get
import java.net.Socket
import java.util.*


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    private var mManager : WifiP2pManager? = null
    private var mChannel : WifiP2pManager.Channel? = null
    private var mReceiver : BroadcastReceiver? = null
    private var mIntentFilter : IntentFilter? = null

    var peers = ArrayList<WifiP2pDevice>()
    var peersAdapater : ArrayAdapter<WifiP2pDevice>? = null
    var receivedMessages = ArrayList<String>()
    var receivedMessagesAdapater : ArrayAdapter<String>? = null

    var mContiniousWifiDiscoveryThread : Thread? = null
    var mContiniousWifiDiscoveryTask : ContiniousWifiDiscoveryTask? = null

    var mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg!!.what == Constants.HANDLER_CODE_NEW_CLIENT_MESSAGE){   // Selbst definierter Code
                var hBundle = Bundle(msg.data)
                var hNachricht = hBundle.getString("MessageKey")
                receivedMessagesAdapater!!.add(hNachricht)
            }
            else if (msg!!.what == Constants.HANDLER_CODE_CLIENT_ROLE_DETERMINED){
                LblClientOrServer.text = "I'm a client ;)"
            }
            else if (msg!!.what == Constants.HANDLER_CODE_SERVER_ROLE_DETERMINED){
                LblClientOrServer.text = "I'm the server :)"
            }
            else if (msg!!.what == Constants.HANDLER_CODE_REGEGISTER_RECEIVER){
                Log.d(TAG, "Code Received: HANDLER_CODE_REGEGISTER_RECEIVER")
                Thread.sleep(1000)
                unregisterReceiver(mReceiver)
                Thread.sleep(1000)
                registerReceiver(mReceiver, mIntentFilter)
            }

        }


    } // Um von Threads an den GUI Thread zu kommen


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        BtnDiscover.setOnClickListener { discover() }
        //BtnConnect.setOnClickListener{ connect() }


        // Listen Adapter und Verbindung mit List View
        peersAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                peers)
        ListViewPeers.adapter = peersAdapater

        receivedMessagesAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                receivedMessages)
        ListViewReceivedMessages.adapter = receivedMessagesAdapater




        // Register App with Wi-Fi P2P Framework and create Channel to connect the app with the Wi-Fi P2P Framework
        // Außerdem den BroadcastReceiver erstellen um von Änderungen zu erfahren
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager?.initialize(this, mainLooper, null)
        mReceiver = WiFiDirectBroadcastReceiver(mManager!!, mChannel!!, this, peerListListener, receivedMessagesAdapater!!)

        mIntentFilter = IntentFilter()
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)



        ListViewPeers.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                connect(position)
            }
        }
    }

    /* register the broadcast receiver with the intent values to be matched */
    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)

        // Start Peer-Discovery Thread
        mContiniousWifiDiscoveryTask = ContiniousWifiDiscoveryTask()
        mContiniousWifiDiscoveryThread = Thread(mContiniousWifiDiscoveryTask)
        mContiniousWifiDiscoveryThread!!.start()
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()

        // Stop Peer-Discovery
        mContiniousWifiDiscoveryTask!!.running = false
        unregisterReceiver(mReceiver)
    }



    fun discover() {
        // Ich glaube "beide" geräte müssen discovern
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
                Log.d(TAG, "Es sind neue Peers vorhanden")
                peersAdapater?.clear()
                peersAdapater?.addAll(refreshedPeers!!.toList())
            }

            if (peers.size == 0) {
                Log.d("MainActivity", "No peers found")

                // no peers found
            }
        }
    }

    fun connect(deviceIndex : Int) {
        Log.d(TAG, "Connect to all Peers")
        // Picking the first device found on the network.
        val device = peers[deviceIndex]

        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        if (device.status == 3 || device.status == 1 ) {
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

    fun setMessage(msg : String) {
        receivedMessagesAdapater!!.add(msg)
    }



    inner class ContiniousWifiDiscoveryTask() : Runnable {

        @Volatile
        var running = true

        override fun run() {
            while (running) {
                discover()

                Thread.sleep(60000)

                mManager!!.stopPeerDiscovery(mChannel, object: WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        //
                    }

                    override fun onFailure(reason: Int) {
                        //
                    }
                })
            }
        }
    }


}
