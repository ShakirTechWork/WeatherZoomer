package com.shakir.weatherzoomer.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.databinding.ActivityLoginBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.response.AppResponse
import com.shakir.weatherzoomer.userSession.UserSessionManagerOld
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory((application as Application).appRepository)
        )[LoginViewModel::class.java]

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                UserSessionManagerOld.handleGoogleSignInResult(
                    result.data,
                    onSuccess = { user ->
                        Utils.printDebugLog("uid_user_logged: ${user?.uid}")
                        Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(this, "Sign-in failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }


        binding.tvSkip.setSafeOnClickListener {
            UserSessionManagerOld.signInAnonymously(
                onSuccess = { user ->
                    Utils.printDebugLog("uid_user: ${user?.uid}")
                    Toast.makeText(this, "Signed in as Guest", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Anonymous sign-in failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnLogin.setSafeOnClickListener {
            val signInIntent = UserSessionManagerOld.getGoogleSignInIntent(this)
            googleSignInLauncher.launch(signInIntent)
        }

        attachObservers()
    }

    private fun attachObservers() {
        loginViewModel.resultLiveData.observe(this) {
            when (it) {
                is AppResponse.Success -> {
                    Utils.printDebugLog("Login_User :: Success")
                    GifProgressDialog.dismiss()
                    Utils.showLongToast(this@LoginActivity, "Registration Successful")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AppResponse.Failure -> {
                    Utils.printErrorLog("Login_User :: ")
                    GifProgressDialog.dismiss()
//                    ExceptionHandler.handleException(this@LoginActivity, it.exception!!)
//                    handleExceptions(it.exception)
                }
            }
        }
    }

}