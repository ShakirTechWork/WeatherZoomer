package com.example.weatherwish.ui.signUp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.DeadObjectException
import android.os.TransactionTooLargeException
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherwish.Application
import com.example.weatherwish.BuildConfig
import com.example.weatherwish.MainActivity
import com.example.weatherwish.R
import com.example.weatherwish.databinding.ActivitySignUpBinding
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.firebase.GoogleSignInCallback
import com.example.weatherwish.firebase.GoogleSignInManager
import com.example.weatherwish.model.AppRelatedData
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.ui.updateApp.UpdateAppActivity
import com.example.weatherwish.utils.ProgressDialog
import com.example.weatherwish.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthWebException
import com.google.firebase.database.DatabaseException
import kotlinx.coroutines.launch
import java.io.EOFException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

class SignUpActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var signUpViewModel: SignUpViewModel

    private lateinit var googleSignInManager: GoogleSignInManager

    private var appRelatedData: AppRelatedData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = (application as Application).appRepository
        signUpViewModel = ViewModelProvider(
            this,
            SignUpViewModelFactory(repository)
        )[SignUpViewModel::class.java]

        appRelatedData = (application as Application).appRelatedData
        if (appRelatedData != null && appRelatedData?.app_latest_version != BuildConfig.VERSION_NAME) {
            Utils.printErrorLog("New_App_Version_Available:${appRelatedData?.app_latest_version}")
            startActivity(Intent(this@SignUpActivity, UpdateAppActivity::class.java))
            finish()
        } else {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("806589059954-hl0bgnug2g90qdjqstvd0jvsapdk8f35.apps.googleusercontent.com")
                .requestEmail().build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)

            val activityResultRegistry = this.activityResultRegistry
            googleSignInManager =
                GoogleSignInManager(activityResultRegistry, this, this, repository)
            attachObservers()

            attachTextChangeListeners()

            attachClickListeners()
        }
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
//                    ExceptionHandler.handleException(this@SignUpActivity, it.exception!!)
                    handleExceptions(it.exception)
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

    private fun showSimpleMessage(message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(this@SignUpActivity)
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }

//        if (enableRetry) {
//            dialogBuilder.setNegativeButton("Retry") { dialog, _ ->
//                dialog.dismiss()
//                // Handle retry action if needed
//            }
//        }

        dialogBuilder.show()
    }

    private fun handleExceptions(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> showSimpleMessage("Something went wrong. Sign up again.")
            is FirebaseAuthActionCodeException -> showSimpleMessage("Something went wrong. Sign up again.")
            is FirebaseAuthUserCollisionException -> showSimpleMessage("Account with this Email Id is already created.")
            is FirebaseAuthWeakPasswordException -> showSimpleMessage("Enter a more strong password.")
            is FirebaseAuthRecentLoginRequiredException -> showSimpleMessage("Something went wrong. Sign up again.")
            is FirebaseAuthEmailException -> showSimpleMessage("Something went wrong. Please check your Email Id.")
            is FirebaseNetworkException -> showSimpleMessage("Internet connection is not available. Please check your internet connection.")
            is FirebaseAuthWebException -> showSimpleMessage("Something went wrong. Please try again.")
            is FirebaseApiNotAvailableException -> showSimpleMessage("Something went wrong. Please try again.")
            is FirebaseAuthInvalidCredentialsException -> showSimpleMessage("Incorrect Password or Email Id.")
            is DeadObjectException -> showSimpleMessage("Something went wrong. Please kill and reopen the app.")
            is TransactionTooLargeException -> showSimpleMessage("Something went wrong. Please try again.")
            is DatabaseException -> showSimpleMessage("Something went wrong. Please try again.")
            is ConnectException -> showSimpleMessage("Something went wrong. May be an internet problem")
            is SocketException -> showSimpleMessage("Something went wrong. May be an internet problem")
            is SocketTimeoutException -> showSimpleMessage("Something went wrong. May be an internet problem")
            is MalformedURLException -> showSimpleMessage("Something went wrong.")
            is UnknownHostException -> showSimpleMessage("Internet connection is not available. Please check your internet connection.")
            is UnknownServiceException -> showSimpleMessage("Something went wrong.")
            is SSLHandshakeException -> showSimpleMessage("Something went wrong.")
            is SSLException -> showSimpleMessage("Something went wrong.")
            is EOFException -> showSimpleMessage("Something went wrong.")
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
                        if (Utils.isInternetAvailable(this@SignUpActivity)) {
                            createUserWithEmailAndPassword(userName, userEmail, userPassword)
                        } else {
                            Utils.showLongToast(this@SignUpActivity, "Please check your internet connection.")
                        }
                    } else {
                        binding.tilConfirmPassword.error =
                            getString(R.string.passwrd_confirm_passwrd_should_be_same)
                    }
                } else {
                    binding.tilUserEmail.error = getString(R.string.please_enter_valid_email)
                }
            }
        }

        binding.btnContinueWithGoogle.setOnClickListener {
            if (Utils.isInternetAvailable(this@SignUpActivity)) {
                googleSignInManager.signInWithGoogleAccount(object: GoogleSignInCallback {
                    override fun onSuccess() {
                        Utils.printDebugLog("signInWithGoogleAccount: Success")
    //                    ProgressDialog.dismiss()
                        Utils.showLongToast(this@SignUpActivity, "Registration Successful")
                        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                        finish()
                    }


                    override fun onFailure(exception: Exception) {
                        Utils.printDebugLog("signInWithGoogleAccount: Failed")
    //                    ProgressDialog.dismiss()
                        ExceptionHandler.handleException(this@SignUpActivity, exception)
                    }

                    override fun onLoading() {
                        Utils.printDebugLog("signInWithGoogleAccount: Loading")
    //                    ProgressDialog.initialize(this@SignUpActivity)
    //                    ProgressDialog.show("Creating your account")
                    }
                })
            } else {
                Utils.showLongToast(this@SignUpActivity, "Please check your internet connection.")
            }
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