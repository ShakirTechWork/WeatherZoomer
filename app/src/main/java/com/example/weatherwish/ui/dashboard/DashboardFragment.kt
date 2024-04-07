package com.example.weatherwish.ui.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.Looper
import android.os.TransactionTooLargeException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherwish.Application
import com.example.weatherwish.BuildConfig
import com.example.weatherwish.adapter.DailyForecastAdapter
import com.example.weatherwish.adapter.TemperatureAdapter
import com.example.weatherwish.databinding.FragmentDashboardBinding
import com.example.weatherwish.utils.Utils
import com.example.weatherwish.R
import com.example.weatherwish.SharedViewModel
import com.example.weatherwish.adapter.DateAdapter
import com.example.weatherwish.api.ApiResponse
import com.example.weatherwish.constants.AppConstants
import com.example.weatherwish.constants.SystemOfMeasurement
import com.example.weatherwish.dataParsers.WeatherDataParser
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_IS_DISABLED
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_NOT_HAVE_ACCESS
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_NOT_PROVIDED
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.EXCEEDED_CALLS_PER_MONTH_QUOTA
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.INTERNAL_APPLICATION_ERROR
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_API_KEY
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_API_REQUEST_URL
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_JSON_BODY_IN_BULK_REQUEST
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.NO_LOCATION_FOUND
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.PARAMETER_Q_NOT_PROVIDED
import com.example.weatherwish.exceptionHandler.AppErrorCode.WeatherApiCodes.TOO_MANY_LOCATIONS_IN_BULK_REQUEST
import com.example.weatherwish.exceptionHandler.WeatherApiException
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.UserModel
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.ui.takelocation.LocationActivity
import com.example.weatherwish.utils.ProgressDialog
import com.github.matteobattilana.weather.PrecipType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.EOFException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

class DashboardFragment : Fragment() {

    private var weatherDataParser: WeatherDataParser? = null
    private var weatherForecastData: WeatherForecastModel? = null
    private lateinit var userDataResult: FirebaseResponse<UserModel?>
    private lateinit var navController: NavController
    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var systemOfMeasurement: SystemOfMeasurement

    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = NavHostFragment.findNavController(this@DashboardFragment)
        val repository = (requireActivity().application as Application).appRepository
        dashboardViewModel = ViewModelProvider(
            this,
            DashboardViewModelFactory(repository)
        )[DashboardViewModel::class.java]

        attachObserver()

