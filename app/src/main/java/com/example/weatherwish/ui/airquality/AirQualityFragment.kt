package com.example.weatherwish.ui.airquality

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.weatherwish.Application
import com.example.weatherwish.adapter.AirQualityDataAdapter
import com.example.weatherwish.api.ApiResponse
import com.example.weatherwish.api.ErrorResponse
import com.example.weatherwish.databinding.FragmentAirQualityBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val TAG = "AirQualityFragment"

class AirQualityFragment : Fragment() {
    private lateinit var location: String

    private var _binding: FragmentAirQualityBinding? = null
    private val binding get() = _binding!!

    private lateinit var airQualityViewModel: AirQualityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = it.getString("location").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAirQualityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pbLoading.visibility = View.VISIBLE
        binding.pbLoading.visibility = View.VISIBLE

        val appRepository = (requireActivity().application as Application).appRepository
        airQualityViewModel = ViewModelProvider(
            this,
            AirQualityViewModelFactory(appRepository)
        )[AirQualityViewModel::class.java]

//        airQualityViewModel.getAirQualityData(location)

        getCurrentWeatherData(location)

        attachObservers()

        attachListeners()

    }

    private fun getCurrentWeatherData(location: String) {
        lifecycleScope.launch {
            airQualityViewModel.getCurrentWeatherData(location, "yes").asLiveData().observe(viewLifecycleOwner) {
                when (it) {
                    is ApiResponse.Success -> {
                        Log.d(TAG, "getServerData: Success: ${it.data!!.location.region}")
                    }
                    is ApiResponse.Failure -> {
                        Log.d(TAG, "getServerData: Failure: ${it.message}   ${it.code}")
                    }
                    ApiResponse.Loading -> {
                        Log.d(TAG, "getServerData: Loading")
                    }
                }
            }
        }
    }

    private fun attachListeners() {

        binding.imgBack.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }
    }

    private fun attachObservers() {
        airQualityViewModel.airValuesListLiveData.observe(viewLifecycleOwner) {
            binding.pbLoading.visibility = View.VISIBLE
            binding.tvAirQuality.text = airQualityViewModel.airQualityLiveData.value
            binding.tvLocationTime.text = airQualityViewModel.locationTImeLiveData.value
            val adapter = AirQualityDataAdapter(it, requireContext())
            binding.rvAirValues.adapter = adapter
        }
    }
}