package com.shakir.weatherzoomer.exceptionHandler

object AppErrorCode {

    object WeatherApiCodes {
        const val API_KEY_NOT_PROVIDED = 1002
        const val PARAMETER_Q_NOT_PROVIDED = 1003
        const val INVALID_API_REQUEST_URL = 1005
        const val NO_LOCATION_FOUND = 1006
        const val INVALID_API_KEY = 2006
        const val EXCEEDED_CALLS_PER_MONTH_QUOTA = 2007
        const val API_KEY_IS_DISABLED = 2008
        const val API_KEY_NOT_HAVE_ACCESS = 2009
        const val INVALID_JSON_BODY_IN_BULK_REQUEST = 9000
        const val TOO_MANY_LOCATIONS_IN_BULK_REQUEST = 9001
        const val INTERNAL_APPLICATION_ERROR = 9999
    }

    object ErrorCodes {
        // Common error codes
        const val GENERIC_ERROR = 1000
        const val NETWORK_ERROR = 1001

        // Firebase Authentication error codes
        const val FIREBASE_AUTH_INVALID_USER = 2000
        const val FIREBASE_AUTH_INVALID_CREDENTIALS = 2001

        // Firebase Realtime Database error codes
        const val FIREBASE_DATABASE_ERROR = 3000

        // Third-party Weather API error codes
        const val WEATHER_API_INVALID_API_KEY = 4000
        const val WEATHER_API_INVALID_PARAMETERS = 4001
        // Add more weather API error codes as needed
    }

    object HttpStatusCodes {
        const val UNAUTHORIZED = 401
        const val BAD_REQUEST = 400
        const val FORBIDDEN = 403
        // Add more HTTP status codes as needed
    }

}