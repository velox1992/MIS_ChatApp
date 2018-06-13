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
import android.support.coreutils.R.id.async
import android.widget.Toast
import edu.rit.se.wifibuddy.CommunicationManager
import edu.rit.se.wifibuddy.DnsSdService
import edu.rit.se.wifibuddy.DnsSdTxtRecord
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {


    var TAG = "MainActivity"
    var wifiDirectHandler : WifiDirectHandler? = null   // Zentraler Ansprechpartner für die WifiBuddy API
    var wifiDirectHandlerBound : Boolean = false
    var mIntent : Intent? = null

    var SdServiceMap :  Map<String, DnsSdService>? = null   // Informationen zu gefunden Services
    var TxtRecordMap : Map<String, DnsSdTxtRecord>? = null  // Informationen zu gefunden Services

    var services = ArrayList<DnsSdService>()
    var servicesAdapater : ArrayAdapter<DnsSdService>? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        BtnRegisterService.setOnClickListener { registerService() }
        BtnStartServiceDiscovery.setOnClickListener { startServiceDiscovery() }
        BtnGetDiscoveryResults.setOnClickListener{ getDiscoverResults() }
        BtnSendMessage.setOnClickListener{ sendMessage() }

        servicesAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                services)
        ListViewFoundServices.adapter = servicesAdapater
        ListViewFoundServices.setOnItemClickListener { parent, view, position, id ->
            Toast.makeText(this, "Position Clicked:"+" "+position,Toast.LENGTH_SHORT).show()
            wifiDirectHandler?.initiateConnectToService(services[position])
        }


        registerCommunicationReceiver()
        Log.i(TAG, "MainActivity created");

        mIntent = Intent(this, WifiDirectHandler::class.java)
        var result = bindService(mIntent, wifiServiceConnection, Context.BIND_AUTO_CREATE)
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
        TxtRecordMap = wifiDirectHandler?.getDnsSdTxtRecordMap()
        SdServiceMap = wifiDirectHandler?.getDnsSdServiceMap()

        servicesAdapater?.clear()
        for  (entries in SdServiceMap!!.iterator()) {
            servicesAdapater!!.add(entries.value)
        }
    }

    fun registerService() {
        var record : HashMap<String, String> = HashMap()
        record.put("Autor", "Georg")
        wifiDirectHandler?.addLocalService("TigerChatService", record)
    }

    fun sendMessage() {

        thread(start=true){
            Log.e("MainActivity", "Thread launched")
            // Das hier sollte asynchron ablaufen!
            var hCommunicationManager = wifiDirectHandler!!.communicationManager

            if (hCommunicationManager != null) {
                var hMsg: String = "Hello, here is " + android.os.Build.MODEL
                hMsg = EdtTextMsg.text.toString()
                var hMsgByteArray = hMsg.toByteArray(Charsets.UTF_8)
                FCommunicationManager!!.write(hMsgByteArray)
            }
        }
    }











    var FCommunicationManager : CommunicationManager? = null
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


                FCommunicationManager = wifiDirectHandler!!.communicationManager


            } else if (intent.action == WifiDirectHandler.Action.DEVICE_CHANGED) {
                // This device's information has changed
                Log.i(TAG, "This device changed")
            } else if (intent.action == WifiDirectHandler.Action.MESSAGE_RECEIVED) {
                // A message from the Communication Manager has been received
                // Die Nachricht erhält man über den Intent
                var hMsgByte = intent.getByteArrayExtra(WifiDirectHandler.MESSAGE_KEY)
                var hMsg = String(hMsgByte, Charsets.UTF_8)
                TxtViewMsg.text = hMsg

                Log.i(TAG, "Message received")
            } else if (intent.action == WifiDirectHandler.Action.WIFI_STATE_CHANGED) {
                // Wi-Fi has been enabled or disabled
                Log.i(TAG, "Wi-Fi state changed")
            }
        }
    }

}
