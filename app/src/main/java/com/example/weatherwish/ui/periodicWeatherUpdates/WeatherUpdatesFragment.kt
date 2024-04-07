package com.example.weatherwish.ui.periodicWeatherUpdates

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.weatherwish.Application
import com.example.weatherwish.R
import com.example.weatherwish.SharedViewModel
import com.example.weatherwish.adapter.TimeAdapter
import com.example.weatherwish.broadcastReceivers.WeatherUpdateReceiver
import com.example.weatherwish.databinding.FragmentWeatherUpdatesBinding
import com.example.weatherwish.model.SelectedTimeModel
import com.example.weatherwish.utils.Utils
import com.example.weatherwish.workManager.WeatherUpdatesWorker
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class WeatherUpdatesFragment : Fragment(), TimeAdapter.OnTimeItemClickListener {

    private lateinit var navController: NavController

    private var _binding: FragmentWeatherUpdatesBinding? = null
    private val binding get() = _binding!!

    private lateinit var weatherUpdatesViewModel: WeatherUpdatesViewModel

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var alarmList: ArrayList<SelectedTimeModel> = arrayListOf()
    private var periodicUpdateIntervalInHours: Int? = null
    private var dndStartTime: Long? = null
    private var dndEndTime: Long? = null

    private var periodicState = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWeatherUpdatesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = NavHostFragment.findNavController(this@WeatherUpdatesFragment)
        val repository = (requireActivity().application as Application).appRepository
        weatherUpdatesViewModel = ViewModelProvider(
            this,
            WeatherUpdatesViewModelFactory(repository)
        )[WeatherUpdatesViewModel::class.java]

        attachObservers()

        val userData = sharedViewModel.userData
        Utils.printDebugLog("user_data: $userData")
        val weatherUpdatesData = userData?.user_settings?.weather_updates
        if (weatherUpdatesData != null) {
//            weatherUpdatesViewModel.updateTypeMutableLiveData.value = weatherUpdatesData.update_type

            if (weatherUpdatesData.hourly_interval != 0) {
                if (weatherUpdatesData.update_type.equals("periodic")) {
                    Utils.printDebugLog("jgedhcfhec")
                    weatherUpdatesViewModel.setPeriodicButtonState("Saved And Disabled")
                } else {
                    weatherUpdatesViewModel.setPeriodicButtonState("Saved And Enabled")
                }
                periodicUpdateIntervalInHours = weatherUpdatesData.hourly_interval
                if (weatherUpdatesData.dnd_start_time.isNotBlank() && weatherUpdatesData.dnd_end_time.isNotBlank()) {
                    binding.tvSelectedDndTime.visibility = View.VISIBLE
                    binding.tvSelectedDndTime.text =
                        "DND will start at ${Utils.convertUnixToHourTime(weatherUpdatesData.dnd_start_time.toLong())} and end at ${
                            Utils.convertUnixToHourTime(weatherUpdatesData.dnd_end_time.toLong())
                        }"
                }
            } else {
                weatherUpdatesViewModel.setPeriodicButtonState("Unsaved And Disabled")
            }

            if (weatherUpdatesData.time_list.isNotEmpty()) {
                alarmList = weatherUpdatesData.time_list
                binding.rvTime.adapter =
                    TimeAdapter(alarmList, requireContext(), this@WeatherUpdatesFragment)
            }

            if (weatherUpdatesData.update_type.equals("timely")) {
                binding.btnSetTimelyUpdates.text = "Disable"
            } else {
                binding.btnSetTimelyUpdates.text = "Enable"
            }

        }

        binding.cdPeriodicLayout.setOnClickListener {
            if (binding.llBottomPeriodicLayout.isVisible) {
                binding.llBottomPeriodicLayout.visibility = View.GONE
                TransitionManager.beginDelayedTransition(binding.cdPeriodicLayout, AutoTransition())
                binding.imgPeriodicUpdatesArrow.setImageResource(R.drawable.baseline_arrow_down_24)
            } else {
                TransitionManager.beginDelayedTransition(binding.cdPeriodicLayout, AutoTransition())
                binding.llBottomPeriodicLayout.visibility = View.VISIBLE
                binding.imgPeriodicUpdatesArrow.setImageResource(R.drawable.baseline_arrow_up_24)
            }
        }

        binding.cdTimelyUpdates.setOnClickListener {
            if (binding.llBottomTimelyLayout.isVisible) {
                binding.llBottomTimelyLayout.visibility = View.GONE
                TransitionManager.beginDelayedTransition(binding.cdTimelyUpdates, AutoTransition())
                binding.imgTimelyUpdatesArrow.setImageResource(R.drawable.baseline_arrow_down_24)
            } else {
                TransitionManager.beginDelayedTransition(binding.cdPeriodicLayout, AutoTransition())
                binding.llBottomTimelyLayout.visibility = View.VISIBLE
                binding.imgTimelyUpdatesArrow.setImageResource(R.drawable.baseline_arrow_up_24)
            }
        }

        binding.btnSetIntervalUpdates.setOnClickListener {
            if (periodicUpdateIntervalInHours != null && periodicUpdateIntervalInHours != 0) {
                Utils.printDebugLog("setting_periodic_weather_updates on_user_choice")
                when (weatherUpdatesViewModel.periodicButtonStateLiveData.value) {
                    "Saved And Disabled" -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            // Check if the worker is already working
//                    val workInfos = WorkManager.getInstance(requireActivity().applicationContext)
//                        .getWorkInfosByTag("WeatherUpdatesWorker").await()
//                    Utils.printDebugLog("workInfos: ${workInfos}")
//                    if (workInfos.any { it.state == WorkInfo.State.RUNNING }) {
                            Utils.printDebugLog("Cancelling already running worker and setting a new worker.")
                            val uniqueWorkName = WeatherUpdatesWorker::class.java.simpleName
                            WorkManager.getInstance(requireContext()).cancelUniqueWork(uniqueWorkName)
//                    }
                            val weatherWorkRequest =
                                PeriodicWorkRequest.Builder(
                                    WeatherUpdatesWorker::class.java,
                                    periodicUpdateIntervalInHours!!.toLong(),
                                    TimeUnit.MINUTES
                                ).addTag("WeatherUpdatesWorker").build()
                            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                                "WeatherUpdatesWorker",
                                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                weatherWorkRequest
                            )
                        }
                    }

                    "Saved And Enabled" -> {
                        Utils.printDebugLog("Cancelling already running worker and setting a new worker.")
                        val uniqueWorkName = WeatherUpdatesWorker::class.java.simpleName
                        WorkManager.getInstance(requireContext()).cancelUniqueWork(uniqueWorkName)
                    }

                    "Unsaved And Disabled" -> {
                        weatherUpdatesViewModel.updatePeriodicWeatherUpdatesData(
                            periodicUpdateIntervalInHours!!, dndStartTime.toString(), dndEndTime.toString()
                        )
                        viewLifecycleOwner.lifecycleScope.launch {
                            // Check if the worker is already working
//                    val workInfos = WorkManager.getInstance(requireActivity().applicationContext)
//                        .getWorkInfosByTag("WeatherUpdatesWorker").await()
//                    Utils.printDebugLog("workInfos: ${workInfos}")
//                    if (workInfos.any { it.state == WorkInfo.State.RUNNING }) {
                            Utils.printDebugLog("Cancelling already running worker and setting a new worker.")
                            val uniqueWorkName = WeatherUpdatesWorker::class.java.simpleName
                            WorkManager.getInstance(requireContext()).cancelUniqueWork(uniqueWorkName)
//                    }
                            val weatherWorkRequest =
                                PeriodicWorkRequest.Builder(
                                    WeatherUpdatesWorker::class.java,
                                    periodicUpdateIntervalInHours!!.toLong(),
                                    TimeUnit.MINUTES
                                ).addTag("WeatherUpdatesWorker").build()
                            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                                "WeatherUpdatesWorker",
                                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                                weatherWorkRequest
                            )
                        }
                    }
                }
            } else {
                Utils.showShortToast(requireContext(), "Please select the interval first.")
            }
            val builder =
                android.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                    .create()
            val view = layoutInflater.inflate(R.layout.periodic_updates_setting_dialog, null)
            val spinnerHourlyInterval = view.findViewById<Spinner>(R.id.spinner_updates_interval)
            val chkboxDndTime = view.findViewById<CheckBox>(R.id.chkbox_dnd_time)
            val tvSelectedDndTime = view.findViewById<TextView>(R.id.tv_selected_dnd_time)
            val btnSetIntervalUpdates = view.findViewById<Button>(R.id.btn_set_interval_updates)
