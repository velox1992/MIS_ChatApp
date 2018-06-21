package com.tigerteam.ui.helper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.ChatItem

class ChatRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private val VIEW_TYPE_MESSAGE_SENT = 1
	private val VIEW_TYPE_MESSAGE_RECEIVED = 2

	private lateinit var itemsList : List<ChatItem>
	private val myUserId : String


	constructor(myid : String) : super()
	{
		itemsList = emptyList<ChatItem>()
		myUserId = myid
	}

	inner class ViewHolderSend(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var messageText : TextView
		var timeText : TextView

		init
		{
			messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
			timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
		}

		fun bind(item : ChatItem)
		{
			messageText.text = item.messageData
			timeText.text = item.getDateInNiceFormat()
		}
	}

	inner class ViewHolderReceive(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var messageText : TextView
		var timeText : TextView
		var nameText : TextView

		init
		{
			messageText = itemView.findViewById<View>(R.id.text_message_body) as TextView
			timeText = itemView.findViewById<View>(R.id.text_message_time) as TextView
			nameText = itemView.findViewById<View>(R.id.text_message_name) as TextView
		}

		fun bind(item : ChatItem)
		{
			messageText.text = item.messageData
			timeText.text = item.getDateInNiceFormat()
			nameText.text = item.userName
		}
	}


	/**
	 * Typ-Unterscheidung
	 */
	override fun getItemViewType(position: Int): Int {
		val message = itemsList.get(position) as ChatItem

		return if (message.userId.equals(myUserId)) {
			// If the current user is the sender of the message
			VIEW_TYPE_MESSAGE_SENT
		} else {
			// If some other user sent the message
			VIEW_TYPE_MESSAGE_RECEIVED
		}
	}


	//
	// This method will be called by the RecyclerView to obtain a ViewHolder object. It inflates
	// the view hierarchy card_layout.xml file and creates an instance of our ViewHolder class
	// initialized with the view hierarchy before returning it to the RecyclerView.
	//
	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder
	{
		var ret: RecyclerView.ViewHolder

		if (viewType === VIEW_TYPE_MESSAGE_SENT)
		{
			val view = LayoutInflater.from(viewGroup.context)
					.inflate(R.layout.item_message_sent, viewGroup, false)
			ret =  ViewHolderSend(view)
		} else
		{
			val view = LayoutInflater.from(viewGroup.context)
					.inflate(R.layout.item_message_received, viewGroup, false)
			ret = ViewHolderReceive(view)
		}


		return ret
	}

	//
	// The purpose of the onBindViewHolder() method is to populate the view hierarchy within the
	// ViewHolder object with the data to be displayed.
	//
	override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int)
	{
		val message = itemsList.get(position) as ChatItem

		when (viewHolder.getItemViewType()) {
			VIEW_TYPE_MESSAGE_SENT ->
			{
				(viewHolder as ViewHolderSend).bind(message)
			}
			VIEW_TYPE_MESSAGE_RECEIVED ->
			{
				(viewHolder as ViewHolderReceive).bind(message)
			}
		}
	}

	override fun getItemCount(): Int
	{
		return itemsList.size
	}

	public fun updateData(items : List<ChatItem>)
	{
		itemsList = items
	}
}