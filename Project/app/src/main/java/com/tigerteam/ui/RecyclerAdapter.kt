package com.tigerteam.ui

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.tigerteam.mischat.R

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>()
{
	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
	{
		var itemImage: ImageView
		var itemTitle: TextView
		var itemDetail: TextView

		init
		{
			itemImage = itemView.findViewById(R.id.item_image)
			itemTitle = itemView.findViewById(R.id.item_title)
			itemDetail = itemView.findViewById(R.id.item_detail)

			itemView.setOnClickListener{ v : View->
				var position: Int = getAdapterPosition()

				Snackbar.make(v, "Click detected on item $position",
						Snackbar.LENGTH_LONG).setAction("Action", null).show()
			}
		}
	}


	private val titles = arrayOf("Chapter One",
			"Chapter Two", "Chapter Three", "Chapter Four",
			"Chapter Five", "Chapter Six", "Chapter Seven",
			"Chapter Eight")

	private val details = arrayOf("Item one details", "Item two details",
			"Item three details", "Item four details",
			"Item five details", "Item six details",
			"Item seven details", "Item eight details")

//	private val images = intArrayOf(
//			R.drawable.android_image_1,
//			R.drawable.android_image_2,
//			R.drawable.android_image_3,
//			R.drawable.android_image_4,
//			R.drawable.android_image_5,
//			R.drawable.android_image_6,
//			R.drawable.android_image_7,
//			R.drawable.android_image_8)

	//
	// This method will be called by the RecyclerView to obtain a ViewHolder object. It inflates
	// the view hierarchy card_layout.xml file and creates an instance of our ViewHolder class
	// initialized with the view hierarchy before returning it to the RecyclerView.
	//
	override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder
	{
		val v = LayoutInflater
				.from(viewGroup.context)
				.inflate(R.layout.card_chat_overview_layout, viewGroup, false)

		return ViewHolder(v)
	}

	//
	// The purpose of the onBindViewHolder() method is to populate the view hierarchy within the
	// ViewHolder object with the data to be displayed.
	//
	override fun onBindViewHolder(viewHolder: ViewHolder, i: Int)
	{
		viewHolder.itemTitle.text = titles[i]
		viewHolder.itemDetail.text = details[i]
		//viewHolder.itemImage.setImageResource(images[i])
	}

	override fun getItemCount(): Int
	{
		return titles.size
	}
}