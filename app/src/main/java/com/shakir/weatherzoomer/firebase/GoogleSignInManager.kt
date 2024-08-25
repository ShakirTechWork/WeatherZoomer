package com.shakir.weatherzoomer.firebase

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shakir.weatherzoomer.exceptionHandler.CustomException
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleSignInManager(private val activityResultRegistry: ActivityResultRegistry, private val lifeCycleOwner: LifecycleOwner, private val activity: Activity, private val appRepository: AppRepository) {
    private var googleSignInClient: GoogleSignInClient
    private lateinit var callback: GoogleSignInCallback
    private val launcher: ActivityResultLauncher<Intent> =
        activityResultRegistry.register("key", lifeCycleOwner, ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                Utils.printDebugLog("activityResultRegistry result: $result")
                Utils.printDebugLog("activityResultRegistry result.resultCode: ${result.resultCode}")
                Utils.printDebugLog("activityResultRegistry result.data: ${result.data}")
                Utils.printDebugLog("activityResultRegistry Activity.RESULT_OK: ${Activity.RESULT_OK}")
                if (result.resultCode == Activity.RESULT_OK) {
                    if (Utils.isInternetAvailable(activity)) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        Utils.printDebugLog("activityResultRegistry task: $task")
                        Utils.printDebugLog("activityResultRegistry task.isSuccessful: ${task.isSuccessful}")
                        Utils.printDebugLog("activityResultRegistry task.isComplete: ${task.isComplete}")
                        Utils.printDebugLog("activityResultRegistry task.isCanceled: ${task.isCanceled}")
                        Utils.printDebugLog("activityResultRegistry task.task.exception: ${task.exception}")
                        Utils.printDebugLog("activityResultRegistry task: ${task.exception?.stackTrace}")
                        if (task.isSuccessful) {
                            val account: GoogleSignInAccount? = task.result
                            val authCredential = GoogleAuthProvider.getCredential(account?.idToken, null)
                            lifeCycleOwner.lifecycleScope.launch {
                                val authResult = appRepository.signInWithGoogleAccount(authCredential)
                                when (authResult) {
                                    is FirebaseResponse.Success -> {
                                        val data = authResult.data
                                        if (data != null) {
                                            val token = data.user?.getIdToken(true)?.await()?.token
                                            if (!token.isNullOrBlank()) {
                                                Utils.printDebugLog("Adding_User_In_DB :: Loading")
                                                val userName = data.user!!.displayName
                                                val userEmail = data.user!!.email
                                                if (!userName.isNullOrBlank() && !userEmail.isNullOrBlank()) {
                                                    val response = addUserIntoFirebase(userName, userEmail)
                                                    if (response is FirebaseResponse.Success) {
                                                        Utils.printDebugLog("Adding_User_In_DB :: Success")
                                                        callback.onSuccess()
                                                    } else if (response is FirebaseResponse.Failure) {
                                                        Utils.printDebugLog("exception: ${response.exception}")
                                                        Utils.printDebugLog("Adding_User_In_DB :: Failure")
                                                        callback.onFailure(Exception("Something went wrong"))
                                                    }
                                                } else {
                                                    callback.onFailure(Exception("User name or email id is not present."))
                                                }
                                            }
                                        } else {
                                            callback.onFailure(Exception("Something went wrong"))
                                        }
                                    }
                                    is FirebaseResponse.Failure -> callback.onFailure(Exception("Something went wrong"))
                                    is FirebaseResponse.Loading -> callback.onLoading()
                                }
                            }
                        }
                    } else {
                        callback.onFailure(CustomException("9999999", "Please check your internet."))
                    }
                } else {
                    callback.onFailure(Exception("Sign-in cancelled or failed"))
                }
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("519620655091-pccssobbeded1frouj0h64smta168djf.apps.googleusercontent.com")
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun signInWithGoogleAccount(callback: GoogleSignInCallback) {
        this.callback = callback
        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    private suspend fun addUserIntoFirebase(name: String, email: String): FirebaseResponse<Boolean> {
        return appRepository.addUserIntoFirebase(name, email)
    }
}