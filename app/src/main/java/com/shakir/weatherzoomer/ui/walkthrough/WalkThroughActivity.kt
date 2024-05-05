package com.shakir.weatherzoomer.ui.walkthrough

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.adapter.WalkthroughAdapter
import com.shakir.weatherzoomer.databinding.ActivityWalkThroughBinding
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import com.shakir.weatherzoomer.ui.signIn.SignInActivity
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.Utils

class WalkThroughActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalkThroughBinding
    private lateinit var walkThroughViewModel: WalkThroughViewModel

    private lateinit var walkthroughAdapter: WalkthroughAdapter

    private lateinit var firebaseRemoteConfigManager: FirebaseRemoteConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkThroughBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = (application as Application).appRepository

        walkThroughViewModel = ViewModelProvider(this, WalkThroughViewModelFactory(repository))[WalkThroughViewModel::class.java]

        walkThroughViewModel.signOutCurrentUser(this@WalkThroughActivity)

        firebaseRemoteConfigManager = FirebaseRemoteConfigManager()
        firebaseRemoteConfigManager.observeRemoteConfigData().observe(this@WalkThroughActivity) {
            Utils.printDebugLog("Firebase_Config WalkThroughActivity data observed: $it")
            for (item in it) {
                if (item.key == "app_latest_version") {
                    if (BuildConfig.VERSION_NAME != item.value) {
                        startActivity(Intent(this@WalkThroughActivity, UpdateAppActivity::class.java))
                        finish()
                    }
                    break
                }
            }
        }

        walkthroughAdapter = WalkthroughAdapter(this, getWalkthroughPages())
        binding.viewPager.adapter = walkthroughAdapter
        setupIndicator()
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        binding.tvPrevious.setOnClickListener {
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem -= 1
            }
        }

        binding.tvNext.setOnClickListener {
            if (binding.viewPager.currentItem < walkthroughAdapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            }
        }

        binding.tvStart.setOnClickListener {
            walkThroughViewModel.updateIsAppOpenedFirstTime(false)
            val intent = Intent(this@WalkThroughActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupIndicator() {
        val indicators = arrayOfNulls<TextView>(walkthroughAdapter.itemCount)
        val margin = resources.getDimensionPixelSize(R.dimen.indicator_margin)

        for (i in indicators.indices) {
            indicators[i] = TextView(this)
            indicators[i]?.apply {
                text = "•"
                textSize = 50f
                setTextColor(resources.getColor(R.color.color_primary_light))

                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(margin, 0, margin, 0)
                this@apply.layoutParams = layoutParams  // Use this@apply to refer to the outer apply
            }
            binding.indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        if (index==0) {
            binding.tvPrevious.visibility = View.GONE
        } else {
            binding.tvPrevious.visibility = View.VISIBLE
        }
        val childCount = binding.indicatorLayout.childCount
        if (index==childCount-1) {
            binding.tvNext.visibility = View.GONE
            binding.tvStart.visibility = View.VISIBLE
        } else {
            binding.tvStart.visibility = View.GONE
            binding.tvNext.visibility = View.VISIBLE
        }
        for (i in 0 until childCount) {
            val indicator = binding.indicatorLayout.getChildAt(i) as TextView
            if (i == index) {
                indicator.setTextColor(resources.getColor(R.color.color_primary_light))
            } else {
                indicator.setTextColor(resources.getColor(R.color.dark_gray))
            }
        }
    }

    private fun getWalkthroughPages(): List<Fragment> {
        // Create and return your walkthrough fragments here
        return listOf(
            WalkthroughPageFragment(
                R.drawable.get_weather_update_0,
                "Welcome to WeatherZoomer App! Access tailored weather data",
                "Real-time updates for any location."
            ),
            WalkthroughPageFragment(
                R.drawable.get_weather_update_2,
                "Monitor air quality and get advice.",
                "Track chances of snowfall and rainfall"
            ),
            WalkthroughPageFragment(R.drawable.get_weather_updates_1,"Never miss sunrise, sunset, and lunar phases.", "Stay aware of humidity, wind speed and UV status"),
            WalkthroughPageFragment(
                R.drawable.get_weather_updates_3,
                "Plan ahead with 3-day forecasts.",
                "Receive warnings for potential hazards"
            ),
            WalkthroughPageFragment(
                R.drawable.get_weather_updates_4,
                "Utilise an AI-based day planner based on current weather.",
                "Enjoy seamless experience with our user-friendly interface"
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager.removeConfigUpdateListener()
    }

}