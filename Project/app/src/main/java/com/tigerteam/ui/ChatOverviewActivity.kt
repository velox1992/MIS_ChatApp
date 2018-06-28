package com.tigerteam.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pConfig
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.tigerteam.communication.WiFiDirectBroadcastReceiver
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.MainActivity
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.CreateChatContact
import com.tigerteam.ui.Objects.NearbyDevices
import com.tigerteam.ui.helper.ChatOverviewRecyclerAdapter
import com.tigerteam.ui.helper.IChatOverviewItemClickListener

import kotlinx.android.synthetic.main.activity_chat_overview.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_chat_overview.*
import kotlin.math.log
import kotlin.system.exitProcess

class ChatOverviewActivity : AppCompatActivity()
{


	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "ChatOverviewActivity"
	private val USER_NAME_REQUEST = 666
	private val CREATE_CHAT_REQUEST = 45

	private val chatServiceConnection = object : ServiceConnection
	{
		override fun onServiceConnected(name: ComponentName?, service: IBinder?)
		{
			Log.i(TAG, "onServiceConnected")
			val binder = service as ChatService.ChatServiceBinder
			chatService = binder.getService()
			isChatServiceBound = true

			//----
			startFirstUseActivity()
            initializeCommunication()
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


	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: RecyclerView.Adapter<*>
	private lateinit var viewManager: RecyclerView.LayoutManager

	private var chatClickListener = object : IChatOverviewItemClickListener
	{
		override fun clickedChat(chatId: String, chatName: String) {
			startChatActivity(chatId, chatName)
		}
	}

    // Kommunikation
    private var mManager : WifiP2pManager? = null
    private var mChannel : WifiP2pManager.Channel? = null
    private var mReceiver : BroadcastReceiver? = null
    private var mIntentFilter : IntentFilter? = null

    var mContiniousWifiDiscoveryThread : Thread? = null
    var mContiniousWifiDiscoveryTask : ContiniousWifiDiscoveryTask? = null

    var peers = java.util.ArrayList<WifiP2pDevice>()
    var peersAdapater : ArrayAdapter<WifiP2pDevice>? = null

    var peerListListener = object : WifiP2pManager.PeerListListener{
        override fun onPeersAvailable(peerList: WifiP2pDeviceList?) {
            val refreshedPeers = peerList?.getDeviceList()

            if (refreshedPeers != peers) {
                Log.d(TAG, "Es sind neue Peers vorhanden")
                peers!!.clear()
                peers!!.addAll(refreshedPeers!!.toList())
            }

            if (peers.size == 0) {
                Log.d("MainActivity", "No peers found")
                // no peers found
            }
        }
    }

    var mChatOverviewBroadCastReceiver : ChatOverviewBroadCastReceiver? = null


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat_overview)
		setSupportActionBar(toolbar)




		//----
		val chatIntent = Intent(this, ChatService::class.java)

		// Keeps the service after unbind alive
		startService(chatIntent)

		// Bind to service to get the service API-Object
		bindService(chatIntent, chatServiceConnection, Context.BIND_AUTO_CREATE)


		// Permissions holen
		var neededPermissions = mutableListOf<String>()

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			neededPermissions.add(Manifest.permission.READ_PHONE_STATE)
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
			Log.e(TAG, "Error because not getting Permission")
			Toast.makeText(this, "Error because not getting Permission", Toast.LENGTH_LONG).show()
			//exitProcess(55);
		}
	}


	override fun onStart()
	{
		super.onStart()
	}

	override fun onResume()
	{
		super.onResume()

		// Chats aktualisieren
		if(isChatServiceBound) {
			showMyChats()
		}


        // Es gibt leider keine Möglichkeit zu prüfen ob schon ein Receiver registriert ist
        try {
            // Kommunikation wieder anschmeißen
            registerReceiver(mReceiver, mIntentFilter)


            // Start Peer-Discovery Thread
            mContiniousWifiDiscoveryTask = ContiniousWifiDiscoveryTask()
            mContiniousWifiDiscoveryThread = Thread(mContiniousWifiDiscoveryTask)
            mContiniousWifiDiscoveryThread!!.start()
        }
        catch (e : Exception) {
            e.printStackTrace()
        }


	}

    override fun onPause() {
        // Stop Peer-Discovery
        //mContiniousWifiDiscoveryTask!!.running = false
        //unregisterReceiver(mReceiver)

        super.onPause()
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

				initCompleted()
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

				showMyChats()
			}
		}

		super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.main_menu, menu) //your file name
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {

		when (item!!.getItemId()) {
			R.id.action_settings -> {
                startNearbyDevicesActivity()
                return true
            }
			else -> return super.onOptionsItemSelected(item)
		}
	}




	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------
