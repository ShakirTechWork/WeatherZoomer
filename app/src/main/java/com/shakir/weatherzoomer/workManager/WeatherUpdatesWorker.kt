package com.shakir.weatherzoomer.workManager

//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters

//class WeatherUpdatesWorker(context: Context, params: WorkerParameters) :
//    CoroutineWorker(context, params) {
//
//    private lateinit var textToSpeech: TextToSpeech
//    private val CHANNEL_ID = "weather_channel"
//    private val NOTIFICATION_ID = 1
//
//    private val retrofit: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl("http://api.weatherapi.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(createOkHttpClient())
//            .build()
//    }
//
//    private val firebaseManager = FirebaseManager()
//
//    private fun createOkHttpClient(): OkHttpClient {
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        return OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .build()
//    }
//
//    private val networkEndpoints: NetworkEndpoints by lazy {
//        retrofit.create(NetworkEndpoints::class.java)
//    }
//
//    override suspend fun doWork(): Result {
//        try {
//            val connectivityManager =
//                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val networkInfo = connectivityManager.activeNetworkInfo
//            Log.d("WEATHER_ZOOMER_LOG", "doWork_called ")
//
//            if (networkInfo != null && networkInfo.isConnected) {
//                CoroutineScope(Dispatchers.IO).launch {
////                    val primaryLocation = appDataStore.userPrimaryLocation.first()
////                    Log.d("WEATHER_ZOOMER_LOG", "user_primary_location: $primaryLocation")
////                    if (primaryLocation.isNotBlank()) {
////                        val response = getCurrentWeather(primaryLocation)
////                        Log.d("WEATHER_ZOOMER_LOG", "fetch_weather_updates: called_API")
////                        if (response.isSuccessful) {
////                            val weatherData = response.body()
////                            Log.d(
////                                "WEATHER_ZOOMER_LOG",
////                                "fetch_weather_updates: Success: ${weatherData!!.location}"
////                            )
////                            speakWeatherData("Current Temperature ${weatherData.current.temp_c.toInt()} degree celsius", weatherData.current.temp_c.toString())
////                        } else {
////                            Log.e(
////                                "WEATHER_ZOOMER_LOG",
////                                "fetch_weather_updates: Failure: ${response.errorBody()}"
////                            )
////                        }
////                    } else {
////                        Log.d("WEATHER_ZOOMER_LOG", "User_Primary_Location_Not_Found")
////                    }
//                    val currentlySignedInUser = firebaseManager.getCurrentLoggedInUser()
//                    if (currentlySignedInUser is FirebaseResponse.Success) {
//                        if (currentlySignedInUser.data != null) {
//                            Utils.printDebugLog("Found_Currently_Signed_in_User :: uid: ${currentlySignedInUser.data.uid}")
//                            val result = firebaseManager.getUserData(currentlySignedInUser.data.uid)
//                            if (result is FirebaseResponse.Success) {
//                                Utils.printDebugLog("Fetching_User_Data :: Success: ${result.data}")
//                                val userData = result.data
//                                if (userData != null) {
//                                    var userDndStartTime= 0L
//                                    var userDndEndTime = 0L
//                                    if (userData.user_settings.weather_updates.dnd_start_time.isNotBlank() &&
//                                        userData.user_settings.weather_updates.dnd_end_time.isNotBlank()) {
//                                        userDndStartTime = userData.user_settings.weather_updates.dnd_start_time.toLong()
//                                        userDndEndTime = userData.user_settings.weather_updates.dnd_end_time.toLong()
//                                    }
//                                    if (checkDoNotDisturbTime(userDndStartTime, userDndEndTime)) {
//                                        val userPrimaryLocation = userData.user_primary_location
//                                        if (userPrimaryLocation.isNotBlank()) {
//                                            val response = getCurrentWeather(userPrimaryLocation)
//                                            Log.d("WEATHER_ZOOMER_LOG", "fetch_weather_updates: Loading")
//                                            if (response.isSuccessful) {
//                                                val weatherData = response.body()
//                                                Log.d(
//                                                    "WEATHER_ZOOMER_LOG",
//                                                    "fetch_weather_updates: Success: ${weatherData!!.location}"
//                                                )
//                                                speakWeatherData(
//                                                    "Current Temperature ${weatherData.current.temp_c.toInt()} degree celsius",
//                                                    weatherData.current.temp_c.toString()
//                                                )
//                                            } else {
//                                                Log.e(
//                                                    "WEATHER_ZOOMER_LOG",
//                                                    "fetch_weather_updates: Failure: ${response.errorBody()}"
//                                                )
//                                            }
//                                        } else {
//                                            Log.d("WEATHER_ZOOMER_LOG", "User_Primary_Location_Not_Found")
//                                        }
//                                    } else {
//                                        Utils.printErrorLog("Skipping_because_of_do_not_disturb_time")
//                                    }
//                                } else {
//                                    Utils.printErrorLog("Firebase_returned_user_data_as_null")
//                                }
//                            } else if (result is FirebaseResponse.Failure) {
//                                Utils.printErrorLog("Fetching_User_Data :: Failure: ${result.exception}")
//                            } else {
//                                Utils.printErrorLog("Fetching_User_Data :: Something_went_wrong_while_fetching_the_user_data")
//                            }
//                        } else {
//                            Log.d("WEATHER_ZOOMER_LOG", "Currently_signed_in_user_not_found")
//                        }
//                    } else if (currentlySignedInUser is FirebaseResponse.Failure) {
//                        Log.d(
//                            "WEATHER_ZOOMER_LOG",
//                            "Something_went_wrong_while_fetching_the_currently_signed_in_user exception: ${currentlySignedInUser.exception}"
//                        )
//                    } else {
//                        Log.d(
//                            "WEATHER_ZOOMER_LOG",
//                            "Something_went_wrong_while_fetching_the_currently_signed_in_user"
//                        )
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("WEATHER_ZOOMER_LOG", "fetch_weather_updates: Failure: ${e.message}")
//        } finally {
//            // Release resources here
//            if (::textToSpeech.isInitialized) {
//                textToSpeech.stop()
//                textToSpeech.shutdown()
//            }
//        }
//        return Result.success()
//    }
//
//    private fun checkDoNotDisturbTime(dndStartTime: Long, dndEndTime: Long): Boolean {
//        val currentTime = System.currentTimeMillis()
//        Log.d("WEATHER_ZOOMER_LOG", "Time: Start DND:${dndStartTime} | End DND:${dndEndTime} | Current time:${currentTime}")
//        Log.d("WEATHER_ZOOMER_LOG", "checkDoNotDisturbTime: ${dndStartTime < currentTime && currentTime < dndEndTime}")
//        return if (dndStartTime==0L && dndEndTime==0L) {
//            true
//        } else !(dndStartTime < currentTime && currentTime < dndEndTime)
//    }
//
//    private suspend fun getCurrentWeather(location: String): Response<WeatherData> {
//        return networkEndpoints.getCurrentWeather(
//            BuildConfig.WEATHER_API_KEY,
//            location,
//            "yes"
//        )
//    }
//
//    private fun showNotification(temperature: String) {
//        // Create a notification channel for Android 8.0 and above
//        createNotificationChannel()
//
//        val currentTemperature = "Current Temperature $temperature degree celsius"
//
//        // Build the notification
//        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
//            .setContentTitle("Weather Update")
//            .setContentText(currentTemperature)
//            .setSmallIcon(R.drawable.baseline_time_24)
//            .setSound(android.net.Uri.EMPTY)
//            .setSilent(true)
//            .build()
//
//        // Show the notification
//        Log.d("WEATHER_ZOOMER_LOG", "showing_notification")
//        val notificationManager =
//            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//
//    private fun speakWeatherData(text: String, temperature: String) {
//        textToSpeech = TextToSpeech(applicationContext) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                showNotification(temperature)
//                Log.d("WEATHER_ZOOMER_LOG", "using text_to_speech ")
//                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
//            } else {
//                showNotification(temperature)
//            }
//        }
//    }
//
//    // Create a notification channel for Android 8.0 and above
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Log.d("WEATHER_ZOOMER_LOG", "creating_notification_channel")
//            val notificationManager =
//                applicationContext.getSystemService(NotificationManager::class.java)
//
//            // Check if the channel already exists
//            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
//                // Create the channel if it doesn't exist
//                val channel = NotificationChannel(
//                    CHANNEL_ID,
//                    "Weather Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//                ).apply {
//                    description = "Channel for weather notifications"
//                }
//                notificationManager.createNotificationChannel(channel)
//            } else {
//                Log.d("WEATHER_ZOOMER_LOG", "notification_channel_is_already_created")
//            }
//        }
//    }
//
//
//}
