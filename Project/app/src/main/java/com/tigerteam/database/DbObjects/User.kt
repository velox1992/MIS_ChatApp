package com.tigerteam.database.DbObjects

data class User (val id : String, var name: String, var phoneNumber : String?)
{
	constructor(syncUser : com.tigerteam.sync.User)
		: this(syncUser.id, syncUser.name, syncUser.phoneNumber)
}