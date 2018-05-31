package com.tigerteam.mischat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.tigerteam.database.ChatDbHelper
import com.tigerteam.database.DbObjects.Parameter
import com.tigerteam.database.DbObjects.User

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
        val list = a.getAllUsers()


        a.Test()
    }
}
