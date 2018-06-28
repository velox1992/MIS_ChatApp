package com.tigerteam.database.DbObjects

data class ChatUser (val chatId : String, val userId : String, val isOwner: Boolean)
{
	constructor(syncChatUser : com.tigerteam.sync.ChatUser)
		: this(syncChatUser.chatId, syncChatUser.userId, syncChatUser.isOwner)
}