package com.example.weatherwish.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.weatherwish.R
import com.example.weatherwish.api.NetworkEndpoints
import com.example.weatherwish.firebase.FirebaseManager
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.Hour
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class MyWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.weatherwish.widget.ACTION_REFRESH_WIDGET"
    }

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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            Log.d("WEATHER_WISH_LOG", "onUpdate_called ")

            if (networkInfo != null && networkInfo.isConnected) {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentlySignedInUser = firebaseManager.getCurrentLoggedInUser()
                    if (currentlySignedInUser is FirebaseResponse.Success) {
                        if (currentlySignedInUser.data != null) {
                            Utils.printDebugLog("Found_Currently_Signed_in_User :: uid: ${currentlySignedInUser.data.uid}")
                            val result =
                                firebaseManager.getUserData(currentlySignedInUser.data.uid)
                            if (result is FirebaseResponse.Success) {
                                Utils.printDebugLog("Fetching_User_Data :: Success: ${result.data}")
                                val userData = result.data
                                if (userData != null) {
                                    var userDndStartTime = 0L
                                    var userDndEndTime = 0L
                                    if (userData.user_settings.weather_updates.dnd_start_time.isNotBlank() &&
                                        userData.user_settings.weather_updates.dnd_end_time.isNotBlank()
                                    ) {
                                        userDndStartTime =
                                            userData.user_settings.weather_updates.dnd_start_time.toLong()
                                        userDndEndTime =
                                            userData.user_settings.weather_updates.dnd_end_time.toLong()
                                    }
                                    val userPrimaryLocation = userData.user_primary_location
                                    if (userPrimaryLocation.isNotBlank()) {
                                        val response = getCurrentWeather(userPrimaryLocation)
                                        Log.d(
                                            "WEATHER_WISH_LOG",
                                            "fetch_weather_updates: Loading"
                                        )
                                        if (response.isSuccessful) {
                                            val weatherData = response.body()
                                            Log.d(
                                                "WEATHER_WISH_LOG",
                                                "fetch_weather_updates: Success: ${weatherData!!.location}"
                                            )

                                            val currentTimeMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                Instant.now().toEpochMilli()
                                            } else {
                                                System.currentTimeMillis()
                                            }
                                            val nearestObjects = getFourNearestHourlyData(weatherData.forecast.forecastday[0].hour, currentTimeMillis)
                                            println("Current Time (Epoch): $currentTimeMillis")
                                            println("Nearest Objects:")
                                            nearestObjects.forEach {
                                                println("Time: ${it.time} (Epoch: ${convertToMillis(it.time)})")
                                            }

                                            // Iterate through each widget
                                            for (appWidgetId in appWidgetIds) {
                                                // Create an instance of RemoteViews for your widget layout
                                                val views = RemoteViews(
                                                    context.packageName,
                                                    R.layout.widget_layout
                                                )
                                                views.setImageViewResource(R.id.img_current_condition_image, context.resources.getIdentifier(Utils.generateStringFromUrl(weatherData.current.condition.icon), "drawable", context.packageName))
                                                views.setTextViewText(
                                                    R.id.tv_location,
                                                    weatherData.location.name
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_current_condition,
                                                    weatherData.current.condition.text
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_current_temperature,
                                                    weatherData.current.temp_c.toString()
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_temperature_1,
                                                    nearestObjects[0].temp_c.toString()
                                                )
                                                views.setImageViewResource(
                                                    R.id.img_current_condition_image_1,
                                                    context.resources.getIdentifier(Utils.generateStringFromUrl(nearestObjects[0].condition.icon), "drawable", context.packageName)
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_time_1,
                                                    Utils.convertToHourTime(nearestObjects[0].time_epoch.toLong())
                                                )

                                                views.setTextViewText(
                                                    R.id.tv_temperature_2,
                                                    nearestObjects[1].temp_c.toString()
                                                )
                                                views.setImageViewResource(
                                                    R.id.img_current_condition_image_2,
                                                    context.resources.getIdentifier(Utils.generateStringFromUrl(nearestObjects[1].condition.icon), "drawable", context.packageName)
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_time_2,
                                                    Utils.convertToHourTime(nearestObjects[1].time_epoch.toLong())
                                                )

                                                views.setTextViewText(
                                                    R.id.tv_temperature_3,
                                                    nearestObjects[2].temp_c.toString()
                                                )
                                                views.setImageViewResource(
                                                    R.id.img_current_condition_image_3,
                                                    context.resources.getIdentifier(Utils.generateStringFromUrl(nearestObjects[2].condition.icon), "drawable", context.packageName)
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_time_3,
                                                    Utils.convertToHourTime(nearestObjects[2].time_epoch.toLong())
                                                )

                                                views.setTextViewText(
                                                    R.id.tv_temperature_4,
                                                    nearestObjects[3].temp_c.toString()
                                                )
                                                views.setImageViewResource(
                                                    R.id.img_current_condition_image_4,
                                                    context.resources.getIdentifier(Utils.generateStringFromUrl(nearestObjects[3].condition.icon), "drawable", context.packageName)
                                                )
                                                views.setTextViewText(
                                                    R.id.tv_time_4,
                                                    Utils.convertToHourTime(nearestObjects[3].time_epoch.toLong())
                                                )

                                                // Set an OnClickListener for the refresh button
                                                val refreshIntent = Intent(context, MyWidgetProvider::class.java)
                                                refreshIntent.action = ACTION_REFRESH_WIDGET
                                                refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                                val pendingIntent = PendingIntent.getBroadcast(
                                                    context,
                                                    0,
                                                    refreshIntent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                                )
                                                views.setOnClickPendingIntent(R.id.tv_refresh, pendingIntent)
                                                
                                                // Update the widget
                                                appWidgetManager.updateAppWidget(appWidgetId, views)
                                            }
                                        } else {
                                            Log.e(
                                                "WEATHER_WISH_LOG",
                                                "fetch_weather_updates: Failure: ${response.errorBody()}"
                                            )
                                        }
                                    } else {
                                        Log.d(
                                            "WEATHER_WISH_LOG",
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
                            Log.d("WEATHER_WISH_LOG", "Currently_signed_in_user_not_found")
                        }
                    } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                        Log.d(
                            "WEATHER_WISH_LOG",
                            "Something_went_wrong_while_fetching_the_currently_signed_in_user exception: ${currentlySignedInUser.exception}"
                        )
                    } else {
                        Log.d(
                            "WEATHER_WISH_LOG",
                            "Something_went_wrong_while_fetching_the_currently_signed_in_user"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WEATHER_WISH_LOG", "fetch_weather_updates: Failure: ${e.message}")
        } finally {
            // Release resources here
            Log.d("WEATHER_WISH", "onUpdate: in_finally_block")
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null && context!=null) {
            if (intent.action == ACTION_REFRESH_WIDGET) {
                // Handle the refresh action here
                // You can fetch new data and update the widget accordingly
                Log.d("WEATHER_WISH_LOG", "Button clicked, refreshing data")

                // Call your data fetching logic and update the widget as needed
                // ...

                // Update the widget to reflect the new data
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
            } else {
                Log.d("WEATHER_WISH_LOG", "Button clicked, onReceive called but intent or context is null")
            }
        }
    }

    private suspend fun getCurrentWeather(location: String): Response<WeatherForecastModel> {
        return networkEndpoints.forecastWeather(
            "5c0b18c8dd744e858aa142154230910",
            location,
            1,
            "yes",
            "yes"
        )
    }

    private fun getFourNearestHourlyData(hourlyDataList: List<Hour>, currentTimeMillis: Long): List<Hour> {
        val sortedList = hourlyDataList.sortedBy { convertToMillis(it.time) }
        var nearestIndex = 0
        var minDifference = Long.MAX_VALUE
        for (i in sortedList.indices) {
            val difference = abs(currentTimeMillis - convertToMillis(sortedList[i].time))
            if (difference < minDifference) {
                minDifference = difference
                nearestIndex = i
            }
        }
        val nearestObjects = mutableListOf<Hour>()
        repeat(4) {
            val index = (nearestIndex + it) % sortedList.size
            nearestObjects.add(sortedList[index])
        }

        return nearestObjects
    }

    private fun convertToMillis(timeString: String): Long {
        val pattern = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val date = pattern.parse(timeString)
        return date?.time ?: 0
    }

}
