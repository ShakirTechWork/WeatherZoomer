package com.shakir.weatherzoomer.ui.hourlyWeatherInfo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import coil.load
import com.github.matteobattilana.weather.PrecipType
import com.shakir.weatherzoomer.constants.SystemOfMeasurement
import com.shakir.weatherzoomer.dataParsers.WeatherHourDataParser
import com.shakir.weatherzoomer.databinding.FragmentHourlyWeatherInfoBinding
import com.shakir.weatherzoomer.model.Hour
import com.shakir.weatherzoomer.utils.Utils

class HourlyWeatherInfoFragment : Fragment() {

    private val args: HourlyWeatherInfoFragmentArgs by navArgs()

    private var _binding: FragmentHourlyWeatherInfoBinding? = null

    private lateinit var hourDataParser: WeatherHourDataParser

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHourlyWeatherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hour: Hour = args.hour
        val systemOfMeasurement: SystemOfMeasurement = args.systemOfMeasurement
        Utils.printDebugLog("hour data: $hour")
        hourDataParser = WeatherHourDataParser(hour, systemOfMeasurement)

        binding.tvSelectedDateTime.text = hourDataParser.getSelectedDateTime()
        binding.tvTemperature.text = hourDataParser.getTemperature()
        binding.tvFeelsLikeTemperature.text = hourDataParser.getFeelsLikeTemperature()
        binding.tvCondition.text = hourDataParser.getConditionText()
        binding.imgCondition.load(hourDataParser.getConditionImage())

        //setting snow precipitation data if data is present
        var isSnowDataDisplayed = false //this variable is used to show only one from snow and rain. because for some loctions it is showing both
        val chanceOfSnowFall = hourDataParser.getSnowPrecipitaionData()
        if (chanceOfSnowFall != null) {
            isSnowDataDisplayed = true
            setSnowFallDataWithAnimation(chanceOfSnowFall)
        } else {
            isSnowDataDisplayed = false
        }

        //setting rain precipitation data if data is present
        if (!isSnowDataDisplayed) {
            val precipitation = hourDataParser.getRainPrecipitationData()
            if (precipitation != null) {
                setRainFallDataWithAnimation(precipitation.first, precipitation.second)
            }
        }

        binding.tvWindDegreeDirection.text = "${hourDataParser.getWindDegree()} | ${hourDataParser.getWindDirection()}"
        binding.tvWindChill.text = hourDataParser.getWindChill()
        binding.tvWindSpeed.text = hourDataParser.getWindSpeed()
        binding.tvGustSpeed.text = hourDataParser.getGustSpeed()
        binding.tvHumidityPercentage.text = hourDataParser.getHumidityPercentage()
        binding.tvHeatIndex.text = hourDataParser.getHeatIndexTemperature()
        binding.tvUvStatus.text = hourDataParser.getUVIndex()
        binding.tvVisibiity.text = hourDataParser.getVisibility()
        binding.tvDewpointTemperature.text = hourDataParser.getDewPointTemperature()
        binding.tvPressure.text = hourDataParser.getPressure()
        binding.tvSkyCondition.text = hourDataParser.getSkyCondition()

    }

    private fun setSnowFallDataWithAnimation(chanceOfSnowfall: String) {
        binding.cvSnowData.visibility = View.VISIBLE
        binding.tvChanceOfSnowFall.text = chanceOfSnowfall
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

}