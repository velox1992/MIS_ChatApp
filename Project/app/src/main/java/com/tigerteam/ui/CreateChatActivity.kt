package com.tigerteam.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.LiveFolders
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.tigerteam.database.DbObjects.Chat
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.R
import com.tigerteam.ui.Objects.CreateChatContact
import com.tigerteam.ui.helper.CreateChatRecyclerAdapter
import kotlinx.android.synthetic.main.activity_create_chat.*
import kotlinx.android.synthetic.main.activity_first_use.*
import java.io.Serializable
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager


class CreateChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var contacts : List<CreateChatContact>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_chat)


        val extras = intent.extras

        if(extras == null)
        {
            Log.e("CreateChatActivity", "Error: Started without Extras in Intent! ")
            return
        }

        val obj = extras.get(Constants.EXTRA_CHAT_USERS)
        if(obj == null)
        {
            Log.e("CreateChatActivity", "Error: Missing Data in Extras! ")
        }

        contacts = (obj as ArrayList<CreateChatContact>).toList()


        viewManager = LinearLayoutManager(this)

        viewAdapter = CreateChatRecyclerAdapter(contacts)

        val context = this

        recyclerView = findViewById<RecyclerView>(R.id.recViewContacts).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

            // vertical Dividing-Lines
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }



        /*//Blende Tastatur bei Focus lost weg
        var editTextChatName = findViewById<EditText>(R.id.editTextChatName)
        editTextChatName.setOnFocusChangeListener( object : View.OnFocusChangeListener
        {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (v!!.getId() == R.id.editTextChatName && !hasFocus) {

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)

                }
            }
        })*/
    }



    fun createChatButtonClick(view: View)
    {
        var selectedContacts = contacts.filter { it.isSelected }

        val chatName = editTextChatName.text.toString()

        if(chatName.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.noChatNameError), Toast.LENGTH_LONG).show()
        }
        else if (selectedContacts.size == 0) {
            Toast.makeText(this, getString(R.string.atLeastOneContactError), Toast.LENGTH_LONG).show()
        }
        else {
            // alles ok

            val data : Intent = Intent()
            data.putExtra(Constants.EXTRA_CHAT_NAME, chatName)
            data.putExtra(Constants.EXTRA_CHAT_USERS, ArrayList(selectedContacts))
            setResult(RESULT_OK, data)

            finish()
        }
    }
}
