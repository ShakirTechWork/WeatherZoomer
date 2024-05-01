package com.shakir.weatherzoomer.ui.splashscreen

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.constants.AppConstants
import com.shakir.weatherzoomer.databinding.ActivitySplashBinding
import com.shakir.weatherzoomer.exceptionHandler.ExceptionHandler
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.AppRelatedData
import com.shakir.weatherzoomer.ui.signIn.SignInActivity
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.ui.walkthrough.WalkThroughActivity
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity(), ConfigUpdateListener {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var splashViewModel: SplashViewModel

    private var appRelatedData: AppRelatedData? = null
    private var firebaseRemoteConfigManager: FirebaseRemoteConfigManager? = null

    private var isNavigatingToWeatherAPIUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository

        splashViewModel =
            ViewModelProvider(this, SplashViewModelFactory(repository))[SplashViewModel::class.java]
        appRelatedData = (application as Application).appRelatedData
        firebaseRemoteConfigManager = FirebaseRemoteConfigManager()
//        splashViewModel.updateAppRelatedData(appRelatedData!!)
        try {
            firebaseRemoteConfigManager!!.addConfigUpdateListener(this)
        } catch (e: Exception) {
            Utils.printErrorLog("remoteConfigManager_exception: $e")
        }
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

    private fun getAppRelatedData() {
        CoroutineScope(Dispatchers.IO).launch {
            Utils.printDebugLog("getAppRelatedData:: Loading")
//            val data = appRepository.getAppRelatedData()
//            when (data) {
//                is FirebaseResponse.Success -> {
//                    if (data.data != null) {
//                        appRelatedData = data.data
//                        if (appRelatedData != null) {
//                            Utils.printDebugLog("getAppRelatedData:: Success | App_version: $appRelatedData")
//                        } else {
//                            Utils.printDebugLog("getAppRelatedData:: Sucess | but got null")
//                        }
//                    }
//                }
//                is FirebaseResponse.Failure -> {
//                    Utils.printDebugLog("getAppRelatedData:: Failed | exception: ${data.exception}")
//                    appRelatedData = null
//                }
//                FirebaseResponse.Loading -> {}
//            }
        }
    }

    private fun checkNextScreen(delay: Long) {
        lifecycleScope.launch {
            Utils.printErrorLog("start")
            delay(delay) // 3 seconds delay for splash screen
            Utils.printErrorLog("end")
            /*if (appRelatedData?.app_latest_version != BuildConfig.VERSION_NAME) {
                Utils.printErrorLog("New_App_Version_Available:${appRelatedData?.app_latest_version}")
                startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
                finish()
            } else {
                splashViewModel.isAppOpenedFirstTime().asLiveData().observe(this@SplashActivity) {
                    if (it) {
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
            }*/
            splashViewModel.isAppOpenedFirstTime().asLiveData().observe(this@SplashActivity) {
                if (it) {
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
            } else {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onUpdate(configUpdate: ConfigUpdate) {
        Utils.printDebugLog("1234 Updated_keys:${configUpdate.updatedKeys}")
        if (configUpdate.updatedKeys.contains("app_latest_version")) {
            firebaseRemoteConfigManager!!.firebaseRemoteConfig.activate().addOnCompleteListener {
                Utils.printDebugLog("Updated keys:${configUpdate.updatedKeys}")
                startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
            }
        }
    }

    override fun onError(error: FirebaseRemoteConfigException) {
        Utils.printDebugLog("Config update error with code: ${error.code} | $error")
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager?.removeConfigUpdateListener()
        firebaseRemoteConfigManager = null
    }

}