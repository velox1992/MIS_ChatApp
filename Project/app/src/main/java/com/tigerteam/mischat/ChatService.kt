package com.tigerteam.mischat

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*
import com.tigerteam.database.*
import com.tigerteam.database.DbObjects.*
import com.tigerteam.ui.Objects.CreateChatContact
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import com.tigerteam.ui.Objects.Contact
import android.provider.ContactsContract
import android.support.v4.content.LocalBroadcastManager
import com.tigerteam.WifiP2p.IPExchangeClient
import com.tigerteam.WifiP2p.IPExchangeServer
import com.tigerteam.WifiP2p.WifiP2pBroadcastReceiver
import com.tigerteam.intent.UpdateUIIntent
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.Objects.ChatOverviewItem
import java.net.InetAddress


class ChatService : Service()
{
	inner class ChatServiceBinder : Binder()
	{
		fun getService() : ChatService
		{
			return this@ChatService
		}
	}

	//----------------------------------------------------------------------------------------------
	// Const Variables
	//----------------------------------------------------------------------------------------------

	private val TAG = "ChatService"
	private val chatServiceBinder = ChatServiceBinder()


	//----------------------------------------------------------------------------------------------
	// Variables
	//----------------------------------------------------------------------------------------------

	private var db : IChatDbHelper = ChatDbHelper(this)
	private var syncServer : Thread? = null
	private var syncClient : Thread? = null

	private var wifiP2pManager : WifiP2pManager? = null
	private var wifiP2pChannel : WifiP2pManager.Channel? = null
	private var intentFilter : IntentFilter? = null
	private var broadcastReceiver : WifiP2pBroadcastReceiver? = null

	private var wifiP2pDiscoveryState : Int = -1
	private var ipExchangeServerThread : Thread? = null
	private var ipExchangeClientThread : Thread? = null
	private var ownInetAddress : InetAddress? = null
	private var inetAddresses = mutableSetOf<InetAddress>()


	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onBind(intent: Intent): IBinder?
	{
		Log.i(TAG, "ChatService onBind")
		return chatServiceBinder
	}

	override fun onCreate()
	{
		Log.i(TAG, "ChatService onCreate")

		//----
		syncServer = Thread(com.tigerteam.sync.Server(this))
		syncServer!!.start()

		syncClient = Thread(com.tigerteam.sync.Client(this))
		syncClient!!.start()

		//----
		wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
		wifiP2pChannel = wifiP2pManager!!.initialize(this, getMainLooper(), null)

		//----
		broadcastReceiver = WifiP2pBroadcastReceiver(this)

		//----
		intentFilter = IntentFilter()
		intentFilter!!.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
		intentFilter!!.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
		intentFilter!!.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
		intentFilter!!.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
		intentFilter!!.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)

		//----
		registerReceiver(broadcastReceiver, intentFilter)

