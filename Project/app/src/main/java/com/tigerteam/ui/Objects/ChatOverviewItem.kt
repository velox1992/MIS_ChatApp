package com.tigerteam.ui.Objects

import java.util.*


/**
 * Eintrag in Übersicht aller Chats mit Daten zu letzter Nachricht
 */
data class ChatOverviewItem(
        val chatName : String
        , val chatId : String
        , val lastUserName : String?
        , val lastUserID : String?
        , val lastMessageDataType : String?
        , val lastMessageData : String?
        , val lastMessageTimeStamp : Date
) {
}