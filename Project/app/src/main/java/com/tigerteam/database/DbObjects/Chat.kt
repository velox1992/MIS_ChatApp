package com.tigerteam.database.DbObjects

data class Chat (val id : String, var name : String)
{
	constructor(syncChat : com.tigerteam.sync.Chat)
		: this(syncChat.id, syncChat.name)
}