		//----
		wifiP2pManager!!.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener
		{
            override fun onSuccess()
            {
                Log.d(TAG, "WifiP2pManager.discoverPeers() => OnSuccess")
            }

            override fun onFailure(reasonCode: Int)
            {
	            Log.d(TAG, "WifiP2pManager.discoverPeers() => onFailure($reasonCode)")
            }
        })
	}

	override fun onDestroy()
	{
		Log.i(TAG, "ChatService onDestroy")

		//----
		unregisterReceiver(broadcastReceiver)
	}


	//----------------------------------------------------------------------------------------------
	// API Methods
	//----------------------------------------------------------------------------------------------

	public fun getVersion() : String
	{
		return "1.0";
	}

	//
	// First use of the application?
	//
	public fun isFirstUse() : Boolean
	{
		val ownUserId = db.getParameter(Constants.PARAM_OWN_USER_ID)

		if(ownUserId == null)
		{
			Log.d(TAG, "isFirstUse() : true")
			return true
		}
		else
		{
			Log.d(TAG, "isFirstUse() : false")
			return false
		}
	}

	//
	// Create a user account for the first time use.
	//
	public fun firstUseCreateUser(nickName : String)
	{

		try
		{
			var phoneNumber : String? = null
			val tMgr = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
				phoneNumber = tMgr.line1Number
			}
			else{
				Log.i(TAG, "firstUseCreateUser: could not read own Phone number")
			}


			val uuidNewUser = UUID.randomUUID().toString()
			val newUser = User(uuidNewUser, nickName, phoneNumber)
			val ownUserId = Parameter(Constants.PARAM_OWN_USER_ID, "String", uuidNewUser)

			Log.d(TAG, "firstUseCreateUser: UserId=${newUser.id}, UserName=${newUser.name}, PhoneNumner=${newUser.phoneNumber}")

			db.upsertUser(newUser)
			db.upsertParameter(ownUserId)
		}
		catch (e: Exception)
		{
			Log.e(TAG, "firstUseCreateUser($nickName) => " + e.toString())

		}
	}

	/**
	 * Kontakte aus dem Telefonbuch lesen
	 */
	public fun getAllContacts() : List<Contact>{
		val ret = mutableListOf<Contact>()

		try {
			val cr = contentResolver
			val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

			if (cur?.count ?: 0 > 0) {
				while (cur != null && cur.moveToNext()) {
					val id = cur.getString(
							cur.getColumnIndex(ContactsContract.Contacts._ID))
					val name = cur.getString(cur.getColumnIndex(
							ContactsContract.Contacts.DISPLAY_NAME))

					if (cur.getInt(cur.getColumnIndex(
									ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
						val pCur = cr.query(
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
								arrayOf(id), null)
						while (pCur!!.moveToNext()) {
							var phoneNo = pCur.getString(pCur.getColumnIndex(
									ContactsContract.CommonDataKinds.Phone.NUMBER))

							Log.d(TAG, "Name: $name")
							Log.d(TAG, "Phone Number: $phoneNo")

							// scheinbar hat die Telefonnummer manchmal leerzeichen
							phoneNo = phoneNo.replace(" ", "")

							ret.add(Contact(name, phoneNo))
						}
						pCur.close()
					}
				}
			}
			cur?.close()
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getAllContacts => " + e.toString())
		}

		return ret
	}


	/**
	 * Liefert eine Liste der Kontakte, die in einem Chat beim Erstellen ausgewählt werden können
	 */
	public fun getContactsForCreatingChat() : List<CreateChatContact>{

		var tmp = mutableListOf<CreateChatContact>()
		val onlyKnownContacts = false // true bei Test, false bei echt

		try {
			if(onlyKnownContacts) {
				// hier noch nicht Performance-Optimiert, sondern recht billo

				val contacts = getAllContacts()

				if(contacts.size > 0){
					val numbers = contacts.map { it.phoneNumber }
					val usersWithThisApp = db.getUsersWithPhoneNumberIn(numbers)

					for(realUser in usersWithThisApp)
					{
						val nameInContact = contacts.find { it.phoneNumber == realUser.phoneNumber }!!.name
						tmp.add(CreateChatContact(realUser.id, realUser.name, nameInContact, realUser.phoneNumber))
					}
				}
			}
			else {
				// Für Testzwecke, also alle User in der DB anzeigen
				val allUsers = db.getAllUsers()

				for(user : User in allUsers)
				{
					tmp.add(CreateChatContact(user.id, user.name, "-", user.phoneNumber))
				}
			}
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getContactsForCreatingChat => " + e.toString())
		}

		return tmp
	}


	/**
	 * Einen Chat anlegen in der DB
	 */
	public fun createChat(chatName : String, otherContacts : List<CreateChatContact>) {
		// nicht vergessen mich selbst mit hinzuzufügen

		try {
		    Log.i(TAG, "Create Chat with Name=${chatName}")

			val chat = Chat(UUID.randomUUID().toString(), chatName)
			db.upsertChat(chat)
			for(elem in otherContacts)
			{
				var chatUser = ChatUser(chat.id, elem.userId,false)
				db.upsertChatUser(chatUser)
			}

			// den Ersteller nicht vergessen
			var ownUserId = getOwnUserId()
			if(ownUserId != null) {
				var chatUser = ChatUser(chat.id, ownUserId, true)
				db.upsertChatUser(chatUser)
			}
			else {
				Log.e(TAG, "CreateChat => missing own userId")
			}
		}
		catch (e: Exception)
		{
			Log.e(TAG, "CreateChat => " + e.toString())

		}
	}

	/**
	 * liefert die eigenen User-Id
	 */
	public fun getOwnUserId() : String?
	{
		var ret : String? = null
		try {
			val param = db.getParameter(Constants.PARAM_OWN_USER_ID)
			if(param != null)
			{
				ret = param.value
			}
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getOwnUserId => " + e.toString())
		}

		return ret
	}


	/**
	 * Liefert die Elemente der Chat-Übersicht
	 */
	public  fun getChatOverview() : List<ChatOverviewItem>
	{
		var ret = mutableListOf<ChatOverviewItem>()

		try {

			val chatOverviewItems = db.getChatOverviewItems()

			if (chatOverviewItems != null) {
				for(item in chatOverviewItems) {
					ret.add(item)
				}
			} else {
				Log.e(TAG, "getChatOverview => no data for ChatOverview" )
			}
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getChatOverview => " + e.toString())

		}

		return ret;
	}


	/**
	 * Einträge aus einem Chat liefern
	 */
	public  fun getChatItems(chatId : String) : List<ChatItem>
	{
		var ret = mutableListOf<ChatItem>()

		try {

			val chatItems = db.getMessagesForChat(chatId)

			if (chatItems != null) {
				for(item in chatItems) {
					ret.add(item)
				}
			} else {
				Log.e(TAG, "getChatItems => no data for Chat" )
			}
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getChatItems => " + e.toString())
		}

		return ret;
	}

	/**
	 * Eine Nachricht in einem Chat versenden
	 * (-> Insert in die Datenbank)
	 */
	public fun sendMessage(chatId : String, message : String)
	{
		val ownUserId = getOwnUserId()
		val uuidNewMessage = UUID.randomUUID().toString()
		val chatMessage = ChatMessage(uuidNewMessage, Date(), Constants.MESSAGE_TYPE_TEXT, message, ownUserId!!, chatId)

		try
		{
			db.upsertMessage(chatMessage)

			LocalBroadcastManager.getInstance(this).sendBroadcast(UpdateUIIntent());
		}
		catch (e: Exception)
		{
			Log.e(TAG, "sendMessage => " + e.toString())
		}
	}

	//
	// Get all users from the database
	//
	public fun getAllUsers() : List<com.tigerteam.database.DbObjects.User>
	{
		var result : List<User> = mutableListOf<User>()

		try
		{
			result = db.getAllUsers()
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getAllUser => ${e.toString()} : ${e.message}")
		}

		return result
	}

	//
	// Get all chats from the database
	//
	public fun getAllChats() : List<com.tigerteam.database.DbObjects.Chat>
	{
		var result : List<Chat> = mutableListOf<Chat>()

		try
		{
			result = db.getAllChats()
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getAllChats => ${e.toString()} : ${e.message}")
		}

		return result
	}

	//
	// Get all ChatUsers from the database
	//
	public fun getAllChatUsers() : List<com.tigerteam.database.DbObjects.ChatUser>
	{
		var result : List<ChatUser> = mutableListOf<ChatUser>()

		try
		{
			result = db.getAllChatUsers()
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getAllChatUsers => ${e.toString()} : ${e.message}")
		}

		return result
	}

	//
	// Get all ChatMessages from the database
	//
	public fun getAllChatMessages() : List<com.tigerteam.database.DbObjects.ChatMessage>
	{
		var result : List<ChatMessage> = mutableListOf<ChatMessage>()

		try
		{
			result = db.getAllMessages()
		}
		catch (e: Exception)
		{
			Log.e(TAG, "getAllChatMessages => ${e.toString()} : ${e.message}")
		}

		return result
	}

	//
	// Add a new user
	//
	public fun addUser(user : User) : Boolean
	{
		var userAdded = false

		try
		{
			userAdded = db.upsertUser(user)
		}
		catch(e : Exception)
		{
			Log.e(TAG, "addUser(${user.id}, ${user.name}) => ${e.toString()} : ${e.message}")
		}

		return userAdded
	}

	//
	// Add a new chat
	//
	public fun addChat(chat : Chat) : Boolean
	{
		var chatAdded = false

		try
		{
			chatAdded = db.upsertChat(chat)
		}
		catch(e : Exception)
		{
			Log.e(TAG, "addChat(${chat.id}, ${chat.name}) => ${e.toString()} : ${e.message}")
		}

		return chatAdded
	}

	//
	// Add a new chatuser
	//
	public fun addChatUser(chatUser : ChatUser) : Boolean
	{
		var chatUserAdded = false

		try
		{
			chatUserAdded = db.upsertChatUser(chatUser)
		}
		catch(e : Exception)
		{
			Log.e(TAG, "addChatUser(${chatUser.chatId}, ${chatUser.userId}, ${chatUser.isOwner}) => ${e.toString()} : ${e.message}")
		}

		return chatUserAdded
	}

	//
	// Add a new chatmessage
	//
	public fun addChatMessage(chatMessage : ChatMessage) : Boolean
	{
		var chatMessageAdded = false

		try
		{
			chatMessageAdded = db.upsertMessage(chatMessage)
		}
		catch(e : Exception)
		{
			Log.e(TAG, "addChatMessage(${chatMessage.chatId}, ${chatMessage.senderId}, ${chatMessage.data}) => ${e.toString()} : ${e.message}")
		}

		return chatMessageAdded
	}

	//
	// Add a list of potential new user
	//
	public fun addUsers(users : List<User>)
	{
		var userAdded = 0

		for(user in users)
		{
			var isUserAdded = addUser(user)
			if(isUserAdded)
				userAdded++
		}
		Log.d(TAG, "User received=${users.size}, added=${userAdded}.")

		if(userAdded > 0)
		{
			LocalBroadcastManager.getInstance(this).sendBroadcast(UpdateUIIntent());
		}
	}

	//
	// Add a list of potential new chats
	//
	public fun addChats(chats : List<Chat>)
	{
		var chatsAdded = 0

		for(chat in chats)
		{
			var isChatAdded = addChat(chat)
			if(isChatAdded)
				chatsAdded++
		}
		Log.d(TAG, "Chats received=${chats.size}, added=${chatsAdded}.")

		if(chatsAdded > 0)
		{
			LocalBroadcastManager.getInstance(this).sendBroadcast(UpdateUIIntent());
		}
	}

	//
	// Add a list of potential new ChatUser
	//
	public fun addChatUsers(chatUsers : List<ChatUser>)
	{
		var chatUserAdded = 0

		for(chatUser in chatUsers)
		{
			var isChatUserAdded = addChatUser(chatUser)
			if(isChatUserAdded)
				chatUserAdded++
		}
		Log.d(TAG, "ChatUsers received=${chatUsers.size}, added=${chatUserAdded}.")

		if(chatUserAdded > 0)
		{
			LocalBroadcastManager.getInstance(this).sendBroadcast(UpdateUIIntent());
		}
	}

	//
	// Add a list of potential new ChatMessages
	//
	public fun addChatMessages(chatMessages : List<ChatMessage>)
	{
		var chatMessageAdded = 0

		for(chatMessage in chatMessages)
		{
			var isChatMessageAdded = addChatMessage(chatMessage)
			if(isChatMessageAdded)
				chatMessageAdded++
		}
		Log.d(TAG, "ChatMessages received=${chatMessages.size}, added=${chatMessageAdded}.")

		if(chatMessageAdded > 0)
		{
			LocalBroadcastManager.getInstance(this).sendBroadcast(UpdateUIIntent());
		}
	}


	//
	// IPExchangeClient adds his ip and the ip's of the other clients
	//
	public fun setPeerAddresses(inetAddresses : List<InetAddress>, ownInetAddress: InetAddress)
	{
		this.ownInetAddress = ownInetAddress
		this.inetAddresses.clear()
		this.inetAddresses.addAll(inetAddresses)
	}

	//
	// IPExchangeServer adds a new client ip.
	//
	public fun addPeerAddress(inetAddress: InetAddress)
	{
		this.inetAddresses.add(inetAddress)
	}

	//
	// Returns the peerAddresses
	//
	public fun getPeerAddresses(filterOwnAddress : Boolean) : List<InetAddress>
	{
		var peerAddresses = mutableListOf<InetAddress>()
		for(inetAddress in this.inetAddresses)
		{
			if(filterOwnAddress && (inetAddress == this.ownInetAddress))
				continue

			peerAddresses.add(inetAddress)
		}
		return peerAddresses
	}

	//
	// If the sync client cannot connect to the address, remove it.
	//
	public fun removePeerAddress(inetAddress: InetAddress)
	{
		this.inetAddresses.remove(inetAddress)
	}


	//
	// React to state changed in the WIFI_P2P_DISCOVERY
	//
	public fun wifiP2pDiscoveryChanged(discoveryState : Int)
	{
		wifiP2pDiscoveryState = discoveryState

		if(wifiP2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
		{
			Log.d(TAG, "Wi-Fi p2p discovery has started.")
		}

		if(wifiP2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)
		{
			Thread({
				Log.d(TAG, "Wi-Fi p2p discovery has stopped, so we start the next discovery in 5 seconds.")
				Thread.sleep(5000)

				wifiP2pManager!!.discoverPeers(wifiP2pChannel, object : WifiP2pManager.ActionListener
				{
					override fun onSuccess()
					{
						Log.d(TAG, "WifiP2pManager.discoverPeers() => OnSuccess")
					}

					override fun onFailure(reasonCode: Int)
					{
						Log.d(TAG, "WifiP2pManager.discoverPeers() => onFailure($reasonCode)")
					}
				})
			}).start()
		}
	}


	//
	// If the peer list changed, try to connect to new peers
	//
	public fun wifiP2pPeersChanged(wifiP2pDeviceList : WifiP2pDeviceList)
	{
		for(device in wifiP2pDeviceList.deviceList)
		{
			Thread({
				val currentThread = Thread.currentThread()

				if(device.status == WifiP2pDevice.CONNECTED)
				{
					Log.d(TAG, "(${currentThread.id}) Already connected to device: ${device.deviceName}.")
					return@Thread
				}

				if(device.deviceName.startsWith("[TV] Samsung 5 Series", true))
				{
					Log.d(TAG, "(${currentThread.id}) We do not want to connect to a TV device: ${device.deviceName}.")
					return@Thread
				}

				Log.d(TAG, "(${currentThread.id}) Try to connect to device: ${device.deviceName}.")

				val wifiP2pConfig: WifiP2pConfig = WifiP2pConfig()
				wifiP2pConfig.deviceAddress = device.deviceAddress
				wifiP2pConfig.wps.setup = WpsInfo.PBC;

				wifiP2pManager?.connect(wifiP2pChannel, wifiP2pConfig, object : WifiP2pManager.ActionListener
				{
					override fun onSuccess()
					{
						Log.d(TAG, "(${currentThread.id}) WifiP2pManager.connect -> onSuccess().")
					}

					override fun onFailure(reason: Int)
					{
						Log.d(TAG, "(${currentThread.id}) WifiP2pManager.connect -> onFailure(${reason}.")
					}
				})
			}).start()
		}
	}


	//
	// If we get connected to a new group, create a new IPExchangeClient.
	// If we get the groupOwner, create a new IPExchangeServer.
	//
	public fun wifiP2pConnectionChanged(
		wifiP2pInfo : WifiP2pInfo, wifiP2pGroup : WifiP2pGroup, networkInfo : NetworkInfo)
	{
		if(!networkInfo.isConnected())
		{
			if(ipExchangeClientThread != null)
			{
				ipExchangeClientThread!!.interrupt()
				ipExchangeClientThread = null
			}

			if(ipExchangeServerThread != null)
			{
				ipExchangeServerThread!!.interrupt()
				ipExchangeServerThread = null
			}

			return
		}

		if(wifiP2pInfo.isGroupOwner)
		{
			if(ipExchangeClientThread != null)
			{
				ipExchangeClientThread!!.interrupt()
				ipExchangeClientThread = null
			}

			if(ipExchangeServerThread == null)
			{
				ipExchangeServerThread = Thread(IPExchangeServer(this))
				ipExchangeServerThread!!.start()
			}
		}
		else
		{
			if(ipExchangeServerThread != null)
			{
				ipExchangeServerThread!!.interrupt()
				ipExchangeServerThread = null
			}

			if(ipExchangeClientThread == null)
			{
				ipExchangeClientThread = Thread(IPExchangeClient(this, wifiP2pInfo.groupOwnerAddress))
				ipExchangeClientThread !!.start()
			}
		}
	}







	public fun fillSomeTestData()
	{
        // nur einmal einfügen
        if(db.getAllChats().size == 0) {
            // diese nummer im Telefonbuch des Test-Gerätes einfügen:
            val numberother = "015152222222"

            var me = db.getUser(db.getParameter(Constants.PARAM_OWN_USER_ID)!!.value)!!
            var other = User("2222", "Lulatsch", numberother)
            db.upsertUser(other)

            val ownUserId = Parameter(Constants.PARAM_OWN_USER_ID, "String", me.id)
            db.upsertParameter(ownUserId)

            var chat = Chat("0000", "Cooler Chat")
            db.upsertChat(chat)
            var chatUser1 = ChatUser(chat.id, me.id, true)
            var chatUser2 = ChatUser(chat.id, other.id, false)
            db.upsertChatUser(chatUser1)
            db.upsertChatUser(chatUser2)

            var msgMe = ChatMessage("1", Date(Date().time - 100), Constants.MESSAGE_TYPE_TEXT, "Hallo", me.id, chat.id)
            var msgOther = ChatMessage("2", Date(Date().time - 50), Constants.MESSAGE_TYPE_TEXT, "Welt", other.id, chat.id)

            db.upsertMessage(msgMe)
            db.upsertMessage(msgOther)




            chat = Chat("0001", "Cooler Chat 2")
            db.upsertChat(chat)
            chatUser1 = ChatUser(chat.id, me.id, true)
            chatUser2 = ChatUser(chat.id, other.id, false)
            db.upsertChatUser(chatUser1)
            db.upsertChatUser(chatUser2)

            msgMe = ChatMessage("10", Date(Date().time - 100), Constants.MESSAGE_TYPE_TEXT, "Hallo", me.id, chat.id)
            msgOther = ChatMessage("11", Date(), Constants.MESSAGE_TYPE_TEXT, "Welt 2", other.id, chat.id)

            db.upsertMessage(msgMe)
            db.upsertMessage(msgOther)





            chat = Chat("0002", "Cooler Chat 3 ohne Message")
            db.upsertChat(chat)
            chatUser1 = ChatUser(chat.id, me.id, true)
            chatUser2 = ChatUser(chat.id, other.id, false)
            db.upsertChatUser(chatUser1)
            db.upsertChatUser(chatUser2)

        }
	}
}
