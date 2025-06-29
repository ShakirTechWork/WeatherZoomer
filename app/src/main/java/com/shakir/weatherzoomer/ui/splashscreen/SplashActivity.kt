package com.shakir.weatherzoomer.ui.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.databinding.ActivitySplashBinding
import com.shakir.weatherzoomer.ui.login.LoginActivity
import com.shakir.weatherzoomer.ui.walkthrough.WalkThroughActivity
import com.shakir.weatherzoomer.utils.Utils

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository

        splashViewModel =
            ViewModelProvider(this, SplashViewModelFactory(repository))[SplashViewModel::class.java]

        Utils.printDebugLog("app_started")
        splashViewModel.nextScreenLiveData.observe(this) {
            val intent = when (it) {
                NextScreen.WALK_THROUGH_ACTIVITY -> {
                    Intent(this@SplashActivity, WalkThroughActivity::class.java)
                }
                NextScreen.LOGIN_ACTIVITY -> {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }
                NextScreen.MAIN_ACTIVITY -> {
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
            }
            startActivity(intent)
            finish()
        }
        splashViewModel.decideNextScreen()
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