//            val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
//            tpDndStartTime.setIs24HourView(false)
//            tpDndEndTime.setIs24HourView(false)
//            tvAddTime.setOnClickListener {
//                val (dndStartTime, dndStartTimeReadable) = returnTime(tpDndStartTime)
//                val (dndEndTime, dndEndTimeReadable) = returnTime(tpDndEndTime)
//                if (dndStartTimeReadable == dndEndTimeReadable) {
//                    tvErrorMessage.text = "Starting time and ending time should not be same."
//                    return@setOnClickListener
//                }
//                this.dndStartTime = dndStartTime
//                this.dndEndTime = dndEndTime
//                binding.tvSelectedDndTime.visibility = View.VISIBLE
//                binding.tvSelectedDndTime.text =
//                    "DND will start at $dndStartTimeReadable and end at $dndEndTimeReadable"
//                builder.dismiss()
//            }
//            tvCancel.setOnClickListener {
//                binding.chkboxDndTime.isChecked = false
//                builder.dismiss()
//            }
            builder.setView(view)
            builder.setCanceledOnTouchOutside(false)
            builder.setCancelable(false)
            builder.show()
        }

        binding.imgAddTime.setOnClickListener {
            val mTimePicker: TimePickerDialog
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mcurrentTime.get(Calendar.MINUTE)

            mTimePicker =
                TimePickerDialog(requireContext(), object : TimePickerDialog.OnTimeSetListener {
                    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                        if (alarmList.size < 3) {
                            val timeInMillis = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            alarmList.add(SelectedTimeModel(timeInMillis, "$hourOfDay:$minute"))
//                            val adapter = TimeAdapter(alarmList, requireContext(), this@WeatherUpdatesFragment)
//                            binding.rvTime.adapter = adapter
                            binding.rvTime.adapter?.notifyDataSetChanged()
                        } else {
                            Utils.showLongToast(requireContext(), "You can add only three times.")
                        }
                        Log.d("TAG", "onTimeSet: ${String.format("%d : %d", hourOfDay, minute)}")
                    }
                }, hour, minute, false)
            mTimePicker.show()
        }

