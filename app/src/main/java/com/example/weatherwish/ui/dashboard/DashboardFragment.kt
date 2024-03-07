package com.example.weatherwish.ui.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherwish.Application
import com.example.weatherwish.CenterScrollLayoutManager
import com.example.weatherwish.adapter.DailyForecastAdapter
import com.example.weatherwish.adapter.TemperatureAdapter
import com.example.weatherwish.databinding.FragmentDashboardBinding
import com.example.weatherwish.utils.Utils
import kotlin.math.abs
import com.example.weatherwish.R
import com.example.weatherwish.SharedViewModel
import com.example.weatherwish.adapter.DateAdapter
import com.example.weatherwish.api.ApiResponse
import com.example.weatherwish.constants.SystemOfMeasurement
import com.example.weatherwish.dataParsers.WeatherDataParser
import com.example.weatherwish.exceptionHandler.ExceptionHandler
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.UserModel
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.ui.takelocation.LocationActivity
import com.example.weatherwish.utils.ProgressDialog
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var weatherForecastData: WeatherForecastModel? = null
    private lateinit var userDataResult: FirebaseResponse<UserModel?>
    private lateinit var navController: NavController
    private lateinit var layoutmanager: CenterScrollLayoutManager
    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    private lateinit var dashboardViewModel: DashboardViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var location = ""

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

        /*binding.imgHamBurgerMenu.setOnClickListener {
            val intent = Intent(requireContext(), WalkThroughActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }*/

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
            dialogView.setBackgroundResource(R.drawable.dialog_background)
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
                }
            })
            rvDates.adapter = datesAdapter
            dialog.show()
        }

        /*binding.cvAirQuality.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("location", location)
            navController.navigate(R.id.action_dashboard_to_air_quality_fragment, bundle)
        }*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun attachObserver() {
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
                            dashboardViewModel.getForecastData(primaryLocation, 3, "yes", "yes")
                                .asLiveData()
                                .observe(viewLifecycleOwner) {
                                    when (it) {
                                        is ApiResponse.Success -> {
                                            weatherForecastData = it.data
                                            if (weatherForecastData != null) {
                                                ProgressDialog.dismiss()
                                                Utils.printDebugLog("Fetch_Weather_forecast :: Success location: ${weatherForecastData!!.location.region}")
                                                val weatherDataParser = WeatherDataParser(weatherForecastData!!, 0, SystemOfMeasurement.METRIC)

                                                //setting location
                                                binding.tvLocation.text = weatherDataParser.getSelectedLocation()

                                                //setting current weather data
                                                binding.tvDateTime.text = weatherDataParser.getSelectedDate()
                                                binding.imgCurrentTemp.setImageResource(resources.getIdentifier(
                                                    Utils.generateStringFromUrl(
                                                        weatherForecastData!!.current.condition.icon
                                                    ), "drawable", requireActivity().packageName
                                                ))
                                                binding.tvCurrentTemperature.text = weatherDataParser.getCurrentTemperature()
                                                binding.tvFeelsLike.text = weatherDataParser.getFeelsLikeTemperature()
                                                binding.tvCurrentCondition.text = weatherDataParser.getCurrentConditionText()
                                                binding.imgCurrentTemp.setImageResource(resources.getIdentifier(Utils.generateStringFromUrl(weatherDataParser.getConditionImageUrl()), "drawable", requireActivity().packageName))

                                                //setting hour wise horizontal list
                                                val temperatureAdapter =
                                                    TemperatureAdapter(weatherDataParser.getHourlyTemperatureData(), requireContext())
                                                val currentTimeMillis =
                                                    weatherForecastData!!.location.localtime_epoch.toLong() / 1000
                                                var nearestTimeDifference = Long.MAX_VALUE
                                                var nearestTimePosition = 0
                                                for ((index, time) in weatherForecastData!!.forecast.forecastday[0].hour.withIndex()) {
                                                    val timeDifference =
                                                        abs(currentTimeMillis - time.time_epoch)
                                                    if (timeDifference < nearestTimeDifference) {
                                                        nearestTimeDifference = timeDifference
                                                        nearestTimePosition = index
                                                    }
                                                }
                                                binding.rvForecastTemp.apply {
                                                    adapter = temperatureAdapter
                                                    layoutmanager =
                                                        CenterScrollLayoutManager(
                                                            context,
                                                            LinearLayoutManager.HORIZONTAL,
                                                            false
                                                        )
                                                    scrollToPosition(nearestTimePosition)
                                                }

                                                //setting air quality data
                                                val airQualityText = weatherDataParser.getAirQualityGrade()
                                                if (airQualityText.isNotBlank()) {
                                                    binding.cvAirQuality.visibility = View.VISIBLE
                                                    binding.tvAirQuality.text = airQualityText
                                                } else {
                                                    binding.tvAirQuality.text = ""
                                                    binding.cvAirQuality.visibility = View.GONE
                                                }
                                            }
                                            if (weatherForecastData != null) {
                                                location = weatherForecastData!!.location.name
                                                binding.tvHumidityPercentage.text =
                                                    "${weatherForecastData!!.current.humidity}%"
                                                binding.tvWindSpeed.text =
                                                    "${weatherForecastData!!.current.wind_kph} km/hr"
                                                binding.tvUvStatus.text =
                                                    "${dashboardViewModel.getUVValue(
                                                        weatherForecastData!!.current.uv.toInt())}"
//            binding.tvVisibilityKm.text = "${it.current.vis_km} km"
                                                binding.tvWindDirection.text = weatherForecastData!!.current.wind_dir

                                                binding.tvSunrise.text =
                                                    weatherForecastData!!.forecast.forecastday[0].astro.sunrise
                                                binding.tvSunset.text =
                                                    weatherForecastData!!.forecast.forecastday[0].astro.sunset

                                                val alerts = weatherForecastData!!.alerts.alert
                                                if (alerts.size > 0) {
                                                    val alert = alerts[0]
                                                    binding.cdAlertView.visibility = View.VISIBLE
                                                    binding.tvHeadline.text = alert.headline
                                                    binding.tvInstruction.text = alert.instruction
                                                }

                                                val dailyForecastAdapter =
                                                    DailyForecastAdapter(
                                                        weatherForecastData!!.forecast.forecastday,
                                                        requireContext()
                                                    )
                                                binding.rvDailyForecast.adapter =
                                                    dailyForecastAdapter
                                            }
                                        }

                                        is ApiResponse.Failure -> {
                                            ProgressDialog.dismiss()
                                            Utils.printErrorLog("Fetch_Weather_forecast :: Failure ${it.exception}")
                                            ExceptionHandler.handleException(requireContext(),
                                                it.exception!!
                                            )
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
                            "Soemthing went wrong",
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
                        "Soemthing went wrong",
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

        dashboardViewModel.forecastWeatherLiveData.observe(viewLifecycleOwner) {
            Log.d("TAG", "onViewCreateddata: http:${it.current.condition.icon}")
            binding.tvDateTime.text =
                Utils.convertUnixTimeToFormattedDayAndDate(it.current.last_updated_epoch.toLong())
            binding.imgCurrentTemp.setImageResource(
                resources.getIdentifier(
                    Utils.generateStringFromUrl(
                        it.current.condition.icon
                    ), "drawable", requireActivity().packageName
                )
            )
            location = it.location.name
            binding.tvLocation.text = "${it.location.name}, ${it.location.country}"
            binding.tvCurrentCondition.text = it.current.condition.text
            binding.tvHumidityPercentage.text = "${it.current.humidity}%"
            binding.tvCurrentTemperature.text = "${it.current.temp_c.toInt()}°C"
            binding.tvFeelsLike.text = "Feels like ${it.current.feelslike_c.toInt()}°C"
            binding.tvWindSpeed.text = "${it.current.wind_kph} km/hr"
            binding.tvUvStatus.text = "${dashboardViewModel.getUVValue(it.current.uv.toInt())}"
//            binding.tvVisibilityKm.text = "${it.current.vis_km} km"
            binding.tvWindDirection.text = it.current.wind_dir

            binding.tvSunrise.text = it.forecast.forecastday[0].astro.sunrise
            binding.tvSunset.text = it.forecast.forecastday[0].astro.sunset

            val alerts = it.alerts.alert
            if (alerts.size > 0) {
                val alert = alerts[0]
                binding.cdAlertView.visibility = View.VISIBLE
                binding.tvHeadline.text = alert.headline
                binding.tvInstruction.text = alert.instruction
            }

//            val currentTimeMillis = System.currentTimeMillis() / 1000
            val currentTimeMillis = it.location.localtime_epoch.toLong() / 1000
            var nearestTimeDifference = Long.MAX_VALUE
            var nearestTimePosition = 0

            for ((index, time) in it.forecast.forecastday[0].hour.withIndex()) {
                val timeDifference = abs(currentTimeMillis - time.time_epoch)
                if (timeDifference < nearestTimeDifference) {
                    nearestTimeDifference = timeDifference
                    nearestTimePosition = index
                }
            }

            val temperatureAdapter =
                TemperatureAdapter(it.forecast.forecastday[0].hour, requireContext())

            binding.rvForecastTemp.apply {
                adapter = temperatureAdapter
                layoutManager = CenterScrollLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                smoothScrollToPosition(nearestTimePosition)
            }


            val dailyForecastAdapter =
                DailyForecastAdapter(it.forecast.forecastday, requireContext())
            binding.rvDailyForecast.adapter = dailyForecastAdapter
        }

        dashboardViewModel.airQualityIndexLiveData.observe(viewLifecycleOwner) {
            binding.tvAirQuality.text = "Air Quality: $it"
        }
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