package com.tigerteam.mischat


import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.content.BroadcastReceiver
import android.net.wifi.p2p.WifiP2pManager
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.widget.ArrayAdapter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.os.*
import android.view.Menu
import com.tigerteam.communication.WiFiDirectBroadcastReceiver
import java.util.*
import android.R.menu
import android.view.MenuInflater
import android.view.MenuItem


class MainActivity : AppCompatActivity()
{

    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // GUI-Events mit Methoden verbinden
        //BtnDiscover.setOnClickListener { discover() }
        //BtnConnect.setOnClickListener{ connect() }


        /*
        // Listen Adapter und Verbindung mit List View
        peersAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                peers)
        //ListViewPeers.adapter = peersAdapater
        */

        /*
        ListViewPeers.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                connect(position)
            }
        }
        */


    }







/*

*/





}
