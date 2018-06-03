package com.tigerteam.mischat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import com.tigerteam.database.ChatDbHelper
import com.tigerteam.database.DbObjects.*
import java.util.*
import java.util.UUID.randomUUID



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val a = ChatDbHelper(this)
        a.getParameter("Test")
        a.upsertParameter(Parameter("T1", "long", "1"))
        a.upsertParameter(Parameter("T1", "long", "1"))
        a.deleteParameter("T1")

        val u = User("015467", "Nils")
        a.upsertUser(u)
        u.name = "NilsNeu"
        a.upsertUser(u)

        var one = a.getUser("0154")

        /*for(cnt : Int in 1 until 1000)
        {
            val uTest = User(cnt.toString(), "Nils")
            a.upsertUser(uTest)
        }*/

        //a.Test()

        a.recalculateUserHash();

        val userHash = a.getUserHash();


/*
        for(cnt : Int in 1 until 1000) {

            val uuid = UUID.randomUUID()
            val str = uuid.toString()
            var time = Date((Date().time + (1000*60*60) * 0.25 * cnt).toLong())
            val mTest = ChatMessage(str, time , "String", "Hello" + cnt.toString(), "", "" )
            a.upsertMessage(mTest)
        }
*/

        /*
        var now = Date()
        var between = a.getMessagesBetween(now, Date(now.time + 1000*60*60*24))


        a.recalculateMessageHash()

        val msgHashes = a.getMessageHashes()

        */


        a.upsertChat(Chat("Nils1", "Nils 1"))
        var chat = a.getAllChats()

        a.upsertChatUser(ChatUser("Nils1", "Nils1", true))
        var chatUsers = a.getAllChatUsers()
    }
}
