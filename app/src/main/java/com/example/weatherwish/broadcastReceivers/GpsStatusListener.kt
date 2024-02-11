package com.example.weatherwish.broadcastReceivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData

private const val TAG = "GpsStatusListener"
class GpsStatusListener(private val context: Context) : LiveData<Boolean>() {

    override fun onActive() {
        registerReceiver()
        checkGpsStatus()
    }

    override fun onInactive() {
        Log.d(TAG, "onInactive: un registering the gps state broadcast receiver")
        unRegisterReceiver()
    }

    private val gpsStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            checkGpsStatus()
        }
    }

    private fun checkGpsStatus() = if (isLocationEnabled()) {
        postValue(true)
    } else {
        postValue(false)
    }

    private fun isLocationEnabled() = context.getSystemService(LocationManager::class.java)
        .isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun registerReceiver() = context.registerReceiver(
        gpsStatusReceiver,
        IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
    )

    private fun unRegisterReceiver() = context.unregisterReceiver(gpsStatusReceiver)

}