        attachClickListener()

    }

    private fun attachClickListener() {

        binding.tvChangeLocation.setOnClickListener {
            Utils.singleOptionAlertDialog(
                requireContext(),
                "Change Location",
                "Want to change the location?",
                "Yes",
                true
            ) {
                val intent = Intent(requireContext(), LocationActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        binding.imgSettings.setOnClickListener {
            navController.navigate(R.id.action_dashboard_to_settings_fragment)
        }

        binding.tvDateTime.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_date_selection_dialog, null)
            // Determine theme mode (light/dark)
            val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

            // Set background drawable based on theme mode
            val drawableResId = if (isDarkMode) R.drawable.dialog_background_dark_mode else R.drawable.dialog_background_light_mode
            dialogView.setBackgroundResource(drawableResId)
            val builder = AlertDialog.Builder(requireContext())
            val dialog = builder.setView(dialogView).create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val textTitle = dialogView.findViewById<TextView>(R.id.tv_title)
            val rvDates = dialogView.findViewById<RecyclerView>(R.id.rv_dates)
            textTitle.text = "See Weather from below list of dates."

            val datesAdapter = DateAdapter(weatherForecastData!!.forecast.forecastday, object: DateAdapter.OnItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    Utils.printDebugLog("selected_index: $index")
                    dialog.dismiss()
                    binding.tvGeminiTitle.text = "Tap to plan your day"
                    binding.llTopGeminiLayout.isClickable = true
                    binding.progressBarGeminiResponse.visibility = View.GONE
                    binding.tvGeminiResponse.text = ""
                    binding.tvGeminiResponse.visibility = View.GONE
                    weatherDataParser = null
                    setData(weatherForecastData!!, index)
                }
            })
            rvDates.adapter = datesAdapter
            dialog.show()
        }

        binding.llTopGeminiLayout.setOnClickListener {
            generateGeminiAnswer()
        }

        /*binding.cvAirQuality.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("location", location)
            navController.navigate(R.id.action_dashboard_to_air_quality_fragment, bundle)
        }*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun attachObserver() {
        fetchUserAndWeatherData()
    }

    private fun fetchUserAndWeatherData() {
        resetViews()
        ProgressDialog.initialize(requireContext())
        ProgressDialog.show("Loading weather data")
        lifecycleScope.launch {
            userDataResult = dashboardViewModel.getUserData()
            when (userDataResult) {
                is FirebaseResponse.Success -> {
                    val userData = (userDataResult as FirebaseResponse.Success<UserModel?>).data
                    if (userData != null) {
                        Utils.printDebugLog("Fetching_User_Data :: Success")
                        sharedViewModel.userData = userData
                        if (userData.user_primary_location.isNotBlank()) {
                            Utils.printDebugLog("Got_primary_location :: ${userData.user_primary_location}")
                            val primaryLocation = userData.user_primary_location
                            systemOfMeasurement = when (userData.user_settings.preferred_unit) {
                                AppConstants.UserPreferredUnit.METRIC -> {
                                    SystemOfMeasurement.METRIC
                                }
                                AppConstants.UserPreferredUnit.IMPERIAL -> {
                                    SystemOfMeasurement.IMPERIAL
                                }
                                else -> {
                                    SystemOfMeasurement.METRIC
                                }
                            }
                            dashboardViewModel.getForecastData(primaryLocation, 3, "yes", "yes")
                                .asLiveData()
                                .observe(viewLifecycleOwner) {
                                    when (it) {
                                        is ApiResponse.Success -> {
                                            weatherForecastData = it.data
                                            if (weatherForecastData != null) {
                                                ProgressDialog.dismiss()
                                                Utils.printDebugLog("Fetch_Weather_forecast :: Success location: ${weatherForecastData!!.location.region}")
                                                weatherDataParser = null
                                                setData(weatherForecastData!!, 0)
                                            }
                                        }

                                        is ApiResponse.Failure -> {
                                            ProgressDialog.dismiss()
                                            Utils.printErrorLog("Fetch_Weather_forecast :: Failure ${it.exception}")
                                            handleExceptions(it.exception)
                                        }

                                        is ApiResponse.Loading -> {
                                            Utils.printDebugLog("Fetch_Weather_forecast :: Loading")
                                        }
                                    }
                                }
                        } else {
                            Utils.printDebugLog("User_Primary_Location_Not_Found")
                            Utils.singleOptionAlertDialog(
                                requireContext(),
                                "No location found",
                                "Please give the location name.",
                                "OKAY",
                                false
                            ) {
                                ProgressDialog.dismiss()
                                val intent = Intent(requireContext(), LocationActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    } else {
                        Utils.printErrorLog("User_Data_Not_Found")
                        Utils.singleOptionAlertDialog(
                            requireContext(),
                            "Something went wrong",
                            "Please login again.",
                            "OKAY",
                            false
                        ) {
                            ProgressDialog.dismiss()
                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }

                is FirebaseResponse.Failure -> {
                    ProgressDialog.dismiss()
                    Utils.printErrorLog("Fetching_User_Data :: Failure: ${(userDataResult as FirebaseResponse.Failure).exception}")
                    Utils.singleOptionAlertDialog(
                        requireContext(),
                        "Something went wrong",
                        "Please login again.",
                        "OKAY",
                        false
                    ) {
                        val intent = Intent(requireContext(), SignInActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }

                is FirebaseResponse.Loading -> {
                    Utils.printErrorLog("Fetching_User_Data :: Loading")
                }
            }
        }
    }

    private fun setData(weatherForecastData: WeatherForecastModel, index: Int) {

        weatherDataParser = WeatherDataParser(weatherForecastData, index, systemOfMeasurement)

        //setting location
        binding.clTopHeaderLayout.visibility = View.VISIBLE
        binding.tvLocation.text = weatherDataParser!!.getSelectedLocation()

        //setting current weather data
        binding.cvCurrentDataCard.visibility = View.VISIBLE
        binding.tvDateTime.text = weatherDataParser!!.getSelectedDate()
        binding.imgCurrentTemp.setImageResource(resources.getIdentifier(
            Utils.generateStringFromUrl(
                weatherForecastData.current.condition.icon
            ), "drawable", requireActivity().packageName
        ))
        binding.tvCurrentTemperature.text = weatherDataParser!!.getCurrentTemperature()
        binding.tvFeelsLike.text = weatherDataParser!!.getFeelsLikeTemperature()
        binding.tvCurrentCondition.text = weatherDataParser!!.getCurrentConditionText()
        binding.imgCurrentTemp.setImageResource(resources.getIdentifier(Utils.generateStringFromUrl(
            weatherDataParser!!.getConditionImageUrl()), "drawable", requireActivity().packageName))

        //setting hour wise horizontal list
        val temperatureAdapter = TemperatureAdapter(weatherDataParser!!.getHourlyTemperatureData(), requireContext(), systemOfMeasurement)
        val systemCurrentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(System.currentTimeMillis())).toInt()
        val position: Int
        for ((indexNumber, hourlyDataItem) in weatherDataParser!!.getHourlyTemperatureData().withIndex()) {
            val dataListItemTimeHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(hourlyDataItem.time_epoch.toLong() * 1000)).toInt()
            if (dataListItemTimeHour == systemCurrentHour) {
                position = indexNumber
                binding.rvForecastTemp.visibility = View.VISIBLE
                binding.rvForecastTemp.adapter = temperatureAdapter
                binding.rvForecastTemp.scrollToPosition(position)
                break
            }
        }

        //setting snow precipitation data if data is present
        var isSnowDataDisplayed = false //this variable is used to show only one from snow and rain. because for some loctions it is showing both
        val snowFallData = weatherDataParser!!.getSnowPrecipitaionData()
        if (snowFallData != null) {
            isSnowDataDisplayed = true
            setSnowFallDataWithAnimation(snowFallData.first, snowFallData.second)
        } else {
            isSnowDataDisplayed = false
        }

        //setting rain precipitation data if data is present
        if (!isSnowDataDisplayed) {
            val precipitation = weatherDataParser!!.getRainPrecipitationData()
            if (precipitation != null) {
                setRainFallDataWithAnimation(precipitation.first, precipitation.second)
            }
        }

        //setting air quality data
        val airQualityData = weatherDataParser!!.getAirQualityData(requireContext())
        if (airQualityData != null) {
            binding.cvAirQuality.visibility = View.VISIBLE
            binding.tvAirQuality.text = airQualityData.text
            val aqiIndexType = airQualityData.aqi_index_type
            binding.tvAqiMessage.text = "${getString(R.string.advice)} ${airQualityData.message}"
            binding.tvAqiBasedOn.text = airQualityData.aqi_index_type
            val aqiIndex = airQualityData.index
            if (aqiIndexType.contains("UK")) {
                binding.llUkAqiBand.visibility = View.VISIBLE
                when (aqiIndex) {
                    1 -> {
                        binding.imgUkAqiIndex1.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.imgUkAqiIndex2.visibility = View.VISIBLE
                    }
                    3 -> {
                        binding.imgUkAqiIndex3.visibility = View.VISIBLE
                    }
                    4 -> {
                        binding.imgUkAqiIndex4.visibility = View.VISIBLE
                    }
                    5 -> {
                        binding.imgUkAqiIndex5.visibility = View.VISIBLE
                    }
                    6 -> {
                        binding.imgUkAqiIndex6.visibility = View.VISIBLE
                    }
                    7 -> {
                        binding.imgUkAqiIndex7.visibility = View.VISIBLE
                    }
                    8 -> {
                        binding.imgUkAqiIndex8.visibility = View.VISIBLE
                    }
                    9 -> {
                        binding.imgUkAqiIndex9.visibility = View.VISIBLE
                    }
                    10 -> {
                        binding.imgUkAqiIndex10.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.llUsaAqiBand.visibility = View.VISIBLE
                when (aqiIndex) {
                    1 -> {
                        binding.imgUsaAqiIndex1.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.imgUsaAqiIndex2.visibility = View.VISIBLE
                    }
                    3 -> {
                        binding.imgUsaAqiIndex3.visibility = View.VISIBLE
                    }
                    4 -> {
                        binding.imgUsaAqiIndex4.visibility = View.VISIBLE
                    }
                    5 -> {
                        binding.imgUsaAqiIndex5.visibility = View.VISIBLE
                    }
                    6 -> {
                        binding.imgUsaAqiIndex6.visibility = View.VISIBLE
                    }
                }
            }

        }

        //making the ai day planner visible to the user
        binding.cvAiDayPlanner.visibility = View.VISIBLE

        //setting humidity percentage
        binding.tvHumidityPercentage.text = weatherDataParser!!.getHumidityPercentage()

        //setting Wind Speed
        binding.tvWindSpeed.text = weatherDataParser!!.getWindSpeed()

        //setting UV Index
        binding.tvUvStatus.text = weatherDataParser!!.getUVIndex()

        //setting wind direction data
        binding.tvWindDirection.text = weatherDataParser!!.getWindDirection()
        binding.cvOtherData.visibility = View.VISIBLE

        //sunrise data
        binding.cvSunData.visibility = View.VISIBLE
        binding.tvSunrise.text = weatherDataParser!!.getSunriseTime()
        binding.tvSunset.text = weatherDataParser!!.getSunsetTime()

        //setting moon data
        binding.cvMoonData.visibility = View.VISIBLE
        val moonData = weatherDataParser!!.getMoonData()
        Utils.printDebugLog("moonData: $moonData")
        if (moonData.moon_phase_drawable != null) {
            binding.imgMoonPhase.setImageResource(moonData.moon_phase_drawable!!)
        }
        binding.tvMoonPhase.text = moonData.moon_phase_text
        binding.tvMoonriseTime.text = moonData.moon_rise_time
        binding.tvMoonsetTime.text = moonData.moon_set_time
        binding.tvMoonIlluminationPercentage.text = moonData.moon_illumination_percentage

        //setting alerts if present
        val alertPair = weatherDataParser!!.getAlerts()
        if (alertPair != null) {
            binding.cvAlertCard.visibility = View.VISIBLE
            binding.tvHeadline.text = alertPair.first
            binding.tvInstruction.text = alertPair.second
        }

        //future days weather data
        binding.cvFutureData.visibility = View.VISIBLE
        val dailyForecastAdapter =
            DailyForecastAdapter(
                weatherForecastData.forecast.forecastday,
                requireContext()
            )
        binding.rvDailyForecast.adapter =
            dailyForecastAdapter
    }

    private fun setSnowFallDataWithAnimation(chanceOfSnowfall: String, snowPrecipitation: String) {
        binding.cvSnowData.visibility = View.VISIBLE
        binding.tvChanceOfSnowFall.text = chanceOfSnowfall
        binding.tvSnowPreciitation.text = snowPrecipitation
        val weather = PrecipType.SNOW
        Utils.printDebugLog("Snow")
        binding.snowView.apply {
            setWeatherData(weather)
            speed = 200 // How fast
            emissionRate = 40f // How many particles
            angle = 325 // The angle of the fall
            fadeOutPercent = 1.0f // When to fade out (0.0f-1.0f)
        }
    }

    private fun setRainFallDataWithAnimation(chanceOfRainfall: String, rainPrecipitation: String) {
        binding.cvRainData.visibility = View.VISIBLE
        binding.tvChanceOfRainFall.text = chanceOfRainfall
        binding.tvRainPrecipitation.text = rainPrecipitation
        val weather = PrecipType.RAIN
        Utils.printDebugLog("Rain")
        binding.rainView.apply {
            setWeatherData(weather)
            speed = 600 // How fast
            emissionRate = 90f // How many particles
            angle = 325 // The angle of the fall
            fadeOutPercent = 1.0f // When to fade out (0.0f-1.0f)
        }
    }

    private fun resetViews() {
        binding.clTopHeaderLayout.visibility = View.GONE
        binding.cvCurrentDataCard.visibility = View.GONE
        binding.cvAlertCard.visibility = View.GONE
        binding.rvForecastTemp.visibility = View.GONE
        binding.cvSnowData.visibility = View.GONE
        binding.cvRainData.visibility = View.GONE
        binding.cvAirQuality.visibility = View.GONE
        binding.progressBarGeminiResponse.visibility = View.GONE
        binding.tvGeminiResponse.visibility = View.GONE
        binding.cvAiDayPlanner.visibility = View.GONE
        binding.cvSunData.visibility = View.GONE
        binding.cvMoonData.visibility = View.GONE
        binding.cvOtherData.visibility = View.GONE
        binding.cvFutureData.visibility = View.GONE
    }

    private fun handleExceptions(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> makeUserSignInAgain("Something went wrong. Sign in again.")
            is FirebaseAuthActionCodeException -> makeUserSignInAgain("Something went wrong. Sign in again.")
            is FirebaseAuthUserCollisionException -> makeUserSignInAgain("Something went wrong. Sign in again.")
            is FirebaseAuthRecentLoginRequiredException -> makeUserSignInAgain("Something went wrong. Sign in again.")
            is FirebaseAuthEmailException -> makeUserSignInAgain("Something went wrong. Sign in again.")
            is FirebaseNetworkException -> {
                makeUserRetryAgain("Internet connection is not available. Please check your internet connection.") {
                    fetchUserAndWeatherData()
                }
            }
            is WeatherApiException -> handleWeatherApiException(exception)
            is DeadObjectException -> showSimpleMessage("Something went wrong. Please kill and reopen the app.")
            is TransactionTooLargeException -> {
                makeUserRetryAgain("Internet connection is not available. Please check your internet connection.") {
                    fetchUserAndWeatherData()
                }
            }
            is DatabaseException -> {
                makeUserRetryAgain("Internet connection is not available. Please check your internet connection.") {
                    fetchUserAndWeatherData()
                }
            }
            is ConnectException -> {
                makeUserRetryAgain("Internet connection is not available. Please check your internet connection.") {
                    fetchUserAndWeatherData()
                }
            }
            is SocketException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is SocketTimeoutException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is MalformedURLException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is UnknownHostException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is UnknownServiceException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is SSLHandshakeException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is SSLException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is EOFException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            else -> {
                makeUserRetryAgain("Something went wrong. Try again.") {
                    fetchUserAndWeatherData()
                }
            }
        }
    }

    private fun handleWeatherApiException(weatherApiException: WeatherApiException) {
        val errorCode = weatherApiException.errorCode
        if (errorCode == API_KEY_NOT_PROVIDED ||
            errorCode == INVALID_API_REQUEST_URL ||
            errorCode == INVALID_API_KEY ||
            errorCode == EXCEEDED_CALLS_PER_MONTH_QUOTA ||
            errorCode == API_KEY_IS_DISABLED ||
            errorCode == API_KEY_NOT_HAVE_ACCESS ||
            errorCode == INVALID_JSON_BODY_IN_BULK_REQUEST ||
            errorCode == TOO_MANY_LOCATIONS_IN_BULK_REQUEST ||
            errorCode == INTERNAL_APPLICATION_ERROR) {
            makeUserRetryAgain("Something went wrong") {
                fetchUserAndWeatherData()
            }
        } else if (errorCode == PARAMETER_Q_NOT_PROVIDED || errorCode == NO_LOCATION_FOUND) {
            makeUserEnterTheLocationAgain("Location Not found. Please enter the location again.")
        }
    }

    private fun showSimpleMessage(message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.show()
    }

    private fun makeUserRetryAgain(message: String, retryAction: () -> Unit = {}) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                retryAction()
            }

        dialogBuilder.show()
    }

    private fun makeUserEnterTheLocationAgain(message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(requireContext(), LocationActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        dialogBuilder.show()
    }

    private fun makeUserSignInAgain(message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        dialogBuilder.show()
    }

    private fun generateGeminiAnswer() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_AI_API_KEY
        )
        val prompt = weatherDataParser!!.getGeminiAiPrompt()
        Utils.printDebugLog("AI_Prompt: $prompt")
        lifecycleScope.launch (Dispatchers.Main) {
            try {
                binding.tvGeminiTitle.text = "Please Wait, planning your day"
                binding.tvGeminiResponse.visibility = View.GONE
                binding.tvGeminiResponse.text = ""
                binding.progressBarGeminiResponse.visibility = View.VISIBLE
                val response = generativeModel.generateContent(prompt)
                Utils.printDebugLog("generatedResponse: ${response.text}")
                displayTextCharacterByCharacter(response.text!!)
            } catch (exception: ServerException) {
                binding.llTopGeminiLayout.isClickable = true
                binding.tvGeminiTitle.text = "Tap to plan your day"
                binding.tvGeminiResponse.visibility = View.GONE
                binding.tvGeminiResponse.text = ""
                binding.progressBarGeminiResponse.visibility = View.GONE
                Utils.showLongToast(requireContext(), "Please try again!")
            } catch (e: Exception) {
                binding.llTopGeminiLayout.isClickable = true
                binding.tvGeminiTitle.text = "Tap to plan your day"
                binding.tvGeminiResponse.visibility = View.GONE
                binding.tvGeminiResponse.text = ""
                binding.progressBarGeminiResponse.visibility = View.GONE
                Utils.showLongToast(requireContext(), "Please try again!")
                Utils.printErrorLog("Gemini_Exception $e")
            }
        }
    }

    private fun displayTextCharacterByCharacter(textToDisplay: String) {
        binding.llTopGeminiLayout.isClickable = false
        binding.progressBarGeminiResponse.visibility = View.GONE
        binding.tvGeminiResponse.visibility = View.VISIBLE
        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < textToDisplay.length) {
                    val currentText = binding.tvGeminiResponse.text.toString()
                    val newText = currentText + textToDisplay[currentIndex]
                    binding.tvGeminiResponse.text = newText
                    currentIndex++
                    handler.postDelayed(this, 30)
                } else {
                    currentIndex = 0
                    binding.llTopGeminiLayout.isClickable = true
                    binding.tvGeminiTitle.text = "Tap to plan your day"
                }
            }
        }
        handler.post(runnable)
    }

    fun updateApp() {
        val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        Log.d("TAG", "updateAppCalled: "+ UpdateAvailability.UPDATE_AVAILABLE)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo ->
            Log.d("TAG", "updateAppCalled: "+ result.updateAvailability())
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Update the App!!!")
                builder.setMessage("A mandatory update is ready for you! Please update the app to ensure a seamless experience.")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                builder.setPositiveButton("Update") { _, _ ->
                    try {
                        startActivity(
                            Intent(
                                "android.intent.action.VIEW",
                                Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().packageName)
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), "Please update the app from play store!!!", Toast.LENGTH_LONG).show()
                    }
                }
//                builder.setNegativeButton("NO") { dialogInterface, which ->
//                    requireActivity().finish()
//                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()

            }
        }
    }

}