package com.shakir.weatherzoomer.firebase

import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.utils.Utils

class FirebaseRemoteConfigManager() {

    val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    var configUpdateListener: ConfigUpdateListener? = null

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        fetchAndActivateConfig()
    }

    private fun fetchAndActivateConfig() {
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.printErrorLog("fetchAndActivateConfig success")
                } else {
                    Utils.printErrorLog("fetchAndActivateConfig failed")
                }
            }
    }

    fun addConfigUpdateListener(listener: ConfigUpdateListener) {
        configUpdateListener = listener
        firebaseRemoteConfig.addOnConfigUpdateListener(listener)
    }

    // Method to remove config update listener
    fun removeConfigUpdateListener() {
        Utils.printDebugLog("removing_the_listener")
        configUpdateListener = null
    }
}