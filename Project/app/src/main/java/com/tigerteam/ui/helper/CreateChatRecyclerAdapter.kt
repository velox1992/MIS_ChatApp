package com.tigerteam.ui.helper

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.CreateChatContact

class CreateChatRecyclerAdapter : RecyclerView.Adapter<CreateChatRecyclerAdapter.ViewHolder>{
    private lateinit var itemsList : List<CreateChatContact>

    constructor(items : List<CreateChatContact>) : super()
    {
        itemsList = items
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var txtUserName : TextView
        var txtNameContact : TextView
        var txtPhoneNumber : TextView
        var cbxSelected : CheckBox

        init
        {
            txtUserName = itemView.findViewById(R.id.txtUserName)
            txtNameContact = itemView.findViewById(R.id.txtNameContact)
            txtPhoneNumber = itemView.findViewById(R.id.txtNumber)
            cbxSelected = itemView.findViewById(R.id.cbxSelect)

            val delegat = {v : View->
                if (itemsList.get(adapterPosition).isSelected) {
                    cbxSelected.setChecked(false)
                    itemsList.get(adapterPosition).isSelected = false
                }
                else {
                    cbxSelected.setChecked(true)
                    itemsList.get(adapterPosition).isSelected = true
                }
            }

            // bei Klick das Selected umsetzen
            // beide, da sonst beim Click auf die Checkbox nicht das Event auf der itemView ausgelöst wird
            itemView.setOnClickListener(delegat)
            cbxSelected.setOnClickListener(delegat)
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
                .inflate(R.layout.create_chat_row, viewGroup, false)

        return ViewHolder(v)
    }

    //
    // The purpose of the onBindViewHolder() method is to populate the view hierarchy within the
    // ViewHolder object with the data to be displayed.
    //
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int)
    {
        viewHolder.txtUserName.text = "(" + itemsList[i].userName + ")"
        viewHolder.txtNameContact.text = itemsList[i].nameInContacts
        viewHolder.txtPhoneNumber.text = itemsList[i].phoneNumber
        viewHolder.cbxSelected.isChecked = itemsList[i].isSelected
    }

    override fun getItemCount(): Int
    {
        return itemsList.size
    }
}