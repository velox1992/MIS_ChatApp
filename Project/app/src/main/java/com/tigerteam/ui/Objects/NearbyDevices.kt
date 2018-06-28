package com.tigerteam.ui.Objects

import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.tigerteam.mischat.Constants
import com.tigerteam.mischat.R
import kotlinx.android.synthetic.main.activity_nearby_devices.*

class NearbyDevices : AppCompatActivity() {

    var nearbyDevicesAdapater : ArrayAdapter<WifiP2pDevice>? = null
    var nearbyDevices = ArrayList<WifiP2pDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_devices)

        // OnClick - Event in der Liste
        ListViewDiscoveredDevices.onItemClickListener = object : AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var mConnectToDeviceIntent = Intent()
                mConnectToDeviceIntent.setAction(Constants.WIFI_CONNECT_TO_DEVICE)

                var mDevice = nearbyDevices[position]
                mConnectToDeviceIntent.putExtra(Constants.EXTRA_SELECTED_WIFI_DEVICE,mDevice)

                sendBroadcast(mConnectToDeviceIntent)
            }
        }

        val extras = intent.extras
        val obj = extras.get(Constants.EXTRA_DEVICES)
        nearbyDevices = obj as ArrayList<WifiP2pDevice>

        nearbyDevicesAdapater = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                nearbyDevices)

        ListViewDiscoveredDevices.adapter = nearbyDevicesAdapater



    }
}
