package com.example.weatherwish.ui.splashscreen

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.weatherwish.Application
import com.example.weatherwish.MainActivity
import com.example.weatherwish.R
import com.example.weatherwish.constants.AppConstants
import com.example.weatherwish.databinding.ActivitySignInBinding
import com.example.weatherwish.databinding.ActivitySplashBinding
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.extensionFunctions.setSafeOnClickListener
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.ui.walkthrough.WalkThroughActivity
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var splashViewModel: SplashViewModel

    private var isNavigatingToWeatherAPIUrl = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository

        splashViewModel =
            ViewModelProvider(this, SplashViewModelFactory(repository))[SplashViewModel::class.java]

        checkNextScreen()

        binding.tvWeatherApiAttributionText.setSafeOnClickListener {
            Utils.printErrorLog("Navigating to weatherapi.com")
            isNavigatingToWeatherAPIUrl = true
            binding.tvTapToContinue.visibility = View.VISIBLE
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.Other.WEATHER_API_ATTRIBUTION_URL))
            startActivity(intent)
        }

        binding.tvTapToContinue.setSafeOnClickListener {
            isNavigatingToWeatherAPIUrl = false
            checkNextScreen()
        }

    }

    private fun checkNextScreen() {
        splashViewModel.isAppOpenedFirstTime().asLiveData().observe(this@SplashActivity) {
            if (it) {
                splashViewModel.currentLoggedInUserLiveData.observe(this) {
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

    private fun navigate(nextScreen: String) {
        lifecycleScope.launch {
            delay(1000) // 2 seconds delay for splash screen
            if (!isNavigatingToWeatherAPIUrl) {
                Utils.printDebugLog("Navigating_to: ${nextScreen}")
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
    }
}