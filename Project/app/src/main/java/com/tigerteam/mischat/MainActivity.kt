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
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import java.lang.reflect.Array.get
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mManager : WifiP2pManager? = null
    private var mChannel : WifiP2pManager.Channel? = null
    private var mReceiver : BroadcastReceiver? = null
    private var mIntentFilter : IntentFilter? = null

    var peers = ArrayList<WifiP2pDevice>()
    var peersAdapater : ArrayAdapter<WifiP2pDevice>? = null
    var receivedMessages = ArrayList<String>()

    //var services = ArrayList<WifiP2pDevice>()
    //var servicesAdapater : ArrayAdapter<WifiP2pDevice>? = null
    var services = ArrayList<String>()
    var servicesAdapater : ArrayAdapter<String>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        BtnDiscover.setOnClickListener { discover() }
        BtnConnect.setOnClickListener{ connect() }
        BtnPublishService.setOnClickListener{ publishService() }
        BtnDiscoverServices.setOnClickListener{ detectServices() }
        BtnAddMessage.setOnClickListener{ addMessageToRecord() }

        // Listen Adapter und Verbindung mit List View
        peersAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                peers)
        ListViewPeers.adapter = peersAdapater

        var receivedMessagesAdapater : ArrayAdapter<String> = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                receivedMessages)
        ListViewReceivedMessages.adapter = receivedMessagesAdapater

        servicesAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                services)
        ListViewServices.adapter = servicesAdapater


        // Register App with Wi-Fi P2P Framework and create Channel to connect the app with the Wi-Fi P2P Framework
        // Außerdem den BroadcastReceiver erstellen um von Änderungen zu erfahren
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager?.initialize(this, mainLooper, null)
        mReceiver = WiFiDirectBroadcastReceiver(mManager!!, mChannel!!, this, peerListListener, receivedMessagesAdapater)

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



    // Service Discovery

    fun publishService() {
        startRegistration()
    }

    fun detectServices() {
        discoverService()
        executeServiceRequest()
    }

    fun addMessageToRecord() {
        record.put(Calendar.getInstance().time.toString(),EditMessageText.text.toString())
    }

    // Add a local service
    var record : HashMap<String, String> = HashMap()
    private fun startRegistration() {
        //  Create a string map containing information about your service.

        record.put("listenport", "8888")
        record.put("buddyname", "John Doe" + (Math.random() * 1000).toInt())
        record.put("available", "visible")

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record)

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager?.addLocalService(mChannel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                Log.e("MainActivity", "ActionListener OnSuccess")
            }

            override fun onFailure(arg0: Int) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.e("MainActivity", "Failure")
            }
        })
    }

    val buddies = HashMap<String, String>()
    fun discoverService() {

        var txtListener = object : WifiP2pManager.DnsSdTxtRecordListener{
            override fun onDnsSdTxtRecordAvailable(fullDomainName: String?, txtRecordMap: MutableMap<String, String>?, srcDevice: WifiP2pDevice?) {
                buddies.clear()
                                for (entries in txtRecordMap!!.iterator()) {
                    buddies?.put(entries.key, entries.value)
                }
            }
        }

        var servListener = object : WifiP2pManager.DnsSdServiceResponseListener{
            override fun onDnsSdServiceAvailable(instanceName: String?, registrationType: String?, srcDevice: WifiP2pDevice?) {
                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.

                if (buddies.containsKey(srcDevice?.deviceAddress)) {
                    srcDevice?.deviceName = buddies.getValue(srcDevice!!.deviceAddress)
                }

                // Add to the custom adapter defined specifically for showing
                // wifi devices.
                //servicesAdapater?.add(srcDevice)
                servicesAdapater?.clear()
                for ((key, value) in buddies) {
                    servicesAdapater?.add(key + " : " + value)
                }
            }
        }

        mManager?.setDnsSdResponseListeners(mChannel, servListener, txtListener)


    }

    fun executeServiceRequest() {
        var serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
        mManager?.addServiceRequest(mChannel, serviceRequest, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Success!
                Log.e("MainActivity", "Service Request OnSuccess")
            }

            override fun onFailure(reason: Int) {
                // Command failed. Check for P2P_UNSUPPORTED, ERROR or BUSY
                Log.e("MainActivity", "Service Request OnFailure")
            }

        })

        mManager?.discoverServices(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Success
                Log.e("MainActivity", "Discover Services OnSuccess")
            }

            override fun onFailure(reason: Int) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.e("MainActivity", "Discover Services OnFailure")
            }
        })
    }


}
