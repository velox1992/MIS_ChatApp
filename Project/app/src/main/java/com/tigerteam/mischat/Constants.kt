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
        const val HANDLER_CODE_NEW_CLIENT_MESSAGE = 1
        const val HANDLER_CODE_CLIENT_ROLE_DETERMINED = 2
        const val HANDLER_CODE_SERVER_ROLE_DETERMINED = 3
        const val HANDLER_CODE_REGEGISTER_RECEIVER = 4

		// Sync
		const val SYNC_SERVER_SOCKET_PORT = 7734
		const val SYNC_SERVER_SOCKET_TIMEOUT = 5000
		const val SYNC_CLIENT_SOCKET_TIMEOUT = 5000

		// Service Discovery
		const val DISCO_INSTANCE_NAME : String = "com.tigerteam.msichat"
		const val DISCO_SERVICE_TYPE : String = "com.tigerteam.msichat._tcp"
		const val DISCO_TIMESPAN : Long = 10000
	}
}
