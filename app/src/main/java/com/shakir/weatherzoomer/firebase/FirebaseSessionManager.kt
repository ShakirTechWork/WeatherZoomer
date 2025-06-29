package com.shakir.weatherzoomer.firebase

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object FirebaseSessionManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("FirebaseAuth", "Signed in anonymously as: ${user?.uid}")
                    } else {
                        Log.e("FirebaseAuth", "Anonymous sign-in failed", task.exception)
                    }
                }
        } else {
            Log.d("FirebaseAuth", "User already signed in: ${auth.currentUser?.uid}")
        }
    }

    fun linkGoogleAccount(activity: Activity, idToken: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("519620655091-pccssobbeded1frouj0h64smta168djf.apps.googleusercontent.com")
            .requestEmail().build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Anonymous account upgraded to Google account")
                } else {
                    Log.e("FirebaseAuth", "Upgrade failed", task.exception)
                }
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun isUserAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: false
    }

    fun signOut() {
        auth.signOut()
        Log.d("FirebaseAuth", "User signed out")
    }
}
