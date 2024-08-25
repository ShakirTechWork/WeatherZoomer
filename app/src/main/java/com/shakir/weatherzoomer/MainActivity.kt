package com.shakir.weatherzoomer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shakir.weatherzoomer.adapter.DrawerItemAdapter
import com.shakir.weatherzoomer.databinding.ActivityMainBinding
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.Utils

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var firebaseRemoteConfigManager: FirebaseRemoteConfigManager

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
        firebaseRemoteConfigManager.observeRemoteConfigData().observe(this@MainActivity) {
            Utils.printDebugLog("Firebase_Config SplashActivity data observed: $it")
            for (item in it) {
                if (item.key == "app_latest_version") {
                    if (BuildConfig.VERSION_NAME != item.value) {
                        startActivity(Intent(this@MainActivity, UpdateAppActivity::class.java))
                        finish()
                    }
                    break
                }
            }
            for (item in it) {
                if (item.key == "app_play_store_link") {
                    sharedViewModel.appPlayStoreLink = item.value
                }
            }
            for (item in it) {
                if (item.key == "privacy_policy_url") {
                    sharedViewModel.privacyPolicyUrl = item.value
                }
            }
        }

        val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        val adapter = DrawerItemAdapter(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

    }

    // Method to open the drawer from a fragment
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager.removeConfigUpdateListener()
    }
}