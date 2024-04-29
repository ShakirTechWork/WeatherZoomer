package com.shakir.weatherzoomer.ui.updateApp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shakir.weatherzoomer.databinding.ActivityUpdateAppBinding
import com.shakir.weatherzoomer.utils.Utils

class UpdateAppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpdateApp.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${this.packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                Utils.showShortToast(this@UpdateAppActivity, "Please update the app from Play Store!!!")
            }
        }

    }
}