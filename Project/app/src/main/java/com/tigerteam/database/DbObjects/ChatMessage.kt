package com.tigerteam.database.DbObjects

import java.util.*

data class ChatMessage(
	val id : String,
	var timeStamp : Date,
	var dataType : String,
	var data : String,
	var senderId : String,
	var chatId : String)
{
	constructor(syncChatMessage : com.tigerteam.sync.ChatMessage)
		: this(
			syncChatMessage.id,
			syncChatMessage.timeStamp,
			syncChatMessage.dataType,
			syncChatMessage.data,
			syncChatMessage.senderId,
			syncChatMessage.chatId)
}