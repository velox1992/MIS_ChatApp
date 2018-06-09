package com.tigerteam.mischat

import com.google.gson.Gson

class Peer(uuid: String, device_name: String) {

    private val device_name: String = device_name
    private val uuid: String = uuid
    private var isNearby: Boolean = false
    private var deviceType: DeviceType? = null

    enum class DeviceType {
        UNDEFINED,
        ANDROID,
        IPHONE
    }





    fun getDeviceName(): String {
        return device_name
    }

    fun getUuid(): String {
        return uuid
    }

    fun getDeviceType(): DeviceType? {
        return deviceType
    }

    fun setDeviceType(deviceType: DeviceType) {
        this.deviceType = deviceType
    }

    fun isNearby(): Boolean {
        return isNearby
    }

    fun setNearby(nearby: Boolean) {
        isNearby = nearby
    }


    fun create(json: String): Peer {
        return Gson().fromJson(json, Peer::class.java)
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}