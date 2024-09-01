package com.shakir.weatherzoomer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.shakir.weatherzoomer.adapter.UserSavedLocationsAdapter
import com.shakir.weatherzoomer.databinding.ActivityMainBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.interfaces.MainActivityInteractionListener
import com.shakir.weatherzoomer.model.UserLocationItem
import com.shakir.weatherzoomer.model.UserLocationModel
import com.shakir.weatherzoomer.ui.dashboard.DashboardFragment
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.Utils

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), MainActivityInteractionListener {

    private lateinit var binding: ActivityMainBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var firebaseRemoteConfigManager: FirebaseRemoteConfigManager

    private var userSavedLocationList: ArrayList<UserLocationItem> = arrayListOf()
    private lateinit var userSavedLocationAdapter: UserSavedLocationsAdapter

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
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding. drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                Utils.printDebugLog("addDrawerListener: onDrawerOpened")
                binding.btnAddNewLocation.setSafeOnClickListener {
                    sharedViewModel.onNewLocationRequested()
                }
                userSavedLocationList = ArrayList(
                    sharedViewModel.userData!!.user_settings.locations.map { UserLocationItem(it.key, it.value) }
                )
                Utils.printDebugLog("userSavedLocationList: $userSavedLocationList")
                userSavedLocationAdapter = UserSavedLocationsAdapter(userSavedLocationList, object : UserSavedLocationsAdapter.OnItemInteractionListener {
                    override fun onItemDeleted(position: Int, locationId: String) {
                        userSavedLocationAdapter.deleteItem(position)
                        sharedViewModel.deleteLocationAtIndex(locationId)
                    }
                    override fun onItemSelectedListener(location: String) {
                        binding.drawerLayout.close()
                        sharedViewModel.selectLocation(location)
                    }
                })
                binding.recyclerView.adapter = userSavedLocationAdapter
            }
            override fun onDrawerClosed(drawerView: View) {
                userSavedLocationList.clear()
                userSavedLocationAdapter.notifyDataSetChanged()
            }
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager.removeConfigUpdateListener()
    }

    override fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

}