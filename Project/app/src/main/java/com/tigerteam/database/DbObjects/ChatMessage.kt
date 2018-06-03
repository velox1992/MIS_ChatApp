package com.tigerteam.database.DbObjects

import java.util.*

data class ChatMessage(val id : String,
                   var timeStamp : Date,
                   var dataType : String,
                   var data : String,
                   var senderId : String,
                   var chatId : String)