package com.example.weatherwish.exceptionHandler

import android.os.DeadObjectException
import android.os.TransactionTooLargeException
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
    fun categorizeErrorType(throwable: Throwable) {
        when (throwable) {
            is FirebaseAuthInvalidUserException -> handleFirebaseException(throwable)
            is FirebaseAuthActionCodeException -> getErrorMessage(throwable)
            is FirebaseAuthUserCollisionException -> getErrorMessage(throwable)
            is FirebaseAuthWeakPasswordException -> getErrorMessage(throwable)
            is FirebaseAuthRecentLoginRequiredException -> getErrorMessage(throwable)
            is FirebaseAuthEmailException -> getErrorMessage(throwable)
            is FirebaseNetworkException -> getErrorMessage(throwable)
            is FirebaseAuthWebException -> getErrorMessage(throwable)
            is FirebaseApiNotAvailableException -> getErrorMessage(throwable)
            is FirebaseAuthInvalidCredentialsException -> getErrorMessage(throwable)
            is DeadObjectException -> getErrorMessage(throwable)
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

}
