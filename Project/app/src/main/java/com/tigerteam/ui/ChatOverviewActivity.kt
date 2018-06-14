package com.tigerteam.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.tigerteam.mischat.R

import kotlinx.android.synthetic.main.activity_chat_overview.*
import kotlinx.android.synthetic.main.content_chat_overview.*

class ChatOverviewActivity : AppCompatActivity()
{
	private var layoutManager : RecyclerView.LayoutManager? = null
	private var adapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null


	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat_overview)
		setSupportActionBar(toolbar)

		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
		}

		layoutManager = LinearLayoutManager(this)
		recycler_view.layoutManager = layoutManager

		adapter = RecyclerAdapter()
		recycler_view.adapter = adapter


	}

}
