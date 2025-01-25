package com.shakir.weatherzoomer.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.databinding.ActivityLoginBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(repository)
        )[LoginViewModel::class.java]

        binding.btnLogin.setSafeOnClickListener {
            loginUser(binding.edtUserName.text.toString().trim())
        }

        attachObservers()
    }

    private fun attachObservers() {
        loginViewModel.resultLiveData.observe(this) {
            when (it) {
                FirebaseResponse.Loading -> {
                    Utils.printDebugLog("Login_User :: Loading")
                    GifProgressDialog.initialize(this@LoginActivity)
                    GifProgressDialog.show("Creating your account")
                }
                is FirebaseResponse.Success -> {
                    Utils.printDebugLog("Login_User :: Success")
                    GifProgressDialog.dismiss()
                    Utils.showLongToast(this@LoginActivity, "Registration Successful")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is FirebaseResponse.Failure -> {
                    Utils.printErrorLog("Login_User :: ${it.exception}")
                    GifProgressDialog.dismiss()
//                    ExceptionHandler.handleException(this@LoginActivity, it.exception!!)
//                    handleExceptions(it.exception)
                }
            }
        }
    }

    private fun loginUser(userName: String) {
        if (userName.isBlank()) {
            Utils.showLongToast(this@LoginActivity, "Please enter a user name.")
            return
        }
        loginViewModel.loginUser(userName)
    }
}