//        binding.btnCancelAllWorker.setOnClickListener {
//            val uniqueWorkName = WeatherUpdatesWorker::class.java.simpleName
//            WorkManager.getInstance(requireContext()).cancelUniqueWork(uniqueWorkName)
//        }

        setPeriodicUpdatesAdapter()

        binding.btnSetTimelyUpdates.setOnClickListener {
            if (alarmList.isNotEmpty()) {
                Utils.printDebugLog("cancelling_previously_timely_updates_if_present")
//                cancelAllAlarms(requireContext())
                Utils.printDebugLog("setting_timely_updates")
                for (time in alarmList) {
//                    scheduleNotification(
//                        requireContext(),
//                        time.time_in_millis,
//                        time.time_in_millis.toInt()
//                    )
                }
                Utils.printDebugLog("saving_timely_updates_in_firebase")
                weatherUpdatesViewModel.updateTimelyWeatherUpdatesData(alarmList)
            } else {
                Utils.showLongToast(requireContext(), "Add time first.")
            }
        }

    }

    private fun attachObservers() {
        weatherUpdatesViewModel.updateTypeLiveData.observe(viewLifecycleOwner) {
            if (it.equals("timely")) {

            } else if (it.equals("periodic")) {

            } else {

            }
        }

        weatherUpdatesViewModel.periodicButtonStateLiveData.observe(viewLifecycleOwner) {
            when (periodicState) {
                "Saved And Disabled" -> {
                    binding.btnSetIntervalUpdates.setText("Disable")
                }

                "Saved And Enabled" -> {
                    binding.btnSetIntervalUpdates.text = "Enable"
                }

                "Unsaved And Disabled" -> {
                    binding.btnSetIntervalUpdates.text = "Enable"
                }
            }
        }
    }

    private fun setPeriodicUpdatesAdapter() {
        val updateIntervalsList = resources.getStringArray(R.array.update_intervals)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, updateIntervalsList
        )
