package com.shakir.weatherzoomer.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.util.Util
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.ConfigUpdateListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.model.RemoteConfigItem
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.Utils

class FirebaseRemoteConfigManager {
    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            // Set minimum fetch interval
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3000)
                .build()
            setConfigSettingsAsync(configSettings)
        }
    }

    @Volatile
    var configUpdateListener: ConfigUpdateListener? = null
    private var configUpdateListenerRegistration: ConfigUpdateListenerRegistration? = null

    private val remoteConfigMLiveData: MutableLiveData<ArrayList<RemoteConfigItem>> = MutableLiveData()

    init {
        // Fetch and activate Remote Config data initially
        fetchAndActivateRemoteConfigData()
    }

    fun observeRemoteConfigData(): LiveData<ArrayList<RemoteConfigItem>> {
        return remoteConfigMLiveData
    }

    private fun addOnConfigChangedListener() {
        Utils.printDebugLog("Firebase_Config: addOnConfigChangedListener: attached")
        configUpdateListener = object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Utils.printDebugLog("Firebase_Config: addOnConfigUpdateListener: onUpdate called")
                val remoteConfigItemList = ArrayList<RemoteConfigItem>()
                for (key in configUpdate.updatedKeys) {
                    Utils.printDebugLog("aaa: ${configUpdate.updatedKeys.contains("app_latest_version")}")
                    if (configUpdate.updatedKeys.contains("app_latest_version")) {
                        firebaseRemoteConfig.activate().addOnCompleteListener {
                            val value = firebaseRemoteConfig.getString(key)
                            Utils.printDebugLog("value: $value")
                            val remoteConfigItem = RemoteConfigItem(key, value)
                            remoteConfigItemList.add(remoteConfigItem)
                            remoteConfigMLiveData.postValue(remoteConfigItemList)
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Utils.printDebugLog("Firebase_Config: addOnConfigUpdateListener: onError called")
            }

        }
        configUpdateListenerRegistration = firebaseRemoteConfig.addOnConfigUpdateListener(configUpdateListener!!)
    }

    /*private fun fetchAndActivateRemoteConfigData() {
        Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Loading")
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Success")
                    // Config fetched and activated successfully, notify observers with updated data
                    val remoteConfigItemList = ArrayList<RemoteConfigItem>()
                    for ((key, value) in firebaseRemoteConfig.all) {
                        val stringValue = value.asString() // Assuming values are strings
                        remoteConfigItemList.add(RemoteConfigItem(key, stringValue))
                    }
                    remoteConfigMLiveData.postValue(remoteConfigItemList)
                    addOnConfigChangedListener()
                } else {
                    Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Failed")
                }
            }
    }*/

    private fun fetchAndActivateRemoteConfigData() {
        Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Loading")
        firebaseRemoteConfig.fetch()
            .addOnCompleteListener { fetchTask ->
                if (fetchTask.isSuccessful) {
                    Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Success")
                    firebaseRemoteConfig.activate().addOnCompleteListener { activationTask ->
                        if (activationTask.isSuccessful) {
                            Utils.printDebugLog("Firebase_Config: applyFetchedValues: Activation success")
                            val remoteConfigItemList = ArrayList<RemoteConfigItem>()
                            for ((key, value) in firebaseRemoteConfig.all) {
                                val stringValue = value.asString()
                                remoteConfigItemList.add(RemoteConfigItem(key, stringValue))
                            }
                            remoteConfigMLiveData.postValue(remoteConfigItemList)
                            addOnConfigChangedListener()
                        } else {
                            Utils.printDebugLog("Firebase_Config: applyFetchedValues: Activation failed")
                        }
                    }
                } else {
                    Utils.printDebugLog("Firebase_Config: fetchAndActivateRemoteConfigData: Failed")
                }
            }
    }


    fun onRemoteConfigDataUpdated(arrayList: ArrayList<RemoteConfigItem>, context: Context) {
        Utils.printDebugLog("app__latest__version: ${firebaseRemoteConfig.getString("app_latest_version")}")
//        if (arrayList["app_latest_version") {
//            if (BuildConfig.VERSION_NAME != firebaseRemoteConfig.getString("app_latest_version")) {
//                val intent = Intent(context, UpdateAppActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//                context.startActivity(intent)
//                (context as Activity).finish()
//            }
//        }
    }

    // Method to remove config update listener
    fun removeConfigUpdateListener() {
        Utils.printDebugLog("Firebase_Config: removeConfigUpdateListener")
        configUpdateListener?.let {
            configUpdateListenerRegistration?.remove()
            configUpdateListener = null
            configUpdateListenerRegistration = null
        }
    }
}