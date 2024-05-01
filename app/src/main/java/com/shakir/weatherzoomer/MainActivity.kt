package com.shakir.weatherzoomer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.shakir.weatherzoomer.databinding.ActivityMainBinding
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.Utils

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), ConfigUpdateListener {

    private lateinit var binding: ActivityMainBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private var firebaseRemoteConfigManager: FirebaseRemoteConfigManager? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.navigation_host_fragment)

        firebaseRemoteConfigManager = FirebaseRemoteConfigManager()
//        splashViewModel.updateAppRelatedData(appRelatedData!!)
        try {
            firebaseRemoteConfigManager!!.addConfigUpdateListener(this)
        } catch (e: Exception) {
            Utils.printErrorLog("remoteConfigManager_exception: $e")
        }

    }

    override fun onUpdate(configUpdate: ConfigUpdate) {
        Utils.printDebugLog("Updated_keys:${configUpdate.updatedKeys}")
        if (configUpdate.updatedKeys.contains("app_latest_version")) {
            firebaseRemoteConfigManager!!.firebaseRemoteConfig.activate().addOnCompleteListener {
                Utils.printDebugLog("Updated keys:${configUpdate.updatedKeys}")
                startActivity(Intent(this@MainActivity, UpdateAppActivity::class.java))
            }
        }
    }

    override fun onError(error: FirebaseRemoteConfigException) {
        Utils.printDebugLog("Config update error with code: ${error.code} | $error")
    }
}