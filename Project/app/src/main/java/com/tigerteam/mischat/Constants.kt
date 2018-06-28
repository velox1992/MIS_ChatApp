package com.tigerteam.mischat

//
// Where Should I Keep My Constants in Kotlin?
// https://blog.egorand.me/where-do-i-put-my-constants-in-kotlin/
//
class Constants
{
	companion object
	{
		//Text-Nachrichten-Typ
		const val MESSAGE_TYPE_TEXT = "text"




		const val EXTRA_USER_NAME : String = "user_name"

		const val PARAM_OWN_USER_ID : String = "own_user_id"



		// Create-Chat

		const  val EXTRA_CHAT_NAME : String = "chat_name"
		const  val EXTRA_CHAT_USERS : String = "chat_users"

		// Chat
		const  val EXTRA_CHAT_ITEMS : String = "chat_items"
		const  val EXTRA_OWN_USER_ID : String = "own_user_id"
		const  val EXTRA_CHAT_TITLE : String = "chat_title"
		const  val EXTRA_CHAT_ID : String  = "chat_id"

		// Communication
        const val EXTRA_DEVICES : String = "discovered_devices"

		// Sync
		const val SYNC_SERVER_SOCKET_PORT = 7734

		// Intents
		const val WIFI_CONNECT_TO_DEVICE : String = "wifi_connect_to_device"
		const val EXTRA_SELECTED_WIFI_DEVICE : String = "selected_wifi_device"
	}
}
