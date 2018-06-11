package com.tigerteam.mischat

import android.content.*
import android.net.wifi.p2p.WifiP2pManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.os.IBinder
import java.util.*
import edu.rit.se.wifibuddy.WifiDirectHandler
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.content.BroadcastReceiver




class MainActivity : AppCompatActivity() {


    var TAG = "MainActivity"
    var wifiDirectHandler : WifiDirectHandler? = null
    var wifiDirectHandlerBound : Boolean = false
    var mIntent : Intent? = null





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        BtnRegisterService.setOnClickListener { registerService() }
        BtnStartServiceDiscovery.setOnClickListener { startServiceDiscovery() }
        BtnGetDiscoveryResults.setOnClickListener{ getDiscoverResults() }

        registerCommunicationReceiver()
        Log.i(TAG, "MainActivity created");


        mIntent = Intent(this, WifiDirectHandler::class.java)
        var result = bindService(mIntent, wifiServiceConnection, Context.BIND_AUTO_CREATE)
        // result == false
    }


    fun registerCommunicationReceiver() {
        val communicationReceiver = CommunicationReceiver()
        var mIntentFilter = IntentFilter()
        mIntentFilter?.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        mIntentFilter?.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        mIntentFilter?.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        mIntentFilter?.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(CommunicationReceiver(), mIntentFilter)
        Log.i(TAG, "Communication Receiver registered");
    }


    val wifiServiceConnection  = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i("MainAcitivity", "Binding WifiDirectHandler service")
            Log.i("MainAcitivity", "ComponentName: $name")
            Log.i("MainAcitivity", "Service: $service")

            val binder = service as WifiDirectHandler.WifiTesterBinder
            wifiDirectHandler = binder.service
            wifiDirectHandlerBound = true
            Log.i("MainAcitivity", "WifiDirectHandler service bound")

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            wifiDirectHandlerBound = false;
            Log.i("MainAcitivity", "WifiDirectHandler service unbound");
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.i("MainAcitivity", "WifiDirectHandler service unbound");
        }


    }












    fun startServiceDiscovery() {
        wifiDirectHandler?.continuouslyDiscoverServices()

    }

    fun getDiscoverResults() {
        var TxtRecordMap = wifiDirectHandler?.getDnsSdTxtRecordMap()
        var SdServiceMap = wifiDirectHandler?.getDnsSdServiceMap()
    }

    fun registerService() {
        var record : HashMap<String, String> = HashMap()
        record.put("Autor", "Georg")
        wifiDirectHandler?.addLocalService("TigerChatService", record)
    }



















    /**
     * BroadcastReceiver used to receive Intents fired from the WifiDirectHandler when P2P events occur
     * Used to update the UI and receive communication messages
     */
    inner class CommunicationReceiver : BroadcastReceiver() {

        var TAG = "CommunicationReceiver"
        override fun onReceive(context: Context, intent: Intent) {
            // Get the intent sent by WifiDirectHandler when a service is found
            if (intent.action == WifiDirectHandler.Action.SERVICE_CONNECTED) {
                // This device has connected to another device broadcasting the same service
                Log.i(TAG, "Service connected")
            } else if (intent.action == WifiDirectHandler.Action.DEVICE_CHANGED) {
                // This device's information has changed
                Log.i(TAG, "This device changed")
            } else if (intent.action == WifiDirectHandler.Action.MESSAGE_RECEIVED) {
                // A message from the Communication Manager has been received
                Log.i(TAG, "Message received")
            } else if (intent.action == WifiDirectHandler.Action.WIFI_STATE_CHANGED) {
                // Wi-Fi has been enabled or disabled
                Log.i(TAG, "Wi-Fi state changed")
            }
        }
    }

}
