package com.tigerteam.mischat

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bridgefy.sdk.client.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap
import com.bridgefy.sdk.client.Bridgefy



class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var chatService: ChatService? = null
    private var isChatServiceBound = false


    private val chatServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChatService.ChatServiceBinder
            chatService = binder.getService()
            isChatServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            chatService = null
            isChatServiceBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //----
        val chatIntent = Intent(this, ChatService::class.java)

        // Keeps the service after unbind alive
        startService(chatIntent)

        // Bind to service to get the service API-Object
        bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)



        var listener = object : RegistrationListener() {
            override fun onRegistrationSuccessful(bridgefyClient: BridgefyClient?) {

                val myId = bridgefyClient?.userUuid;
                // Start Bridgefy
                startBridgefy()
            }

            override fun onRegistrationFailed(errorCode: Int, message: String?) {
                Toast.makeText(baseContext, getString(R.string.registration_error),
                        Toast.LENGTH_LONG).show()
            }
        }


        JackTheRipper.Rip(applicationContext, listener)

        //Bridgefy.initialize(applicationContext, listener)

    }

    private fun startBridgefy()    {
        Bridgefy.start(messageListener, stateListener);
    }


    private val messageListener = object : MessageListener() {
        override fun onMessageReceived(message: Message) {
            // direct messages carrying a Device name represent device handshakes
            if (message.content["device_name"] != null) {
                val peer = Peer(message.senderId,
                        message.content["device_name"] as String)
                peer.setNearby(true)
                peer.setDeviceType(extractType(message))

                peerList.add(peer);

                Log.d(TAG, "Peer introduced itself: " + peer.getDeviceName())

                // any other direct message should be treated as such
            } else {
                val incomingMessage = message.content["text"] as String
                Log.d(TAG, "Incoming private message: $incomingMessage")
                /*LocalBroadcastManager.getInstance(baseContext).sendBroadcast(
                        Intent(message.senderId)
                                .putExtra(INTENT_EXTRA_MSG, incomingMessage))*/

                handleIncomingMessage(message)

            }
        }

        override fun onBroadcastMessageReceived(message: Message) {
            // we should not expect to have connected previously to the device that originated
            // the incoming broadcast message, so device information is included in this packet
            val incomingMsg = message.content["text"] as String
            val deviceName = message.content["device_name"] as String
            val deviceType = extractType(message)

            Log.d(TAG, "Incoming broadcast message: $incomingMsg")

            /*LocalBroadcastManager.getInstance(baseContext).sendBroadcast(
                    Intent(BROADCAST_CHAT)
                            .putExtra(INTENT_EXTRA_NAME, deviceName)
                            .putExtra(INTENT_EXTRA_TYPE, deviceType)
                            .putExtra(INTENT_EXTRA_MSG, incomingMsg))*/
        }
    }

    private fun extractType(message: Message): Peer.DeviceType {
        val eventOrdinal: Int
        val eventObj = message.content["device_type"]
        if (eventObj is Double) {
            eventOrdinal = eventObj.toInt()
        } else {
            eventOrdinal = eventObj as Int
        }
        return Peer.DeviceType.values()[eventOrdinal]
    }


    internal var stateListener: StateListener = object : StateListener() {
        override fun onDeviceConnected(device: Device, session: Session?) {
            Log.i(TAG, "onDeviceConnected: " + device.userId)
            // send our information to the Device
            val map = HashMap<String, Any>()
            map["device_name"] = Build.MANUFACTURER + " " + Build.MODEL
            map["device_type"] = Peer.DeviceType.ANDROID.ordinal
            device.sendMessage(map)
        }

        override fun onDeviceLost(device: Device) {
            Log.w(TAG, "onDeviceLost: " + device.userId)


            var peerToDelete : Peer? = null;
            for(peer : Peer in peerList) {
                if(peer.getUuid() == device.userId) {
                    peerToDelete = peer;
                }
            }

            if(peerToDelete != null)
            {
                peerList.remove(peerToDelete)
            }
        }

        override fun onStartError(message: String?, errorCode: Int) {
            Log.e(TAG, "onStartError: " + message!!)

            if (errorCode == StateListener.INSUFFICIENT_PERMISSIONS) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
        }
    }


    var peerList = mutableListOf<Peer>()

    fun SendMsgToAllPeers(view: View) {

        var msg = editText.text.toString();
        if(msg != null && msg.length > 0)
        {
            for(peer : Peer in peerList) {

                val map = HashMap<String, Any>()
                map["text"] = msg

                // Create a message with the HashMap and the recipient's id
                val message = Message.Builder().setContent(map).setReceiverId(peer.getUuid()).build()

                // Send the message to the specified recipient
                Bridgefy.sendMessage(message)

                Log.i(TAG, "SendMsgToAllPeers: to Peer: " + peer.getUuid())
            }
        }
    }

    fun handleIncomingMessage(msg : Message)    {

        val incomingMessage = msg.content["text"] as String
        val senderID = msg.senderId

        textView2.text = "${senderID}: ${incomingMessage}"
    }






    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing)
            Bridgefy.stop()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start Bridgefy
            startBridgefy()

        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Location permissions needed to start peers discovery.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }



















    fun call_chat_service_button_clicked(view: View) {
        hello_world_text_view.text = chatService?.getVersion()
    }



}
