package com.example.weatherwish.exceptionHandler

import android.content.Context
import android.os.DeadObjectException
import android.os.TransactionTooLargeException
import com.example.weatherwish.utils.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseException
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
import java.io.EOFException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

class ExceptionHandler2 {
    /*fun handleError(throwable: Throwable, context: Context) {
        when (throwable) {
            is FirebaseAuthInvalidUserException -> suggestSignInAgain()
            is FirebaseAuthActionCodeException -> suggestSignInAgain()
            is FirebaseAuthUserCollisionException -> //show simple message that account with this email id os already created
            is FirebaseAuthWeakPasswordException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthRecentLoginRequiredException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthEmailException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseNetworkException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthWebException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseApiNotAvailableException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthInvalidCredentialsException -> parseFirebaseExceptionMessage(throwable)
            is DeadObjectException ->
            is TransactionTooLargeException -> getErrorMessage(throwable)
            is DatabaseException -> getErrorMessage(throwable)
            is WeatherApiException -> getErrorMessage(throwable)
            is ConnectException -> //KJVKV
            is SocketException -> //KJVKV
            is SocketTimeoutException -> //KJVKV
            is MalformedURLException -> //KJVKV
            is UnknownHostException -> //KJVKV
            is UnknownServiceException -> //KJVKV
            is SSLHandshakeException -> //KJVKV
            is SSLException -> //KJVKV
            is EOFException -> //KJVKV
        }
    }

    fun handleError(exception: Exception, context: Context) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> suggestSignInAgain()
            is FirebaseAuthActionCodeException -> suggestSignInAgain()
            is FirebaseAuthUserCollisionException -> showSimpleMessage(context,"Account with this Email Id is already created.")
            is FirebaseAuthWeakPasswordException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthRecentLoginRequiredException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthEmailException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseNetworkException -> showSimpleMessage(context,"Internet connection is not available. Please check your internet connection.")
            is FirebaseAuthWebException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseApiNotAvailableException -> parseFirebaseExceptionMessage(throwable)
            is FirebaseAuthInvalidCredentialsException -> showSimpleMessage(context,"Incorrect Password or Email Id.")
            is DeadObjectException ->
            is TransactionTooLargeException -> getErrorMessage(throwable)
            is DatabaseException -> getErrorMessage(throwable)
            is WeatherApiException -> getErrorMessage(throwable)
            is ConnectException -> //KJVKV
            is SocketException -> //KJVKV
            is SocketTimeoutException -> //KJVKV
            is MalformedURLException -> //KJVKV
            is UnknownHostException -> //KJVKV
            is UnknownServiceException -> //KJVKV
            is SSLHandshakeException -> //KJVKV
            is SSLException -> //KJVKV
            is EOFException -> //KJVKV
        }
    }

    private fun parseFirebaseExceptionMessage(firebaseException: FirebaseException) {
        val message = firebaseException.localizedMessage
            ?: firebaseException.message
            ?: firebaseException.cause?.localizedMessage
            ?: firebaseException.cause?.message
            ?: "Something went wrong."
    }

    private fun showSimpleMessage(context: Context, message: String) {

    }

    private fun showDialog(context: Context, message: String, isEnableRetry: Boolean) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.cancel()
            }
        if (isEnableRetry) {
            dialogBuilder.setNegativeButton("Retry") { dialog, _ ->
                //TODO: do required operation here
                dialog.cancel()
            }
        }
        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }*/

}
