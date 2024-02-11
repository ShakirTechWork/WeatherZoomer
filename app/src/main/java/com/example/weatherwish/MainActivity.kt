package com.example.weatherwish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.example.weatherwish.databinding.ActivityMainBinding

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.navigation_host_fragment)

    }
}