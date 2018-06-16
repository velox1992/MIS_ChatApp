package com.tigerteam.ui.Objects

import java.util.*

data class ChatItem(
        val userId : String
        , val userName : String
        , val messageDataType : String
        , val messageData : String
        , val messageTimeStamp : Date
) {
}