//        binding.spinnerUpdatesInterval.adapter = adapter
//
//        binding.spinnerUpdatesInterval.setSelection(
//            when (periodicUpdateIntervalInHours) {
//                4 -> {
//                    1
//                }
//
//                5 -> {
//                    2
//                }
//
//                6 -> {
//                    3
//                }
//
//                else -> {
//                    0
//                }
//            }
//        )
//
//        binding.spinnerUpdatesInterval.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View, position: Int, id: Long
//            ) {
//                periodicUpdateIntervalInHours = when (position) {
//                    1 -> {
//                        4
//                    }
//
//                    2 -> {
//                        5
//                    }
//
//                    3 -> {
//                        6
//                    }
//
//                    else -> {
//                        0
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                periodicUpdateIntervalInHours = null
//            }
//        }
//
//        binding.chkboxDndTime.setOnCheckedChangeListener { compoundButton, b ->
//            if (b) {
//                val builder =
//                    android.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
//                        .create()
//                val view = layoutInflater.inflate(R.layout.do_not_disturb_time_dialog, null)
//                val tpDndStartTime = view.findViewById<TimePicker>(R.id.tp_dnd_start_time)
//                val tpDndEndTime = view.findViewById<TimePicker>(R.id.tp_dnd_end_time)
//                val tvErrorMessage = view.findViewById<TextView>(R.id.tv_error_message)
//                val tvAddTime = view.findViewById<TextView>(R.id.tvAddTime)
//                val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
//                tpDndStartTime.setIs24HourView(false)
//                tpDndEndTime.setIs24HourView(false)
//                tvAddTime.setOnClickListener {
//                    val (dndStartTime, dndStartTimeReadable) = returnTime(tpDndStartTime)
//                    val (dndEndTime, dndEndTimeReadable) = returnTime(tpDndEndTime)
//                    if (dndStartTimeReadable == dndEndTimeReadable) {
//                        tvErrorMessage.text = "Starting time and ending time should not be same."
//                        return@setOnClickListener
//                    }
//                    this.dndStartTime = dndStartTime
//                    this.dndEndTime = dndEndTime
//                    binding.tvSelectedDndTime.visibility = View.VISIBLE
//                    binding.tvSelectedDndTime.text =
//                        "DND will start at $dndStartTimeReadable and end at $dndEndTimeReadable"
//                    builder.dismiss()
//                }
//                tvCancel.setOnClickListener {
//                    binding.chkboxDndTime.isChecked = false
//                    builder.dismiss()
//                }
//                builder.setView(view)
//                builder.setCanceledOnTouchOutside(false)
//                builder.setCancelable(false)
//                builder.show()
//            } else {
//                binding.tvSelectedDndTime.text = ""
//                binding.tvSelectedDndTime.visibility = View.GONE
//            }
//        }
    }

    private fun returnTime(tpDndStartTime: TimePicker): Pair<Long, String> {
        var dndStartTime = 0L
        var dndStartingHour = tpDndStartTime.hour
        val dndStartingMinute = tpDndStartTime.minute
        var dndStartingAmPm = ""
        when {
            dndStartingHour == 0 -> {
                dndStartingHour = dndStartingHour + 12
                dndStartingAmPm = "AM"
            }

            dndStartingHour == 12 -> {
                dndStartingAmPm = "PM"
            }

            dndStartingHour > 12 -> {
                dndStartingHour = dndStartingHour - 12
                dndStartingAmPm = "PM"
            }

            else -> dndStartingAmPm = "AM"
        }
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar[Calendar.HOUR_OF_DAY] = dndStartingHour
        calendar[Calendar.MINUTE] = dndStartingMinute
        calendar[Calendar.SECOND] = 0
        dndStartTime = calendar.timeInMillis
        Utils.printDebugLog(
            "${
                Pair(
                    dndStartTime,
                    "$dndStartingHour:$dndStartingMinute $dndStartingAmPm"
                )
            }  ---  ${System.currentTimeMillis()}"
        )
        return Pair(dndStartTime, "$dndStartingHour:$dndStartingMinute $dndStartingAmPm")
    }

    private fun scheduleNotification(
        context: Context,
        notificationTimeMillis: Long,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeatherUpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        try {
            // Use setExact for precise timing
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle the exception, request the necessary permission, or provide guidance to the user
            // For example, you might prompt the user to go to battery optimization settings
            // and exclude your app from battery optimizations.
            // Keep in mind that handling permissions properly is crucial to avoid crashes.
            Utils.printErrorLog("exception_in_setting_alarm: ${e.message}")
        }
    }

    private fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeatherUpdateReceiver::class.java)

        for (requestCode in 0 until 3) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    override fun onDeleteItem(index: Int) {
        if (index < alarmList.size) {
            alarmList.removeAt(index)
            binding.rvTime.adapter?.notifyItemRemoved(index)
            binding.rvTime.adapter?.notifyDataSetChanged()
        }
    }

}