/*
	fun call_chat_service_button_clicked(view: View)
	{
		hello_world_text_view.text = chatService?.getVersion()
	}


	fun createChatButtonClicked(view : View){
		startCreateChatActivity();
	}*/




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
		else
		{
			initCompleted()
		}
	}


	fun initCompleted()
	{
		Log.i(TAG, "initCompleted")

		fab.setOnClickListener { view ->
			startCreateChatActivity()
		}

		chatService!!.fillSomeTestData()

		showMyChats()
	}


	fun showMyChats(){
		if(isChatServiceBound) {
			val chatOverviewITems = chatService!!.getChatOverview()

			viewManager = LinearLayoutManager(this)

			viewAdapter = ChatOverviewRecyclerAdapter(chatOverviewITems, chatClickListener)


			recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
				// use this setting to improve performance if you know that changes
				// in content do not change the layout size of the RecyclerView
				setHasFixedSize(true)

				// use a linear layout manager
				layoutManager = viewManager

				// specify an viewAdapter (see also next example)
				adapter = viewAdapter

				// vertical Dividing-Lines
				addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
			}
		}
		else{
			Log.e(TAG, "showMyChats => ChatService is not bound")
		}
	}



	fun startCreateChatActivity() {
		val contacts = chatService!!.getContactsForCreatingChat()

		val intent = Intent(this, CreateChatActivity::class.java)
		intent.putExtra(Constants.EXTRA_CHAT_USERS, ArrayList(contacts))
		startActivityForResult(intent, CREATE_CHAT_REQUEST)
	}


	fun startChatActivity(chatId: String, chatName : String){
		val chatItems = chatService!!.getChatItems(chatId)
		val ownUserId = chatService!!.getOwnUserId()

		if(ownUserId != null) {
			val intent = Intent(this, ChatActivity::class.java)
			intent.putExtra(Constants.EXTRA_CHAT_ITEMS, ArrayList(chatItems))
			intent.putExtra(Constants.EXTRA_OWN_USER_ID, ownUserId)
			intent.putExtra(Constants.EXTRA_CHAT_TITLE, chatName)
			intent.putExtra(Constants.EXTRA_CHAT_ID, chatId)
			startActivity(intent)
		}
		else
		{
			Log.e(TAG, "startChatActivity -> NO Param with own UserId")
		}
	}

    fun initializeCommunication() {
        // Register App with Wi-Fi P2P Framework and create Channel to connect the app with the Wi-Fi P2P Framework
        // Außerdem den BroadcastReceiver erstellen um von Änderungen zu erfahren
        mManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager?.initialize(this, mainLooper, null)


        mReceiver = WiFiDirectBroadcastReceiver(mManager!!, mChannel!!, this, peerListListener, chatService!!)

        mIntentFilter = IntentFilter()
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter?.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)


        // Reagieren auf die Device-Auswahl in der Connect Activity
        mChatOverviewBroadCastReceiver = ChatOverviewBroadCastReceiver()
        registerReceiver(mChatOverviewBroadCastReceiver, IntentFilter(Constants.WIFI_CONNECT_TO_DEVICE))

        registerReceiver(mReceiver, mIntentFilter)
    }


    fun startNearbyDevicesActivity() {
        val intent = Intent(this, NearbyDevices::class.java)
        intent.putExtra(Constants.EXTRA_DEVICES, peers)
        startActivity(intent)
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

    fun connect(device : WifiP2pDevice) {
        Log.d(TAG, "Connect to Peer")


        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress
        config.wps.setup = WpsInfo.PBC

        if (device.status == 3 || device.status == 1 ) {
            mManager?.connect(mChannel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver notifies zus. Ignore for now
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(this@ChatOverviewActivity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //----------------------------------------------------------------------------------------------
    // Inner classes
    //----------------------------------------------------------------------------------------------

    inner class ChatOverviewBroadCastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Receiced connect task from NearbyDevices activity")

            val extras = intent!!.extras
            val obj = extras.get(Constants.EXTRA_SELECTED_WIFI_DEVICE)
            var selectedDevice = obj as WifiP2pDevice
            connect(selectedDevice)
        }
    }


    inner class ContiniousWifiDiscoveryTask() : Runnable {

        @Volatile
        var running = true

        override fun run() {
            while (running) {
                discover()
                Log.d(TAG, "New discover started.")
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
