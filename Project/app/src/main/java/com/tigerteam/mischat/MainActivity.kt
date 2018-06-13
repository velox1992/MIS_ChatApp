package com.tigerteam.mischat

import android.content.*
import android.net.wifi.p2p.WifiP2pManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.os.IBinder
import java.util.*
import edu.rit.se.wifibuddy.WifiDirectHandler
import android.support.v4.content.LocalBroadcastManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.support.coreutils.R.id.async
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import edu.rit.se.wifibuddy.CommunicationManager
import edu.rit.se.wifibuddy.DnsSdService
import edu.rit.se.wifibuddy.DnsSdTxtRecord
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {



    internal var node: Node? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sendFrameButton.setOnClickListener{ sendFrames() }

        node = Node(this)
    }

    override fun onStart() {
        super.onStart()
        node!!.start()
    }

    override fun onStop() {
        super.onStop()

        if (node != null)
            node!!.stop()
    }




    private val started = false

    fun sendFrames() {
        /*if(!started)
		{
			started = true;
			node = new Node(this);
			node.start();
			return;
		}*/

        /*
        node!!.broadcastFrame(ByteArray(1))

        for (i in 0..1999) {
            val frameData = ByteArray(1024)
            Random().nextBytes(frameData)

            node!!.broadcastFrame(frameData)
        }
        */
        var hMsg = NewMessageEditText.text.toString()
        var hMsgByteArray = hMsg.toByteArray(Charsets.UTF_8)
        node!!.broadcastFrame(hMsgByteArray)
        /*for(int i = 0; i < 100; ++i)
		{
			byte[] frameData = new byte[100 * 1024];
			new Random().nextBytes(frameData);

			node.broadcastFrame(frameData);
		}*/
    }

    fun refreshPeers() {
        peersTextView!!.setText(node!!.links.size.toString() +  " connected")
    }

    fun refreshFrames() {
        framesTextView!!.setText(node!!.framesCount.toString() + " frames")
    }

    fun updateMessage(message : String) {
        MessageTextView.setText(message)
    }




}
