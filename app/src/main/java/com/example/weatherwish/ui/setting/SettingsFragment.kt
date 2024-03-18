package com.example.weatherwish.ui.setting

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.weatherwish.Application
import com.example.weatherwish.BuildConfig
import com.example.weatherwish.R
import com.example.weatherwish.SharedViewModel
import com.example.weatherwish.constants.AppConstants
import com.example.weatherwish.constants.AppEnum
import com.example.weatherwish.databinding.FragmentSettingsBinding
import com.example.weatherwish.ui.signIn.SignInActivity
import com.example.weatherwish.utils.Utils


class SettingsFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = NavHostFragment.findNavController(this@SettingsFragment)
        val repository = (requireActivity().application as Application).appRepository
        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(repository)
        )[SettingsViewModel::class.java]

        binding.tvAppVersion.text = "${getString(R.string.app_version)} ${BuildConfig.VERSION_NAME}"
        attachClickListener()
    }

    private fun attachClickListener() {
        binding.clTopUnitLayout.setOnClickListener {
            if (binding.llBottomTemperatureLayout.isVisible) {
                binding.llBottomTemperatureLayout.visibility = View.GONE
                TransitionManager.beginDelayedTransition(binding.cdUnitLayout, AutoTransition())
                binding.imgTemperatureArrow.setImageResource(R.drawable.baseline_arrow_down_24)
            } else {
                TransitionManager.beginDelayedTransition(binding.cdUnitLayout, AutoTransition())
                binding.llBottomTemperatureLayout.visibility = View.VISIBLE
                binding.imgTemperatureArrow.setImageResource(R.drawable.baseline_arrow_up_24)
            }
        }

        binding.clMetricLayout.setOnClickListener {
            binding.imgMetricTick.setImageResource(R.drawable.tick_circle)
            binding.imgImperialTick.setImageResource(0)
            settingsViewModel.updateUserUnitPreference(AppConstants.UserPreferredUnit.METRIC)
        }

        binding.clImperialLayout.setOnClickListener {
            binding.imgImperialTick.setImageResource(R.drawable.tick_circle)
            binding.imgMetricTick.setImageResource(0)
            settingsViewModel.updateUserUnitPreference(AppConstants.UserPreferredUnit.IMPERIAL)
        }

        binding.cdThemeLayout.setOnClickListener {
            Utils.twoOptionAlertDialog(
                requireContext(),
                "Change App Theme",
                "Change app theme to light or dark mode. App will automatically restart in the selected theme.",
                "Light",
                "Dark",
                true,
                {
                    Utils.changeMode(AppEnum.LIGHTMODE)
//                    navController.popBackStack()
//                    requireActivity().finish()
//                    startActivity(Intent(requireActivity(),SplashActivity::class.java))
                },
                {
                    Utils.changeMode(AppEnum.DARKMODE)
                })
        }

        binding.cdSignoutLayout.setOnClickListener {
            Utils.twoOptionAlertDialog(
                requireContext(),
                "Confirmation",
                "Are you sure you want to sign out?",
                "Yes",
                "Cancel",
                true,
                {
                    settingsViewModel.signOutCurrentUser(requireActivity())
                    navController.popBackStack()
                    requireActivity().finish()
                    startActivity(Intent(requireActivity(), SignInActivity::class.java))
                },
                {})
        }

        /*binding.cdPeriodicLayout.setOnClickListener {
            navController.navigate(R.id.action_settings_to_weather_updates_fragment)
        }*/

    }

}