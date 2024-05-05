package com.shakir.weatherzoomer.broadcastReceivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.TextToSpeechManager
import com.shakir.weatherzoomer.api.NetworkEndpoints
import com.shakir.weatherzoomer.firebase.FirebaseManager
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.WeatherData
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherUpdateReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "weather_channel"
    private val NOTIFICATION_ID = 1

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient())
            .build()
    }

    private val firebaseManager = FirebaseManager()

    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val networkEndpoints: NetworkEndpoints by lazy {
        retrofit.create(NetworkEndpoints::class.java)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            Log.d("WEATHER_ZOOMER_LOG", "onReceive_called ")

            if (networkInfo != null && networkInfo.isConnected) {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentlySignedInUser = firebaseManager.getCurrentLoggedInUser()
                    if (currentlySignedInUser is FirebaseResponse.Success) {
                        if (currentlySignedInUser.data != null) {
                            Utils.printDebugLog("Found_Currently_Signed_in_User :: uid: ${currentlySignedInUser.data.uid}")
                            val result = firebaseManager.getUserData(currentlySignedInUser.data.uid)
                            if (result is FirebaseResponse.Success) {
                                Utils.printDebugLog("Fetching_User_Data :: Success: ${result.data}")
                                val userData = result.data
                                if (userData != null) {
                                    val userPrimaryLocation = userData.user_primary_location
                                    if (userPrimaryLocation.isNotBlank()) {
                                        val response = getCurrentWeather(userPrimaryLocation)
                                        Log.d(
                                            "WEATHER_ZOOMER_LOG",
                                            "fetch_weather_updates: Loading"
                                        )
                                        if (response.isSuccessful) {
                                            val weatherData = response.body()
                                            Log.d(
                                                "WEATHER_ZOOMER_LOG",
                                                "fetch_weather_updates: Success: ${weatherData!!.location}"
                                            )
//                                            TextToSpeechManager.initialize(context)
//                                            TextToSpeechManager.speak("Current Temperature: ${weatherData.current.temp_c.toInt()} degree celsius")
//                                            speakWeatherData(
//                                                context,
//                                                weatherData.current.temp_c.toInt()
//                                            )
                                            showNotification(context, weatherData.current.temp_c.toInt())
                                        } else {
                                            Log.e(
                                                "WEATHER_ZOOMER_LOG",
                                                "fetch_weather_updates: Failure: ${response.errorBody()}"
                                            )
                                        }
                                    } else {
                                        Log.d(
                                            "WEATHER_ZOOMER_LOG",
                                            "User_Primary_Location_Not_Found"
                                        )
                                    }
                                } else {
                                    Utils.printErrorLog("Firebase_returned_user_data_as_null")
                                }
                            } else if (result is FirebaseResponse.Failure) {
                                Utils.printErrorLog("Fetching_User_Data :: Failure: ${result.exception}")
                            } else {
                                Utils.printErrorLog("Fetching_User_Data :: Something_went_wrong_while_fetching_the_user_data")
                            }
                        } else {
                            Log.d("WEATHER_ZOOMER_LOG", "Currently_signed_in_user_not_found")
                        }
                    } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                        Log.d(
                            "WEATHER_ZOOMER_LOG",
                            "Something_went_wrong_while_fetching_the_currently_signed_in_user exception: ${currentlySignedInUser.exception}"
                        )
                    } else {
                        Log.d(
                            "WEATHER_ZOOMER_LOG",
                            "Something_went_wrong_while_fetching_the_currently_signed_in_user"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WEATHER_ZOOMER_LOG", "fetch_weather_updates: Failure: ${e.message}")
        } finally {
            // Release resources here
            TextToSpeechManager.shutdown()
        }
    }

    private suspend fun getCurrentWeather(location: String): Response<WeatherData> {
        return networkEndpoints.getCurrentWeather(
            BuildConfig.WEATHER_API_KEY,
            location,
            "yes"
        )
    }

    private fun showNotification(context: Context, temperature: Int) {
        // Create a notification channel for Android 8.0 and above
        createNotificationChannel(context)

        val currentTemperature = "Current Temperature $temperature degree celsius"

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Weather Update")
            .setContentText(currentTemperature)
            .setSmallIcon(R.drawable.baseline_time_24)
            .build()

        // Show the notification
        Log.d("WEATHER_ZOOMER_LOG", "showing_notification")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // Create a notification channel for Android 8.0 and above
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("WEATHER_ZOOMER_LOG", "creating_notification_channel")
            val notificationManager =
                context.getSystemService(NotificationManager::class.java)

            // Check if the channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                // Create the channel if it doesn't exist
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Weather Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Channel for weather notifications"
                }
                notificationManager.createNotificationChannel(channel)
            } else {
                Log.d("WEATHER_ZOOMER_LOG", "notification_channel_is_already_created")
            }
        }
    }

}