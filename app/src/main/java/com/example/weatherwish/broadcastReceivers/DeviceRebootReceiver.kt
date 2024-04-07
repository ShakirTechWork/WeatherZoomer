package com.example.weatherwish.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.weatherwish.workManager.WeatherUpdatesWorker
import java.util.concurrent.TimeUnit

class DeviceRebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("WEATHER_WISH_LOG", "device_reboot_completed_and_again_setting_periodic_weather_updates")
//            val weatherWorkRequest = PeriodicWorkRequest.Builder(WeatherUpdatesWorker::class.java, 15,
//                TimeUnit.MINUTES).build()
//            WorkManager.getInstance(context!!).enqueue(weatherWorkRequest)
        }
    }
}
