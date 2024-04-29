package com.shakir.weatherzoomer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.shakir.weatherzoomer.constants.AppEnum
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "Utils"

object Utils {

    val permission = Manifest.permission.ACCESS_FINE_LOCATION
    val granted = PackageManager.PERMISSION_GRANTED

    fun checkFineLocationPermission(context: Context): Boolean {
        Log.d(
            TAG, "checkFineLocationPermission: ${
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == granted
            }"
        )
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == granted
    }

    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        val boolean = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        printErrorLog("internet: $boolean")
        if (!boolean) {
            printErrorLog("No internet connection")
        }
        return boolean
    }

    fun singleOptionAlertDialog(
        context: Context,
        title: String,
        msg: String,
        optionName: String,
        isCancelable: Boolean,
        positiveMethod: () -> Unit = {}
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)

        // set message of alert dialog
        dialogBuilder.setMessage(msg)

            // if the dialog is cancelable
            .setCancelable(isCancelable)
            // positive button text and action
            .setPositiveButton(optionName) { dialog, id ->
                dialog.cancel()
                positiveMethod()
            }

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(title)
        // show alert dialog
        alert.show()
    }

    fun twoOptionAlertDialog(
        context: Context,
        title: String,
        msg: String,
        positiveText: String,
        negativeText: String,
        isCancelable: Boolean,
        positiveMethod: () -> Unit = {},
        negativeMethod: () -> Unit = {}
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setMessage(msg)
            .setCancelable(isCancelable)
            .setPositiveButton(positiveText) { dialog, id ->
                dialog.cancel()
                positiveMethod()
            }
            .setNegativeButton(negativeText) { dialog, id ->
                dialog.cancel()
                negativeMethod()
            }
        val alert = dialogBuilder.create()
        alert.setTitle(title)
        alert.show()
    }

    fun changeMode(appEnum: AppEnum) {
        when (appEnum) {
            AppEnum.DARKMODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            AppEnum.LIGHTMODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    fun convertToHourTime(unixTime: Long): String {
        val date = Date(unixTime * 1000L) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("h a", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertUnixToHourTime(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("hh:mm a")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date(timestamp))
    }

    fun convertUnixToHourTime2(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("hh:mm a")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date(timestamp))
    }

    fun generateStringFromUrl(url: String): String {
        val parts = url.split("/")
        val timeOfDay = if ("day" in parts) "day" else "night"
        val code = parts.last().removeSuffix(".png")
        return "$timeOfDay$code"
    }

    fun convertUnixTimeToDate(unixTime: Long): String {
        val date = Date(unixTime * 1000L) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertUnixTimeToDayName(unixTime: Long): String {
        val date = Date(unixTime * 1000L) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertUnixTimeToFormattedDayAndDate(unixTime: Long): String {
        val date = Date(unixTime * 1000L) // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    fun convertTimestampToReadableTime(timestamp: String): String {
        //converts this type of time "2024-04-15T22:00:00+00:00" to "15 Apr 2024 10:00 PM"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        return outputFormat.format(date!!)
    }

    fun isValidEmailId(email: String): Boolean {
        // Define a regex pattern to match valid Gmail email addresses
        val pattern = "^[a-zA-Z0-9._%+-]+@gmail\\.com$".toRegex()
        // Return true if the email matches the pattern, otherwise false
        return email.matches(pattern)
    }

    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun showShortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun printDebugLog(message: String) {
//        if (BuildConfig.DEBUG) {
            Log.d("WEATHER_WISH_LOG:", message)
//        }
    }

    fun printErrorLog(message: String) {
//        if (BuildConfig.DEBUG) {
            Log.e("WEATHER_WISH_LOG:", message)
//        }
    }

}