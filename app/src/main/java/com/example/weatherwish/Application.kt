package com.example.weatherwish

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.weatherwish.api.NetworkEndpoints
import com.example.weatherwish.api.RetrofitHelper
import com.example.weatherwish.datastore.AppDataStore
import com.example.weatherwish.datastore.AppDataStore2
import com.example.weatherwish.firebase.FirebaseManager
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.workManager.AppWorker
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit

class Application: Application() {

    lateinit var appRepository: AppRepository

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        TextToSpeechManager.initialize(this)
        initialize()
//        createNotificationChannel()
//        setupWorker()
    }

    private fun setupWorker() {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workerRequest = PeriodicWorkRequest.Builder(AppWorker::class.java,15, TimeUnit.MINUTES)
            .setConstraints(constraint).build()
        WorkManager.getInstance(this).enqueue(workerRequest)



    }

    private fun setupWorker2() {
        val constraint = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

        // Check if it's the first app launch
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            // Set the flag to false after the first launch
            sharedPreferences.edit().putBoolean("is_first_launch", false).apply()
        } else {
            // Create a periodic work request to repeat the API call every 6 hours
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                AppWorker::class.java,
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraint)
                .build()

            // Enqueue the periodic work request with KEEP policy
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "periodic_work_tag",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }


    private fun initialize() {
        val networkEndpoints = RetrofitHelper.getRetrofitInstance().create(NetworkEndpoints::class.java)
//        val appDataStore = AppDataStore(applicationContext)
        val appDataStore: AppDataStore = AppDataStore.getInstance(applicationContext)
        val firebaseManager = FirebaseManager()
        appRepository = AppRepository(networkEndpoints, appDataStore, firebaseManager, applicationContext)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "my_channel_id"
    }

}