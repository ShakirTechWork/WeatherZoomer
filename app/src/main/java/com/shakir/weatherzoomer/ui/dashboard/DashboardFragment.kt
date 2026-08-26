package com.shakir.weatherzoomer.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.load
//import com.github.matteobattilana.weather.PrecipType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.shakir.ItemClickViewType
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.OnItemClickListener
import com.shakir.weatherzoomer.R
import com.shakir.weatherzoomer.SharedViewModel
import com.shakir.weatherzoomer.adapter.DailyForecastAdapter
import com.shakir.weatherzoomer.adapter.DateAdapter
import com.shakir.weatherzoomer.adapter.TemperatureAdapter
import com.shakir.weatherzoomer.constants.AppConstants
import com.shakir.weatherzoomer.constants.SystemOfMeasurement
import com.shakir.weatherzoomer.dataParsers.WeatherDataParser
import com.shakir.weatherzoomer.databinding.FragmentDashboardBinding
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UserNotFoundException
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UserStoredLocationNotFoundException
import com.shakir.weatherzoomer.exceptionHandling.exceptions.WeatherApiServiceException
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.interfaces.MainActivityInteractionListener
import com.shakir.weatherzoomer.model.Hour
import com.shakir.weatherzoomer.model.WeatherForecastModel
import com.shakir.weatherzoomer.response.AppResponse
import com.shakir.weatherzoomer.ui.takelocation.LocationActivity
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var weatherDataParser: WeatherDataParser? = null
    private lateinit var navController: NavController
    private var _binding: FragmentDashboardBinding? = null

    private var mainActivityInteractionListener: MainActivityInteractionListener? = null

    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

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

        attachClickListener()
        askNotificationPermission()

        binding.swipeRefreshLayout.setOnRefreshListener{
            Utils.printDebugLog("swipeRefreshLayout: Refreshed")
            binding.swipeRefreshLayout.isRefreshing = false
            load()
        }

        attachDataObserver()
        if (dashboardViewModel.weatherLiveData.value == null) {
            load()
        }
        checkForAppUpdates()
    }

    private fun attachDataObserver() {
        dashboardViewModel.weatherLiveData.observe(viewLifecycleOwner) { appResponse ->
            Utils.printDebugLog("jbfihdvchr: $appResponse")
            when (appResponse) {
                is AppResponse.Success -> {
                    GifProgressDialog.dismiss()
                    sharedViewModel.userData = dashboardViewModel.userLiveData.value
                    val weatherData = appResponse.data
                    if (weatherData != null) {
                        setData(weatherData, sharedViewModel.selectedDayLiveData.value!!)
                    }
                }
                is AppResponse.Failure -> {
                    GifProgressDialog.dismiss()
                    appResponse.exception.printStackTrace()
                    when (appResponse.exception) {
                        is UserNotFoundException -> {
                            Utils.showLongToast(requireContext(), "UserNotFoundException")
                        }
                        is UserStoredLocationNotFoundException -> {
                            Utils.showLongToast(requireContext(), "UserStoredLocationNotFoundException")
                            makeUserEnterTheLocationAgain()
                        }
                        is WeatherApiServiceException -> {
                            Utils.showLongToast(requireContext(), "WeatherApiServiceException")
                        }
                        else -> {
                            Utils.showLongToast(requireContext(), "UnknownException")
                        }
                    }
                }
                else -> {
                    Utils.printDebugLog("else_condition:")
                }
            }
        }

        dashboardViewModel.isLocationDeletedLiveData.observe(viewLifecycleOwner) { locationDeleteResponse ->
            when(locationDeleteResponse) {
                is FirebaseResponse.Success -> {
                    Utils.showLongToast(requireContext(), "Location Deleted Successfully")
                }
                is FirebaseResponse.Failure -> {
                    Utils.showLongToast(requireContext(), "Something went wrong! Please try again")
                }
                FirebaseResponse.Loading -> {}
                else -> {
                    Utils.printDebugLog("isLocationDeletedLiveData_else_condition:")
                }
            }
        }
    }

    private fun load() {
        resetViews()
        GifProgressDialog.show(requireContext(), "Please wait")
        dashboardViewModel.loadData()
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
            textTitle.text = getString(R.string.see_weather_from_below_list)

            val datesAdapter = DateAdapter(dashboardViewModel.getWeatherData()!!.forecast.forecastday, object: DateAdapter.OnItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    Utils.printDebugLog("selected_index: $index")
                    dialog.dismiss()
                    binding.tvGeminiTitle.text = getString(R.string.tap_to_plan_your_day)
                    binding.llTopGeminiLayout.isClickable = true
                    binding.progressBarGeminiResponse.visibility = View.GONE
                    binding.tvGeminiResponse.text = ""
                    binding.tvGeminiResponse.visibility = View.GONE
                    weatherDataParser = null
                    sharedViewModel.setSelectedDay(index)
                    val weatherData = dashboardViewModel.getWeatherData()
                    if (weatherData != null) {
                        setData(weatherData, index)
                    }
                }
            })
            rvDates.adapter = datesAdapter
            dialog.show()
        }

        binding.llTopGeminiLayout.setOnClickListener {
//            generateGeminiAnswer()
        }

        /*binding.cvAirQuality.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("location", location)
            navController.navigate(R.id.action_dashboard_to_air_quality_fragment, bundle)
        }*/
    }

    private fun setData(weatherForecastData: WeatherForecastModel, index: Int) {

        weatherDataParser = WeatherDataParser(weatherForecastData, index, getSystemOfMeasurement())

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
                val temperatureAdapter = TemperatureAdapter(temperatureList, requireContext(), getSystemOfMeasurement(),
                    object : OnItemClickListener<Hour, ItemClickViewType> {
                        override fun onItemClick(
                            item: Hour,
                            enum: ItemClickViewType,
                            position: Int?
                        ) {
                            Utils.printDebugLog("item: $item")
                            val action = DashboardFragmentDirections.actionDashboardToHourlyWeatherInfoFragment(item, getSystemOfMeasurement())
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
        val isSnowDataDisplayed: Boolean  //this variable is used to show only one from snow and rain. because for some loctions it is showing both
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
            Utils.printDebugLog("aqiIndexType: $aqiIndexType || aqiIndex: $aqiIndex")
            if (aqiIndexType.contains("UK")) {
                binding.llUkAqiBand.visibility = View.VISIBLE
                when (aqiIndex) {
                    1 -> binding.imgUkAqiIndex1.visibility = View.VISIBLE
                    2 -> binding.imgUkAqiIndex2.visibility = View.VISIBLE
                    3 -> binding.imgUkAqiIndex3.visibility = View.VISIBLE
                    4 -> binding.imgUkAqiIndex4.visibility = View.VISIBLE
                    5 -> binding.imgUkAqiIndex5.visibility = View.VISIBLE
                    6 -> binding.imgUkAqiIndex6.visibility = View.VISIBLE
                    7 -> binding.imgUkAqiIndex7.visibility = View.VISIBLE
                    8 -> binding.imgUkAqiIndex8.visibility = View.VISIBLE
                    9 -> binding.imgUkAqiIndex9.visibility = View.VISIBLE
                    10 -> binding.imgUkAqiIndex10.visibility = View.VISIBLE
                }
            } else {
                binding.llUsaAqiBand.visibility = View.VISIBLE
                when (aqiIndex) {
                    1 -> binding.imgUsaAqiIndex1.visibility = View.VISIBLE
                    2 -> binding.imgUsaAqiIndex2.visibility = View.VISIBLE
                    3 -> binding.imgUsaAqiIndex3.visibility = View.VISIBLE
                    4 -> binding.imgUsaAqiIndex4.visibility = View.VISIBLE
                    5 -> binding.imgUsaAqiIndex5.visibility = View.VISIBLE
                    6 -> binding.imgUsaAqiIndex6.visibility = View.VISIBLE
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

    private fun getSystemOfMeasurement(): SystemOfMeasurement {
        return when (sharedViewModel.userData?.userSettings?.preferredUnit) {
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
    }

    private fun setSnowFallDataWithAnimation(chanceOfSnowfall: String, snowPrecipitation: String) {
        binding.cvSnowData.visibility = View.VISIBLE
        binding.tvChanceOfSnowFall.text = chanceOfSnowfall
        binding.tvSnowPreciitation.text = snowPrecipitation
//        val weather = PrecipType.SNOW
        Utils.printDebugLog("Snow")
//        binding.snowView.apply {
//            setWeatherData(weather)
//            speed = 200 // How fast
//            emissionRate = 40f // How many particles
//            angle = 325 // The angle of the fall
//            fadeOutPercent = 1.0f // When to fade out (0.0f-1.0f)
//        }
    }

    private fun setRainFallDataWithAnimation(chanceOfRainfall: String, rainPrecipitation: String) {
        binding.cvRainData.visibility = View.VISIBLE
        binding.tvChanceOfRainFall.text = chanceOfRainfall
        binding.tvRainPrecipitation.text = rainPrecipitation
//        val weather = PrecipType.RAIN
        Utils.printDebugLog("Rain")
//        binding.rainView.apply {
//            setWeatherData(weather)
//            speed = 600 // How fast
//            emissionRate = 90f // How many particles
//            angle = 325 // The angle of the fall
//            fadeOutPercent = 1.0f // When to fade out (0.0f-1.0f)
//        }
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
        if (binding.llUkAqiBand.isVisible) {
            binding.llUkAqiBand.visibility = View.GONE
            binding.imgUkAqiIndex1.visibility = View.INVISIBLE
            binding.imgUkAqiIndex2.visibility = View.INVISIBLE
            binding.imgUkAqiIndex3.visibility = View.INVISIBLE
            binding.imgUkAqiIndex4.visibility = View.INVISIBLE
            binding.imgUkAqiIndex5.visibility = View.INVISIBLE
            binding.imgUkAqiIndex6.visibility = View.INVISIBLE
            binding.imgUkAqiIndex7.visibility = View.INVISIBLE
            binding.imgUkAqiIndex8.visibility = View.INVISIBLE
            binding.imgUkAqiIndex9.visibility = View.INVISIBLE
            binding.imgUkAqiIndex10.visibility = View.INVISIBLE
        }
        if (binding.llUsaAqiBand.isVisible) {
            binding.llUsaAqiBand.visibility = View.GONE
            binding.imgUsaAqiIndex1.visibility = View.INVISIBLE
            binding.imgUsaAqiIndex2.visibility = View.INVISIBLE
            binding.imgUsaAqiIndex3.visibility = View.INVISIBLE
            binding.imgUsaAqiIndex4.visibility = View.INVISIBLE
            binding.imgUsaAqiIndex5.visibility = View.INVISIBLE
            binding.imgUsaAqiIndex6.visibility = View.INVISIBLE
        }
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
                Utils.printDebugLog("indexLocationDeletedMLiveData: $locationKey")
                dashboardViewModel.deleteLocation(locationKey)
            }
        }
        sharedViewModel.isLocationSelectedLiveData.observe(viewLifecycleOwner) { location ->
            if (!location.isNullOrEmpty()) {
                sharedViewModel.isLocationSelectedLiveData.removeObservers(viewLifecycleOwner)
                Utils.printDebugLog("isLocationSelectedLiveData: $location")
                sharedViewModel.selectLocation(null)
            }
        }
    }

    private fun makeUserEnterTheLocationAgain() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setMessage("Please enter the location.")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(requireContext(), LocationActivity::class.java)
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
                binding.tvGeminiTitle.text = getString(R.string.planning_your_day)
                binding.tvGeminiResponse.visibility = View.GONE
                binding.tvGeminiResponse.text = ""
                binding.progressBarGeminiResponse.visibility = View.VISIBLE
                val response = generativeModel.generateContent(prompt)
                Utils.printDebugLog("generatedResponse: ${response.text}")
                displayTextCharacterByCharacter(response.text!!)
            } catch (exception: ServerException) {
                binding.llTopGeminiLayout.isClickable = true
                binding.tvGeminiTitle.text = getString(R.string.tap_to_plan_your_day)
                binding.tvGeminiResponse.visibility = View.GONE
                binding.tvGeminiResponse.text = ""
                binding.progressBarGeminiResponse.visibility = View.GONE
                Utils.showLongToast(requireContext(), "Please try again!")
            } catch (e: Exception) {
                binding.llTopGeminiLayout.isClickable = true
                binding.tvGeminiTitle.text = getString(R.string.tap_to_plan_your_day)
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
                    binding.tvGeminiTitle.text = "s"
                }
            }
        }
        handler.post(runnable)
    }

    private fun checkForAppUpdates() {
        val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo ->
            Utils.printDebugLog("checkForAppUpdates:: ${result.updateAvailability()}")
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Utils.printDebugLog("checkForAppUpdates:: yes_update_available")
                /*val builder = AlertDialog.Builder(requireContext())
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
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()*/
            } else {
                Utils.printDebugLog("checkForAppUpdates:: no_update_available")
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