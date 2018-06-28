package com.tigerteam.ui.helper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.ChatOverviewItem
import java.text.DateFormat
import java.util.*
import java.text.SimpleDateFormat

class ChatOverviewRecyclerAdapter : RecyclerView.Adapter<ChatOverviewRecyclerAdapter.ViewHolder>
{
	private lateinit var itemsList : List<ChatOverviewItem>
	private lateinit var clickListener: IChatOverviewItemClickListener


	constructor(items : List<ChatOverviewItem>, clickListener: IChatOverviewItemClickListener) : super()
	{
		itemsList = items
		this.clickListener = clickListener
	}

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var itemChatName : TextView
		var itemDateTime : TextView
		var itemLastChatMessage : TextView

		init
		{
			itemChatName = itemView.findViewById(R.id.lblChatName)
			itemDateTime = itemView.findViewById(R.id.lblDateTime)
			itemLastChatMessage = itemView.findViewById(R.id.lblLastChatMessage)

			itemView.setOnClickListener { v : View ->
				var position : Int = getAdapterPosition()
				val item = itemsList[position]

				clickListener.clickedChat(item.chatId, item.chatName)
			}
		}
	}

	//
	// This method will be called by the RecyclerView to obtain a ViewHolder object. It inflates
	// the view hierarchy card_layout.xml file and creates an instance of our ViewHolder class
	// initialized with the view hierarchy before returning it to the RecyclerView.
	//
	override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder
	{
		val v = LayoutInflater
				.from(viewGroup.context)
				.inflate(R.layout.item_chat_overview, viewGroup, false)

		return ViewHolder(v)
	}

	//
	// The purpose of the onBindViewHolder() method is to populate the view hierarchy within the
	// ViewHolder object with the data to be displayed.
	//
	override fun onBindViewHolder(viewHolder: ViewHolder, i: Int)
	{
		val item = itemsList[i];

		// Format the ChatName field
		viewHolder.itemChatName.text = item.chatName

		// Format the Date/Time field
		if(item.lastUserID.isNullOrBlank())
		{
			viewHolder.itemDateTime.text = ""
		}
		else
		{
			var lastMessageCalender = Calendar.getInstance()
			lastMessageCalender.time = item.lastMessageTimeStamp

			var currentCalender = Calendar.getInstance()
			val sameDate =
				lastMessageCalender.get(Calendar.YEAR) == currentCalender.get(Calendar.YEAR) &&
				lastMessageCalender.get(Calendar.MONTH) == currentCalender.get(Calendar.MONTH) &&
				lastMessageCalender.get(Calendar.DAY_OF_YEAR) == currentCalender.get(Calendar.DAY_OF_YEAR);

			var formatter: DateFormat
			if(sameDate)
			{
				formatter = SimpleDateFormat("HH:mm")
			}
			else
			{
				formatter = SimpleDateFormat("dd.MM.yyyy")
			}
			viewHolder.itemDateTime.text = formatter.format(item.lastMessageTimeStamp)
		}

		// Format the Message field
		if(item.lastUserID.isNullOrBlank())
		{
			viewHolder.itemLastChatMessage.text = "Noch keine Nachrichten vorhanden."
		}
		else
		{
			viewHolder.itemLastChatMessage.text = "${item.lastUserName}: ${item.lastMessageData}"
		}
	}

	override fun getItemCount(): Int
	{
		return itemsList.size
	}
}