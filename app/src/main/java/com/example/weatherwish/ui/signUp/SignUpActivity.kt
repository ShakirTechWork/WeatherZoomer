package com.example.weatherwish.ui.signUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.weatherwish.Application
import com.example.weatherwish.R
import com.example.weatherwish.databinding.ActivitySignUpBinding
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.utils.ProgressDialog
import com.example.weatherwish.utils.Utils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SignUpActivity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var signUpViewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = (application as Application).appRepository
        signUpViewModel = ViewModelProvider(
            this,
            SignUpViewModelFactory(repository)
        )[SignUpViewModel::class.java]

        attachObservers()

        attachTextChangeListeners()

        attachClickListeners()
    }

    private fun attachObservers() {
        signUpViewModel.resultMutableLiveData.observe(this) {
            when (it) {
                is FirebaseResponse.Success -> {
                    Utils.printDebugLog("Signup_User :: Success")
                    ProgressDialog.dismiss()
                    Utils.showLongToast(this@SignUpActivity, "Registration Successful")
                    startActivity(Intent(this, SignInActivity::class.java))
                    finish()
                }
                is FirebaseResponse.Failure -> {
                    Utils.printErrorLog("Signup_User :: ${it.exception}")
                    ProgressDialog.dismiss()
                    ExceptionHandler.handleException(this@SignUpActivity, it.exception!!)
                }
                is FirebaseResponse.Loading -> {
                    Utils.printDebugLog("Signup_User :: Loading")
                    ProgressDialog.initialize(this@SignUpActivity)
                    ProgressDialog.show("Creating your account")
                }
                else -> {
                    Utils.printErrorLog("Signup_User :: No Status Found")
                }
            }
        }
    }

    private fun attachClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val userName = binding.edtUserName.text?.trim().toString()
            val userEmail = binding.edtUserEmail.text?.trim().toString()
            val userPassword = binding.edtUserPassword.text?.trim().toString()
            val userConfirmPassword = binding.edtConfirmPassword.text?.trim().toString()

            if (userName.isBlank()) {
                binding.tilUserName.error = getString(R.string.please_enter_detail)
            }

            if (userEmail.isBlank()) {
                binding.tilUserEmail.error = getString(R.string.please_enter_detail)
            }

            if (userPassword.isBlank()) {
                binding.tilUserPassword.error = getString(R.string.please_enter_detail)
            }

            if (userConfirmPassword.isBlank()) {
                binding.tilConfirmPassword.error = getString(R.string.please_enter_detail)
            }

            if (userName.isNotBlank() && userEmail.isNotBlank() &&
                userPassword.isNotBlank() && userConfirmPassword.isNotBlank()
            ) {
                if (Utils.isValidEmailId(userEmail)) {
                    if (userPassword == userConfirmPassword) {
                        createUserWithEmailAndPassword(userName, userEmail, userPassword)
                    } else {
                        binding.tilConfirmPassword.error =
                            getString(R.string.passwrd_confirm_passwrd_should_be_same)
                    }
                } else {
                    binding.tilUserEmail.error = getString(R.string.please_enter_valid_email)
                }
            }
        }

        binding.btnSignUpWithGoogle.setOnClickListener {

        }

        binding.tvLoginText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun attachTextChangeListeners() {
        binding.edtUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "beforeTextChanged: ${p0.toString()}")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.tilUserName.error != null) {
                    binding.tilUserName.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
//                Log.d(TAG, "afterTextChanged: ${p0.toString()}")
            }

        })

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

        binding.edtConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "beforeTextChanged: ${p0.toString()}")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.tilConfirmPassword.error != null) {
                    binding.tilConfirmPassword.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
//                Log.d(TAG, "afterTextChanged: ${p0.toString()}")
            }

        })

    }

    private fun createUserWithEmailAndPassword(userName: String, userEmail: String, userPassword: String) {
        lifecycleScope.launch {
            signUpViewModel.createUserWithEmailAndPassword(
                userName,
                userEmail,
                userPassword
            )
        }
    }

}