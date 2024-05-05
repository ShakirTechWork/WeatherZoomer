package com.shakir.weatherzoomer.ui.splashscreen

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.constants.AppConstants
import com.shakir.weatherzoomer.databinding.ActivitySplashBinding
import com.shakir.weatherzoomer.exceptionHandler.ExceptionHandler
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.ui.signIn.SignInActivity
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.ui.walkthrough.WalkThroughActivity
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var splashViewModel: SplashViewModel

    private lateinit var firebaseRemoteConfigManager: FirebaseRemoteConfigManager

    private var isNavigatingToWeatherAPIUrl = false
    private var isAppOpenedFirstTime = false
    private var isAppUpdateNotRequired = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository

        splashViewModel =
            ViewModelProvider(this, SplashViewModelFactory(repository))[SplashViewModel::class.java]
        checkNextScreen(3000L)

        binding.tvWeatherApiAttributionText.setSafeOnClickListener {
            Utils.printErrorLog("Navigating to weatherapi.com")
            isNavigatingToWeatherAPIUrl = true
            binding.tvTapToContinue.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.Other.WEATHER_API_ATTRIBUTION_URL))
            startActivity(intent)
        }

        binding.tvTapToContinue.setSafeOnClickListener {
            isNavigatingToWeatherAPIUrl = false
            checkNextScreen(0L)
        }

    }

    private fun checkNextScreen(delay: Long) {
        lifecycleScope.launch {
            Utils.printDebugLog("action_started")
            delay(delay)
            if (!isNavigatingToWeatherAPIUrl) {
                firebaseRemoteConfigManager = FirebaseRemoteConfigManager()
                firebaseRemoteConfigManager.observeRemoteConfigData().observe(this@SplashActivity) {
                    Utils.printDebugLog("Firebase_Config SplashActivity data observed: $it")
                    for (item in it) {
                        if (item.key == "app_latest_version") {
                            isAppUpdateNotRequired = BuildConfig.VERSION_NAME == item.value
                            if (isAppUpdateNotRequired) {
                                lifecycleScope.launch {
                                    val boolean = isAppOpenedByUserFirstTime()
                                    Utils.printDebugLog("bboooo: $boolean")
                                    if (!boolean) {
                                        splashViewModel.currentLoggedInUserLiveData.observe(this@SplashActivity) {
                                            when (it) {
                                                is FirebaseResponse.Success -> {
                                                    if ((it.data != null) && it.data) {
                                                        Utils.printDebugLog("Currently_LoggedIn_User: Success (user is already logged in)")
                                                        navigate("MainActivity")
                                                    } else {
                                                        Utils.printDebugLog("Currently_LoggedIn_User: no user found")
                                                        navigate("SignInActivity")
                                                    }
                                                }

                                                is FirebaseResponse.Failure -> {
                                                    Utils.printErrorLog("Currently_LoggedIn_User: Failure ${it.exception}")
                                                    ExceptionHandler.handleException(this@SplashActivity, it.exception!!)
                                                }

                                                is FirebaseResponse.Loading -> {
                                                    Utils.printDebugLog("Currently_LoggedIn_User: Loading")
                                                }

                                            }
                                        }
                                    } else {
                                        navigate("WalkThroughActivity")
                                    }
                                }
                            } else {
                                navigate("UpdateAppActivity")
                            }
                            break
                        }
                    }
                }
            }
            Utils.printDebugLog("action_ended")
        }
    }

    private suspend fun isAppOpenedByUserFirstTime(): Boolean {
        return withContext(Dispatchers.Main) {
            splashViewModel.isAppOpenedFirstTime()
                .firstOrNull() ?: false
        }
    }

    private fun navigate(nextScreen: String) {
        if (!isNavigatingToWeatherAPIUrl) {
            Utils.printDebugLog("Navigating_to: $nextScreen")
            if (nextScreen == "SignInActivity") {
                val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                startActivity(intent)
                finish()
            } else if (nextScreen == "WalkThroughActivity") {
                val intent = Intent(this@SplashActivity, WalkThroughActivity::class.java)
                startActivity(intent)
                finish()
            } else if (nextScreen == "UpdateAppActivity"){
                startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
                finish()
            } else {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager.removeConfigUpdateListener()
    }

    private fun isAppUpdateAvailable(): Boolean {
        val appUpdateManager = AppUpdateManagerFactory.create(this@SplashActivity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        var updateAvailable = false
        appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo ->
            Utils.printDebugLog("update_the_app: ${result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE}")
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                updateAvailable = true
            }
        }.addOnFailureListener { exception ->
            // Handle failure to fetch app update info
            Log.e("AppUpdate", "Failed to fetch app update info: ${exception.message}")
        }
        return updateAvailable
    }

}