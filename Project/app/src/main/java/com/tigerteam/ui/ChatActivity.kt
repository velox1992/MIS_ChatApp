package com.tigerteam.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.helper.ChatRecyclerAdapter
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        val demoData = mutableListOf<ChatItem>()
        demoData.add(ChatItem("456", "Peter", "String", "Du Nudel!", Date()))
        demoData.add(ChatItem("12", "Ich", "String", "NEIN!", Date()))
        demoData.add(ChatItem("456", "Klaus", "String", "Haha lol!", Date()))
        demoData.add(ChatItem("12", "Ich", "String", "Selber Handtuch!", Date()))

        viewManager = LinearLayoutManager(this)

        viewAdapter = ChatRecyclerAdapter(demoData, "12")

        val context = this

        recyclerView = findViewById<RecyclerView>(R.id.reyclerview_message_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            // vertical Dividing-Lines
            //addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }
}
