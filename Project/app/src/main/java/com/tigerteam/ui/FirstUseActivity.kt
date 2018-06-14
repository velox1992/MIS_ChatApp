package com.tigerteam.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import com.tigerteam.mischat.ChatService
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.MainActivity
import com.tigerteam.mischat.R
import com.tigerteam.mischat.R.id.*
import kotlinx.android.synthetic.main.activity_first_use.*


class FirstUseActivity : AppCompatActivity()
{
	//----------------------------------------------------------------------------------------------
	// Overridden Methods
	//----------------------------------------------------------------------------------------------

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_first_use)
	}


	//----------------------------------------------------------------------------------------------
	// Event Handler Methods
	//----------------------------------------------------------------------------------------------

	fun btnLetsStart_onClick(view : View)
    {
		if(txbNickName.text.isEmpty())
        {
			return
		}

	    val data : Intent = Intent()
	    val userName : String = txbNickName.text.toString()

	    data.putExtra(Constants.EXTRA_USER_NAME, userName)
	    setResult(RESULT_OK, data)

		finish()
	}
}
