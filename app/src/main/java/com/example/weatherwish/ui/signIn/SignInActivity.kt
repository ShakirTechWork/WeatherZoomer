package com.example.weatherwish.ui.signIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherwish.Application
import com.example.weatherwish.MainActivity
import com.example.weatherwish.R
import com.example.weatherwish.databinding.ActivitySignInBinding
import com.example.weatherwish.exceptionHandler.CustomException
import com.example.weatherwish.exceptionHandler.ExceptionErrorCodes
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.firebase.GoogleSignInCallback
import com.example.weatherwish.firebase.GoogleSignInManager
import com.example.weatherwish.ui.signUp.SignUpActivity
import com.example.weatherwish.utils.ProgressDialog
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var signInViewModel: SignInViewModel

    private lateinit var googleSignInManager: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = (application as Application).appRepository
        signInViewModel = ViewModelProvider(
            this,
            SignInViewModelFactory(repository)
        )[SignInViewModel::class.java]

        val activityResultRegistry = this.activityResultRegistry
        googleSignInManager = GoogleSignInManager(activityResultRegistry, this, this, repository)

        attachObservers()

        attachTextChangeListeners()

        attachClickListeners()

    }

    private fun attachObservers() {
        signInViewModel.resultMutableLiveData.observe(this) {
            when (it) {
                is FirebaseResponse.Success -> {
                    if (it.data!=null) {
                        Utils.printDebugLog("Signing_User_In: Success uid: ${it.data.uid}")
                        Utils.showShortToast(this@SignInActivity, "Logged in successfully")
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Utils.printErrorLog("Signing_User_In: Failure ${getString(R.string.something_went_wrong)}")
                        ExceptionHandler.handleException(this@SignInActivity, CustomException(
                            ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                            getString(R.string.something_went_wrong)))
                    }
                    ProgressDialog.dismiss()
                }

                is FirebaseResponse.Failure -> {
                    Utils.printErrorLog("Signing_User_In: Failure ${it.exception}")
                    ProgressDialog.dismiss()
                    ExceptionHandler.handleException(this@SignInActivity, it.exception!!)
                }

                is FirebaseResponse.Loading -> {
                    Utils.printDebugLog("Signing_User_In: loading")
                    ProgressDialog.initialize(this@SignInActivity)
                    ProgressDialog.show("Signing you in")
                }
            }
        }
    }

    private fun attachClickListeners() {
        binding.btnSignIn.setOnClickListener {
            val userEmail = binding.edtUserEmail.text?.trim().toString()
            val userPassword = binding.edtUserPassword.text?.trim().toString()

            if (userEmail.isBlank()) {
                binding.tilUserEmail.error = getString(R.string.please_enter_detail)
            }

            if (userPassword.isBlank()) {
                binding.tilUserPassword.error = getString(R.string.please_enter_detail)
            }

            if (userEmail.isNotBlank() && userPassword.isNotBlank()) {
                if (Utils.isValidEmailId(userEmail)) {
                    lifecycleScope.launch {
                        signInViewModel.signInWithEmailAndPassword(userEmail, userPassword)
                    }
                } else {
                    binding.tilUserEmail.error = getString(R.string.please_enter_valid_email)
                }
            }
        }

        binding.btnSignUpWithGoogle.setOnClickListener {
            googleSignInManager.signInWithGoogleAccount(object : GoogleSignInCallback {
                override fun onSuccess() {
                    Utils.printDebugLog("signInWithGoogleAccount: Success")
                    ProgressDialog.dismiss()
                    Utils.showLongToast(this@SignInActivity, "Registration Successful")
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                }


                override fun onFailure(exception: Exception) {
                    Utils.printDebugLog("signInWithGoogleAccount: Failed")
                    ProgressDialog.dismiss()
                    ExceptionHandler.handleException(this@SignInActivity, exception)
                }

                override fun onLoading() {
                    Utils.printDebugLog("signInWithGoogleAccount: Loading")
                    ProgressDialog.initialize(this@SignInActivity)
                    ProgressDialog.show("Creating your account")
                }
            })
        }

        binding.tvSignupText.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
        }
    }

    private fun attachTextChangeListeners() {
        binding.edtUserEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "beforeTextChanged: ${p0.toString()}")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.tilUserEmail.error != null) {
                    binding.tilUserEmail.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
//                Log.d(TAG, "afterTextChanged: ${p0.toString()}")
            }

        })

        binding.edtUserPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "beforeTextChanged: ${p0.toString()}")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.tilUserPassword.error != null) {
                    binding.tilUserPassword.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
//                Log.d(TAG, "afterTextChanged: ${p0.toString()}")
            }

        })
    }

}