package com.shakir.weatherzoomer.exceptionHandling

import com.shakir.weatherzoomer.exceptionHandling.exceptions.AppException
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.database.DatabaseException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.shakir.weatherzoomer.response.ErrorResponse
import okhttp3.internal.connection.RouteException
import okhttp3.internal.http2.StreamResetException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object ExceptionHandler {

    fun handleException(errorResponse: ErrorResponse): String {
        return if (errorResponse.exception != null) {
            returnAppException(errorResponse.exception!!).messageString ?: "Something went wrong."
        } else {
            "Something went wrong."
        }
    }

    private fun returnAppException(e: Exception): AppException {
        e.printStackTrace()
        return when (e) {
            is DatabaseException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is FirebaseNetworkException -> {
                AppException(
                    "No internet connection"
                )
            }
            is FirebaseTooManyRequestsException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is FirebaseApiNotAvailableException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is FirebaseRemoteConfigException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is HttpException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is UnknownHostException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is SocketTimeoutException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is StreamResetException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is RouteException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is ConnectException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is ProtocolException -> {
                AppException(
                    "Something went wrong. May be an internet issue."
                )
            }
            is SSLException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is IOException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is JsonSyntaxException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is JsonParseException -> {
                AppException(
                    "Something went wrong."
                )
            }
            is GoogleGenerativeAIException -> {
                AppException(
                    "Something went wrong."
                )
            }
            else -> {
                AppException(
                    "Unexpected error occurred."
                )
            }
        }
    }

    private fun returnAppException(errorCode: Int): AppException {
        return when (errorCode) {
            AppErrorCodes.API_KEY_NOT_PROVIDED -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.PARAMETER_Q_NOT_PROVIDED -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.INVALID_API_REQUEST_URL -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.NO_LOCATION_FOUND -> {
                AppException(
                    "Location not found!"
                )
            }
            AppErrorCodes.INVALID_API_KEY -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.EXCEEDED_CALLS_PER_MONTH_QUOTA -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.API_KEY_IS_DISABLED -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.API_KEY_NOT_HAVE_ACCESS -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.INVALID_JSON_BODY_IN_BULK_REQUEST -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.TOO_MANY_LOCATIONS_IN_BULK_REQUEST -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.INTERNAL_APPLICATION_ERROR -> {
                AppException(
                    "Something went wrong!"
                )
            }
            AppErrorCodes.USER_NOT_FOUND -> {
                AppException(
                    messageString = "Please login again."
                )
            }
            else -> {
                AppException(
                    "Something went wrong!"
                )
            }
        }
    }
}
