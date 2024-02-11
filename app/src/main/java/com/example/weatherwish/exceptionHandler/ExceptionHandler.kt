package com.example.weatherwish.exceptionHandler

import android.content.Context
import com.example.weatherwish.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

object ExceptionHandler {

    fun handleException(context: Context, exception: Exception) {
        val (errorCode, message) = when (exception) {
            is CustomException -> Pair(ExceptionErrorCodes.UNKNOWN_EXCEPTION,context.getString(R.string.something_went_wrong))
            is FirebaseException -> handleFirebaseException(exception)
            is IOException -> handleNetworkException(exception)
//            is DatastoreException -> Pair("sgd","Retrofit error occurred")
            else -> Pair(ExceptionErrorCodes.UNKNOWN_EXCEPTION,context.getString(R.string.something_went_wrong))
        }

        showDialog(context, message)
    }

    private fun handleFirebaseException(firebaseException: FirebaseException): Pair<String, String> {
        return when (firebaseException) {
            is FirebaseAuthWeakPasswordException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnFirebaseExceptionMessage(firebaseException))
            is FirebaseAuthInvalidCredentialsException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnFirebaseExceptionMessage(firebaseException))
            is FirebaseAuthException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnFirebaseExceptionMessage(firebaseException))
            is FirebaseNetworkException -> Pair(ExceptionErrorCodes.NETWORK_EXCEPTION, returnFirebaseExceptionMessage(firebaseException))
            else -> Pair(ExceptionErrorCodes.FIREBASE_UNKNOWN_EXCEPTION, returnFirebaseExceptionMessage(firebaseException))
        }
    }

    private fun handleNetworkException(ioException: IOException): Pair<String, String> {
        return when (ioException) {
            is ConnectException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is SocketException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is SocketTimeoutException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is MalformedURLException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is UnknownHostException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is UnknownServiceException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is SSLHandshakeException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is SSLException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            is EOFException -> Pair(ExceptionErrorCodes.FIREBASE_EXCEPTION, returnIOExceptionMessage(ioException))
            else -> Pair(
                ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                returnIOExceptionMessage(ioException)
            )
        }
    }

    private fun showDialog(context: Context, message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog, id ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Alert")
        alert.show()
    }

    private fun returnFirebaseExceptionMessage(firebaseException: FirebaseException): String {
        return firebaseException.localizedMessage
            ?: firebaseException.message
            ?: firebaseException.cause?.localizedMessage
            ?: firebaseException.cause?.message
            ?: "Something went wrong."
    }

    private fun returnIOExceptionMessage(ioException: IOException): String {
        return ioException.localizedMessage
            ?: ioException.message
            ?: ioException.cause?.localizedMessage
            ?: ioException.cause?.message
            ?: "Something went wrong."
    }

}


