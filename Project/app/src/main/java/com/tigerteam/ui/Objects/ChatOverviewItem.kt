package com.tigerteam.ui.Objects

import java.util.*
import java.text.SimpleDateFormat


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

    public fun getDateInNiceFormat() : String
    {
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        val reportDate = df.format(lastMessageTimeStamp)

        // Print what date is today!
        return reportDate
    }
}