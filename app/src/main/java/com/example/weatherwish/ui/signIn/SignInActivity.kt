package com.example.weatherwish.ui.signIn

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherwish.Application
import com.example.weatherwish.BuildConfig
import com.example.weatherwish.MainActivity
import com.example.weatherwish.R
import com.example.weatherwish.databinding.ActivitySignInBinding
import com.example.weatherwish.exceptionHandler.CustomException
import com.example.weatherwish.exceptionHandler.ExceptionErrorCodes
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.extensionFunctions.setSafeOnClickListener
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.firebase.GoogleSignInCallback
import com.example.weatherwish.firebase.GoogleSignInManager
import com.example.weatherwish.model.AppRelatedData
import com.example.weatherwish.ui.signUp.SignUpActivity
import com.example.weatherwish.ui.updateApp.UpdateAppActivity
import com.example.weatherwish.utils.GifProgressDialog
import com.example.weatherwish.utils.Utils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var signInViewModel: SignInViewModel

    private lateinit var googleSignInManager: GoogleSignInManager

    private var appRelatedData: AppRelatedData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = (application as Application).appRepository
        signInViewModel = ViewModelProvider(
            this,
            SignInViewModelFactory(repository)
        )[SignInViewModel::class.java]

        appRelatedData = (application as Application).appRelatedData
        if (appRelatedData != null && appRelatedData?.app_latest_version != BuildConfig.VERSION_NAME) {
            Utils.printErrorLog("New_App_Version_Available:${appRelatedData?.app_latest_version}")
            startActivity(Intent(this@SignInActivity, UpdateAppActivity::class.java))
            finish()
        } else {
            val activityResultRegistry = this.activityResultRegistry
            googleSignInManager = GoogleSignInManager(activityResultRegistry, this, this, repository)

            attachObservers()

            attachTextChangeListeners()

            attachClickListeners()
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
                        Utils.printDebugLog("signInWithGoogleAccount: Failed")
                        GifProgressDialog.dismiss()
                        ExceptionHandler.handleException(this@SignInActivity, exception)
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

}