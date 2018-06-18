package com.tigerteam.mischat

//
// Where Should I Keep My Constants in Kotlin?
// https://blog.egorand.me/where-do-i-put-my-constants-in-kotlin/
//
class Constants
{
	companion object
	{
		const val EXTRA_USER_NAME : String = "user_name"

		const val PARAM_OWN_USER_ID : String = "own_user_id"



		// Create-Chat

		const  val EXTRA_CHAT_NAME : String = "chat_name"
		const  val EXTRA_CHAT_USERS : String = "chat_users"
	}
}