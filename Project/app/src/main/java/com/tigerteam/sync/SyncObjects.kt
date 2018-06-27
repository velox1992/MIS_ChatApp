package com.tigerteam.sync

import java.io.Serializable
import java.util.*


/*
Kotlin Serializable Classes
https://stonesoupprogramming.com/2017/11/26/kotlin-serializable-classes/

Creating multiple constructors for Data classes in Kotlin
https://proandroiddev.com/creating-multiple-constructors-for-data-classes-in-kotlin-32ad27e58cac
 */


enum class DataRequestType
{
	User,
	Chat,
	Messages,
	ChatUser,
	Quit,
}

data class DataRequest(
	val request : DataRequestType) : Serializable

data class User(
	val id : String,
	val name: String,
	val phoneNumber : String?) : Serializable
{
	constructor(dbUser : com.tigerteam.database.DbObjects.User)
		: this(dbUser.id, dbUser.name, dbUser.phoneNumber)
}

data class Chat(
	val id : String,
	val name : String) : Serializable
{
	constructor(dbChat : com.tigerteam.database.DbObjects.Chat)
		: this(dbChat.id, dbChat.name)
}

data class ChatUser(
	val chatId : String,
	val userId : String,
	val isOwner: Boolean) : Serializable
{
	constructor(dbChatUser : com.tigerteam.database.DbObjects.ChatUser)
		: this(dbChatUser.chatId, dbChatUser.userId, dbChatUser.isOwner)
}

data class ChatMessage(
	val id : String,
	var timeStamp : Date,
	var dataType : String,
	var data : String,
	var senderId : String,
	var chatId : String) : Serializable
{
	constructor(dbChatMessage : com.tigerteam.database.DbObjects.ChatMessage)
		: this(
			dbChatMessage.id,
			dbChatMessage.timeStamp,
			dbChatMessage.dataType,
			dbChatMessage.data,
			dbChatMessage.senderId,
			dbChatMessage.chatId)
}