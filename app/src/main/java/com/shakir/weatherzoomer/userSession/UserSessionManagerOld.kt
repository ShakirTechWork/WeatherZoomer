package com.shakir.weatherzoomer.userSession

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

object UserSessionManagerOld {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun signInAnonymously(onSuccess: (FirebaseUser?) -> Unit, onFailure: (Exception) -> Unit) {
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(firebaseAuth.currentUser)
                    } else {
                        onFailure(task.exception ?: Exception("Anonymous sign-in failed"))
                    }
                }
        } else {
            onSuccess(firebaseAuth.currentUser)
        }
    }

    fun getGoogleSignInIntent(context: Context): Intent {
        return buildGoogleSignInClient(context).signInIntent
    }

    fun handleGoogleSignInResult(
        data: Intent?,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                signInWithGoogle(idToken, onSuccess, onFailure)
            } ?: throw ApiException(Status.RESULT_CANCELED)
        } catch (e: ApiException) {
            onFailure(Exception("Google Sign-In failed: ${e.statusCode}", e))
        }
    }

    private fun signInWithGoogle(idToken: String, onSuccess: (FirebaseUser?) -> Unit, onFailure: (Exception) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = firebaseAuth.currentUser

        if (user?.isAnonymous == true) {
            // Link anonymous account with Google
            user.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(task.result?.user)
                    } else {
                        onFailure(task.exception ?: Exception("Failed to link anonymous account with Google"))
                    }
                }
        } else {
            // Regular Google Sign-In
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(task.result?.user)
                    } else {
                        onFailure(task.exception ?: Exception("Google authentication failed"))
                    }
                }
        }
    }

    fun logout(context: Context) {
        firebaseAuth.signOut()
        buildGoogleSignInClient(context).signOut()
    }

    private fun buildGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("519620655091-pccssobbeded1frouj0h64smta168djf.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context.applicationContext, gso)
    }
}
