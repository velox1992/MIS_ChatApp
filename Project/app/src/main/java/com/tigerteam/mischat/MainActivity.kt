package com.tigerteam.mischat


import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.tigerteam.ui.ChatOverviewActivity
import com.tigerteam.ui.CreateChatActivity
import com.tigerteam.ui.FirstUseActivity
import com.tigerteam.ui.Objects.CreateChatContact
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable
import kotlin.system.exitProcess
import android.content.BroadcastReceiver
import android.net.wifi.p2p.WifiP2pManager
import android.content.Context.WIFI_P2P_SERVICE
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.widget.Adapter
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.*
import android.widget.AdapterView
import java.lang.reflect.Array.get
import java.net.Socket
import java.util.*



class MainActivity : AppCompatActivity()
{
	/*
	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "MainActivity"
    private val USER_NAME_REQUEST = 666
	private val CREATE_CHAT_REQUEST = 45

	private val chatServiceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(name: ComponentName?, service: IBinder?)
		{
			val binder = service as ChatService.ChatServiceBinder
			chatService = binder.getService()
			isChatServiceBound = true

			//----
            startFirstUseActivity()
		}

		override fun onServiceDisconnected(name: ComponentName)
		{
			chatService = null
			isChatServiceBound = false
		}
	}


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

	private var chatService: ChatService? = null
	private var isChatServiceBound = false


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
    {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		//----
		val chatIntent = Intent(this, ChatService::class.java)

		// Keeps the service after unbind alive
		startService(chatIntent)

		// Bind to service to get the service API-Object
		bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)


		// Permissions holen
		var neededPermissions = mutableListOf<String>()

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.READ_PHONE_NUMBERS)
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.READ_CONTACTS)
		}


		if(neededPermissions.size > 0)
		{
			ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 22)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		if(requestCode == 22 && grantResults.any{it == PackageManager.PERMISSION_DENIED} )
		{
			Log.e(TAG, "Exit because not getting Permission")
			exitProcess(55);
		}
	}


	override fun onStart()
	{
		super.onStart()
	}

	override fun onResume()
	{
		super.onResume()
	}

	override fun onDestroy()
	{
		unbindService(chatServiceConnection)
		super.onDestroy()
	}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
	    Log.d(TAG, "onActivityResult($requestCode, $resultCode)")

	    if(requestCode == USER_NAME_REQUEST)
	    {
			if(resultCode == RESULT_OK)
			{
				// User name was entered.
				val userName : String = data!!.extras.getString(Constants.EXTRA_USER_NAME)
				chatService?.firstUseCreateUser(userName)
			}
		    else
			{
				// User clicked back button
				startFirstUseActivity()
			}
	    }
		else if (requestCode == CREATE_CHAT_REQUEST)
		{
			if(resultCode == RESULT_OK)
			{
				val chatName = data!!.extras.getString(Constants.EXTRA_CHAT_NAME)
				val chatUsers = (data!!.extras.get(Constants.EXTRA_CHAT_USERS) as ArrayList<CreateChatContact>).toList()

				chatService?.createChat(chatName, chatUsers)

			}
			else
			{
				Toast.makeText(this,"Etwas ist schief gelaufen beim Chat Erstellen :(", Toast.LENGTH_LONG).show()
			}
		}

        super.onActivityResult(requestCode, resultCode, data)
    }


	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------

	fun call_chat_service_button_clicked(view: View)
	{
		hello_world_text_view.text = chatService?.getVersion()
	}


	fun createChatButtonClicked(view : View){
		startCreateChat();
	}


	fun chatOverviewButtonClicked(view: View){
		startChatOverview()
	}


	//----------------------------------------------------------------------------------------------
	// Methods
	//----------------------------------------------------------------------------------------------

	fun startFirstUseActivity()
	{
		if(chatService!!.isFirstUse())
		{
			val intent = Intent(this, FirstUseActivity::class.java)
			startActivityForResult(intent, USER_NAME_REQUEST)
		}
	}


    fun startCreateChat() {
		val contacts = chatService!!.getContactsForCreatingChat()

        val intent = Intent(this, CreateChatActivity::class.java)
		intent.putExtra(Constants.EXTRA_CHAT_USERS, ArrayList(contacts))
        startActivityForResult(intent, CREATE_CHAT_REQUEST)
    }


	fun startChatOverview() {
		val intent = Intent(this, ChatOverviewActivity::class.java)
		startActivity(intent)
	}
*/

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
                //LblClientOrServer.text = "I'm a client ;)"
                Log.d(TAG, "I'm a client")
            }
            else if (msg!!.what == Constants.HANDLER_CODE_SERVER_ROLE_DETERMINED){
                //LblClientOrServer.text = "I'm the server :)"
                Log.d(TAG, "I'm the server")
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
        //BtnDiscover.setOnClickListener { discover() }
        //BtnConnect.setOnClickListener{ connect() }


        // Listen Adapter und Verbindung mit List View
        peersAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                peers)
        //ListViewPeers.adapter = peersAdapater

        receivedMessagesAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                receivedMessages)
        //ListViewReceivedMessages.adapter = receivedMessagesAdapater




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


        /*
        ListViewPeers.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                connect(position)
            }
        }
        */
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
