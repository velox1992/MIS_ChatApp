package com.tigerteam.mischat

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*
import com.tigerteam.database.*
import com.tigerteam.database.DbObjects.*
import com.tigerteam.ui.Objects.CreateChatContact
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import com.tigerteam.ui.Objects.Contact
import android.provider.ContactsContract
import android.content.ContentResolver
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.Objects.ChatOverviewItem


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
	}

	override fun onDestroy()
	{
		Log.i(TAG, "ChatService onDestroy")
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
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
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
		val onlyKnownContacts = true // true bei Test, false bei echt

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
	public fun sendChatTextMessage(chatId : String, text : String)
	{
		try{
			val ownUserId = getOwnUserId()
			if(ownUserId != null) {
				val msgID = UUID.randomUUID().toString();
				val msg = ChatMessage(msgID, Date(), Constants.MESSAGE_TYPE_TEXT, text,ownUserId , chatId)

				db.upsertMessage(msg)
			}
			else
			{
				Log.e(TAG, "sendChatMessage => missing own userId")
			}
		}
		catch (e : Exception)
		{
			Log.e(TAG, "sendChatMessage => " + e.toString())
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
