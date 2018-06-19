package com.tigerteam.ui.Objects

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class ChatItem(
        val userId : String
        , val userName : String
        , val messageDataType : String
        , val messageData : String
        , val messageTimeStamp : Date
)  : Serializable  {

    public fun getDateInNiceFormat() : String
    {
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        val reportDate = df.format(messageTimeStamp)

        // Print what date is today!
        return reportDate
    }
}