package com.tigerteam.database.DbObjects

import java.time.LocalDateTime

data class Message(val id : String,
                   var timeStamp : LocalDateTime,
                   var dataType : String,
                   var data : String,
                   var senderId : String,
                   var chatId : String)