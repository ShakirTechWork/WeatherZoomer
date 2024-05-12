package com.shakir.weatherzoomer.ui.signIn

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.databinding.ActivitySignInBinding
import com.shakir.weatherzoomer.exceptionHandler.CustomException
import com.shakir.weatherzoomer.exceptionHandler.ExceptionErrorCodes
import com.shakir.weatherzoomer.exceptionHandler.ExceptionHandler
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.firebase.GoogleSignInCallback
import com.shakir.weatherzoomer.firebase.GoogleSignInManager
import com.shakir.weatherzoomer.model.AppRelatedData
import com.shakir.weatherzoomer.ui.signUp.SignUpActivity
import com.shakir.weatherzoomer.ui.updateApp.UpdateAppActivity
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.shakir.weatherzoomer.constants.AppConstants
import com.shakir.weatherzoomer.firebase.FirebaseRemoteConfigManager
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var signInViewModel: SignInViewModel

    private lateinit var googleSignInManager: GoogleSignInManager

    private lateinit var firebaseRemoteConfigManager: FirebaseRemoteConfigManager

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

        firebaseRemoteConfigManager = FirebaseRemoteConfigManager()
        firebaseRemoteConfigManager.observeRemoteConfigData().observe(this@SignInActivity) {
            Utils.printDebugLog("Firebase_Config SplashActivity data observed: $it")
            for (item in it) {
                if (item.key == "app_latest_version") {
                    if (BuildConfig.VERSION_NAME != item.value) {
                        startActivity(Intent(this@SignInActivity, UpdateAppActivity::class.java))
                        finish()
                    }
                    break
                }
            }
            for (item in it) {
                if (item.key == "privacy_policy_url") {
                    signInViewModel.setPrivacyUrl(item.value)
                    break
                }
            }
        }

        attachObservers()

        attachTextChangeListeners()

        attachClickListeners()

        binding.chkboxPrivacyPolicy.setOnCheckedChangeListener { _, isChecked ->
            signInViewModel.setPrivacyCheckBoxChecked(isChecked)
        }

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
                    GifProgressDialog.dismiss()
                }

                is FirebaseResponse.Failure -> {
                    Utils.printErrorLog("Signing_User_In: Failure ${it.exception}")
                    GifProgressDialog.dismiss()
                    ExceptionHandler.handleException(this@SignInActivity, it.exception!!)
                }

                is FirebaseResponse.Loading -> {
                    Utils.printDebugLog("Signing_User_In: loading")
                    GifProgressDialog.initialize(this@SignInActivity)
                    GifProgressDialog.show("Signing you in")
                }
            }
        }
    }

    private fun attachClickListeners() {
        binding.btnSignIn.setSafeOnClickListener {
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
                    if (Utils.isInternetAvailable(this@SignInActivity)) {
                        lifecycleScope.launch {
                            signInViewModel.signInWithEmailAndPassword(userEmail, userPassword)
                        }
                    } else {
                        Utils.showLongToast(this@SignInActivity, "Please check your internet connection.")
                    }
                } else {
                    binding.tilUserEmail.error = getString(R.string.please_enter_valid_email)
                }
            }
        }

        binding.tvForgotPassword.setSafeOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_manual_location_dialog, null)
            // Determine theme mode (light/dark)
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            // Set background drawable based on theme mode
            val drawableResId = if (isDarkMode) R.drawable.dialog_background_dark_mode else R.drawable.dialog_background_light_mode
            dialogView.setBackgroundResource(drawableResId)
            val builder = AlertDialog.Builder(this)
            val dialog = builder.setView(dialogView).create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val textTitle = dialogView.findViewById<TextView>(R.id.tv_title)
            val edtTxtInputEmailId = dialogView.findViewById<TextInputEditText>(R.id.text_input_edit_text)
            val btnApply = dialogView.findViewById<Button>(R.id.btn_apply)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            edtTxtInputEmailId.hint = "Type your email id"
            btnApply.text = "Reset"
            textTitle.text = "Type your email id"
            btnApply.setOnClickListener {
                val emailId = edtTxtInputEmailId.text.toString().trim()
                if (emailId.isNotBlank()) {
                    if (Utils.isInternetAvailable(this@SignInActivity)) {
                        sendPasswordResetEmail(emailId)
                        dialog.dismiss()
                    } else {
                        Utils.showLongToast(this@SignInActivity, "Please check your internet connection.")
                    }
                } else {
                    Utils.showLongToast(this@SignInActivity, "Please enter the email id first.")
                }
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.btnContinueWithGoogle.setSafeOnClickListener {
            if (signInViewModel.privacyCheckBoxLiveData.value!!) {
                if (Utils.isInternetAvailable(this@SignInActivity)) {
                    googleSignInManager.signInWithGoogleAccount(object : GoogleSignInCallback {
                        override fun onSuccess() {
                            Utils.printDebugLog("signInWithGoogleAccount: Success")
                            GifProgressDialog.dismiss()
                            Utils.showLongToast(this@SignInActivity, "Registration Successful")
                            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                            finish()
                        }


                        override fun onFailure(exception: Exception) {
                            Utils.printDebugLog("signInWithGoogleAccount: Failed ${exception.message}")
                            GifProgressDialog.dismiss()
                            if (exception.message != "Sign-in cancelled or failed") {
                                ExceptionHandler.handleException(this@SignInActivity, exception)
                            }
                        }

                        override fun onLoading() {
                            Utils.printDebugLog("signInWithGoogleAccount: Loading")
                            GifProgressDialog.initialize(this@SignInActivity)
                            GifProgressDialog.show("Creating your account")
                        }
                    })
                } else {
                    Utils.showLongToast(this@SignInActivity, "Please check your internet connection.")
                }
            } else {
                Utils.showLongToast(this@SignInActivity, "Please accept the privacy policy.")
            }
        }

        binding.tvPrivacyPolicy.setSafeOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(signInViewModel.privacyCheckUrlLiveData.value))
            startActivity(intent)
        }

        binding.tvSignupText.setSafeOnClickListener {
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

    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Utils.singleOptionAlertDialog(this@SignInActivity, "Email sent", "Password reset email sent successfully.", "Okay", true)
                } else {
                    Utils.singleOptionAlertDialog(
                        this@SignInActivity,
                        "Email sent",
                        "Failed to send password reset email.",
                        "Okay",
                        true
                    )
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRemoteConfigManager.removeConfigUpdateListener()
    }


}