package com.shakir.weatherzoomer.ui.dashboard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.Looper
import android.os.TransactionTooLargeException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.load
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
import com.shakir.ItemClickViewType
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.OnItemClickListener
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.SharedViewModel
import com.shakir.weatherzoomer.adapter.DailyForecastAdapter
import com.shakir.weatherzoomer.adapter.DateAdapter
import com.shakir.weatherzoomer.adapter.TemperatureAdapter
import com.shakir.weatherzoomer.api.ApiResponse
import com.shakir.weatherzoomer.constants.AppConstants
import com.shakir.weatherzoomer.constants.SystemOfMeasurement
import com.shakir.weatherzoomer.dataParsers.WeatherDataParser
import com.shakir.weatherzoomer.databinding.FragmentDashboardBinding
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_IS_DISABLED
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_NOT_HAVE_ACCESS
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.API_KEY_NOT_PROVIDED
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.EXCEEDED_CALLS_PER_MONTH_QUOTA
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.INTERNAL_APPLICATION_ERROR
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_API_KEY
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_API_REQUEST_URL
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.INVALID_JSON_BODY_IN_BULK_REQUEST
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.NO_LOCATION_FOUND
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.PARAMETER_Q_NOT_PROVIDED
import com.shakir.weatherzoomer.exceptionHandler.AppErrorCode.WeatherApiCodes.TOO_MANY_LOCATIONS_IN_BULK_REQUEST
import com.shakir.weatherzoomer.exceptionHandler.WeatherApiException
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.interfaces.MainActivityInteractionListener
import com.shakir.weatherzoomer.model.Hour
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.model.WeatherForecastModel
import com.shakir.weatherzoomer.ui.signIn.SignInActivity
import com.shakir.weatherzoomer.ui.takelocation.LocationActivity
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils
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

    private var mainActivityInteractionListener: MainActivityInteractionListener? = null

    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var systemOfMeasurement: SystemOfMeasurement

    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivityInteractionListener) {
            mainActivityInteractionListener = context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnFragmentInteractionListener"
            )
        }
    }

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
//        fetchUserAndWeatherData()
        attachClickListener()
        askNotificationPermission()

        binding.swipeRefreshLayout.setOnRefreshListener{
            Utils.printDebugLog("swipeRefreshLayout: Refreshed")
            fetchUserAndWeatherData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        //new implementation
        attachNewObservers()
        if (dashboardViewModel.weatherForecastLiveData.value == null) {
            if (Utils.isInternetAvailable(requireContext())) {
                Utils.printDebugLog("onViewCreated")
                GifProgressDialog.initialize(requireContext())
                GifProgressDialog.show("Loading weather data")
                resetViews()
                dashboardViewModel.getCurrentlySignedInUserWithData()
            }
        }
    }

    private fun fetchWeatherDataForLocation(location: String) {
        GifProgressDialog.initialize(requireContext())
        GifProgressDialog.show("Loading weather data")
        resetViews()
        dashboardViewModel.getWeatherForecastData(location, 3, "yes", "yes")
    }

    private fun attachNewObservers() {
        dashboardViewModel.currentlySignedInUserLiveData.observe(viewLifecycleOwner) {response ->
            when (response) {
                is FirebaseResponse.Success -> {
                    val userData = response.data
                    if (userData != null) {
                        Utils.printDebugLog("currentlySignedInUserLiveData :: Success | User_data: ${userData.user_name}")
                        sharedViewModel.userData = userData
                        val locations = userData.user_settings.locations
                        if (locations.isNotEmpty()) {
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
                            if (dashboardViewModel.weatherForecastLiveData.value == null) {
                                var location = ""
                                locations.forEach {
                                    if (it.value.selectedLocation) {
                                        location = it.value.location
                                    }
                                }
                                dashboardViewModel.getWeatherForecastData(location, 3, "yes", "yes" )
                            }
                        } else {
                            Utils.singleOptionAlertDialog(
                                requireContext(),
                                "No location found",
                                "Please give the location name.",
                                "OKAY",
                                false
                            ) {
                                GifProgressDialog.dismiss()
                                val intent = Intent(requireContext(), LocationActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    } else {
                        GifProgressDialog.dismiss()
                        Utils.singleOptionAlertDialog(
                            requireContext(),
                            "Something went wrong",
                            "Please login again.",
                            "OKAY",
                            false
                        ) {
                            dashboardViewModel.signOutCurrentUser(requireActivity())
                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
                is FirebaseResponse.Failure -> {
                    Utils.printErrorLog("currentlySignedInUserLiveData :: Failure: ${response.exception}")
                    GifProgressDialog.dismiss()
                    Utils.singleOptionAlertDialog(
                        requireContext(),
                        "Something went wrong",
                        "Please login again.",
                        "OKAY",
                        false
                    ) {
                        dashboardViewModel.signOutCurrentUser(requireActivity())
                        val intent = Intent(requireContext(), SignInActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                FirebaseResponse.Loading -> {
                    Utils.printDebugLog("currentlySignedInUserLiveData :: Loading")
                }
            }
        }

        dashboardViewModel.weatherForecastLiveData.observe(viewLifecycleOwner) {weatherApiResponse ->
            when (weatherApiResponse) {
                is ApiResponse.Success -> {
                    Utils.printDebugLog("weatherForecastLiveData: Success | ${weatherApiResponse.data?.location?.name}")
                    weatherForecastData = weatherApiResponse.data
                    if (weatherForecastData != null) {
                        GifProgressDialog.dismiss()
                        weatherDataParser = null
                        setData(weatherForecastData!!, 0)
                    }
                }
                is ApiResponse.Failure -> {
                    GifProgressDialog.dismiss()
                    Utils.printErrorLog("weatherForecastLiveData :: Failure ${weatherApiResponse.exception}")
                    handleExceptions(weatherApiResponse.exception)
                }
                is ApiResponse.Loading -> {
                    Utils.printDebugLog("weatherForecastLiveData :: Loading")
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show a rationale to the user and request the permission again.
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun attachClickListener() {

        binding.imgHamburger.setSafeOnClickListener {
            onInteractionWithSideNavigationDrawer()
        }

        binding.tvLocation.setSafeOnClickListener {
            onInteractionWithSideNavigationDrawer()
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

    private fun fetchUserAndWeatherData() {
        resetViews()
        if (Utils.isInternetAvailable(requireContext())) {
            GifProgressDialog.initialize(requireContext())
            GifProgressDialog.show("Loading weather data")
            lifecycleScope.launch {
                Utils.printDebugLog("Fetching_User_Data :: Loading2")
                userDataResult = dashboardViewModel.getUserData()
                when (userDataResult) {
                    is FirebaseResponse.Success -> {
                        val userData = (userDataResult as FirebaseResponse.Success<UserModel?>).data
                        if (userData != null) {
                            Utils.printDebugLog("Fetching_User_Data :: Success")
                            sharedViewModel.userData = userData
                            if (userData.user_settings.locations.isNotEmpty()) {
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
                                                    GifProgressDialog.dismiss()
                                                    Utils.printDebugLog("Fetch_Weather_forecast :: Success location: ${weatherForecastData!!.location.region}")
                                                    weatherDataParser = null
                                                    setData(weatherForecastData!!, 0)
                                                }
                                            }

                                            is ApiResponse.Failure -> {
                                                GifProgressDialog.dismiss()
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
                                    GifProgressDialog.dismiss()
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
                                GifProgressDialog.dismiss()
                                dashboardViewModel.signOutCurrentUser(requireActivity())
                                val intent = Intent(requireContext(), SignInActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                        }
                    }

                    is FirebaseResponse.Failure -> {
                        GifProgressDialog.dismiss()
                        Utils.printErrorLog("Fetching_User_Data :: Failure: ${(userDataResult as FirebaseResponse.Failure).exception}")
                        Utils.singleOptionAlertDialog(
                            requireContext(),
                            "Something went wrong",
                            "Please login again.",
                            "OKAY",
                            false
                        ) {
                            dashboardViewModel.signOutCurrentUser(requireActivity())
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
        } else {
            makeUserRetryAgain("Something went wrong. May be an internet problem") {
                fetchUserAndWeatherData()
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
        binding.imgCurrentTemp.load(weatherDataParser!!.getConditionImage())
        binding.tvCurrentTemperature.text = weatherDataParser!!.getCurrentTemperature()
        binding.tvFeelsLike.text = weatherDataParser!!.getFeelsLikeTemperature()
        binding.tvCurrentCondition.text = weatherDataParser!!.getCurrentConditionText()

        //setting hour wise horizontal list
        val temperatureList = weatherDataParser!!.getHourlyTemperatureData()
        val systemCurrentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(System.currentTimeMillis())).toInt()
        val position: Int
        for ((indexNumber, hourlyDataItem) in weatherDataParser!!.getHourlyTemperatureData().withIndex()) {
            val dataListItemTimeHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(hourlyDataItem.time_epoch.toLong() * 1000)).toInt()
            if (dataListItemTimeHour == systemCurrentHour) {
                position = indexNumber
                temperatureList[position].isCurrentHour = true
                val temperatureAdapter = TemperatureAdapter(temperatureList, requireContext(), systemOfMeasurement,
                    object : OnItemClickListener<Hour, ItemClickViewType> {
                        override fun onItemClick(
                            item: Hour,
                            enum: ItemClickViewType,
                            position: Int?
                        ) {
                            Utils.printDebugLog("item: $item")
                            val action = DashboardFragmentDirections.actionDashboardToHourlyWeatherInfoFragment(item, systemOfMeasurement)
                            navController.navigate(action)
//                            val bundle = Bundle().apply {
//                                putParcelable("hour", hour)
//                            }
//                            navController.navigate(R.id.action_dashboard_to_hourly_weather_info_fragment, bundle)
                        }
                    })
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
            binding.tvHeadline.text = alertPair.headline
            binding.tvDescription.text = alertPair.desc
            if (alertPair.areas.isNotBlank()) {
                binding.tvAreas.text = alertPair.areas
            } else {
                binding.tvAreas.visibility = View.GONE
            }
            if (alertPair.effective.isNotBlank()) {
                binding.tvAlertStartTime.visibility = View.VISIBLE
                binding.tvAlertStartTime.text = alertPair.effective
            }
            if (alertPair.expires.isNotBlank()) {
                binding.tvAlertEndTime.visibility = View.VISIBLE
                binding.tvAlertEndTime.text = alertPair.expires
            }
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
        binding.imgCurrentTemp.clear()
        binding.cvAlertCard.visibility = View.GONE
        binding.tvAlertStartTime.visibility = View.GONE
        binding.tvAlertEndTime.visibility = View.GONE
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

    private fun onInteractionWithSideNavigationDrawer() {
        Utils.printDebugLog("onInteractionWithSideNavigationDrawer")
        mainActivityInteractionListener?.openNavigationDrawer()
        sharedViewModel.onNewLocationRequestedLiveData.removeObservers(viewLifecycleOwner)
        sharedViewModel.deleteSavedLocationLiveData.removeObservers(viewLifecycleOwner)
        sharedViewModel.isLocationSelectedLiveData.removeObservers(viewLifecycleOwner)
        sharedViewModel.onNewLocationRequestedLiveData.observe(viewLifecycleOwner) {isNewLocationRequested ->
            Utils.printDebugLog("isNewLocationRequested: $isNewLocationRequested")
            if (isNewLocationRequested) {
                startActivity(Intent(requireContext(), LocationActivity::class.java))
                requireActivity().finish()
            }
        }
        sharedViewModel.deleteSavedLocationLiveData.observe(viewLifecycleOwner) { locationKey ->
            if (locationKey.isNotBlank()) {
                dashboardViewModel.deleteSavedLocation(locationKey)
                Utils.printDebugLog("indexLocationDeletedMLiveData: $locationKey")
            }
        }
        sharedViewModel.isLocationSelectedLiveData.observe(viewLifecycleOwner) { location ->
            if (!location.isNullOrEmpty()) {
                sharedViewModel.isLocationSelectedLiveData.removeObservers(viewLifecycleOwner)
                Utils.printDebugLog("isLocationSelectedLiveData: $location")
                sharedViewModel.selectLocation(null)
                fetchWeatherDataForLocation(location)
            }
        }
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
            is SocketTimeoutException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
                    fetchUserAndWeatherData()
                }
            }
            is SocketException -> {
                makeUserRetryAgain("Something went wrong. May be an internet problem") {
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
                dashboardViewModel.signOutCurrentUser(requireActivity())
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

    fun checkForAppUpdates2() {
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue with the operation.
        } else {
            // Permission is denied. Inform the user that your app will not show notifications.
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivityInteractionListener = null
    }

}