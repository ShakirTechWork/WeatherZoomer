package com.shakir.weatherzoomer.ui.takelocation

import com.shakir.weatherzoomer.R
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.Application
import com.shakir.weatherzoomer.MainActivity
import com.shakir.weatherzoomer.TurnOnGps
import com.shakir.weatherzoomer.broadcastReceivers.GpsStatusListener
import com.shakir.weatherzoomer.databinding.ActivityLocationBinding
import com.shakir.weatherzoomer.extensionFunctions.setSafeOnClickListener
import com.shakir.weatherzoomer.utils.GifProgressDialog
import com.shakir.weatherzoomer.utils.Utils
import com.google.android.gms.location.*
import com.shakir.weatherzoomer.firebase.FirebaseResponse


private const val TAG = "LocationActivity"
class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    private lateinit var locationViewModel: LocationViewModel

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val turnOnGps = TurnOnGps(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as Application).appRepository

        locationViewModel = ViewModelProvider(this, LocationViewModelFactory(repository))[LocationViewModel::class.java]


        locationViewModel.updateLocationPermissionStatus(Utils.checkFineLocationPermission(this))
        val gpsStatusListener = GpsStatusListener(this)
        var isGpsStatusChanged: Boolean? = null
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@LocationActivity)

        gpsStatusListener.observe(this) { isGpsOn ->
            if (isGpsStatusChanged == null) {
                if (!isGpsOn) {
                    locationViewModel.updateGpsStatus(false)
                } else {
                    locationViewModel.updateGpsStatus(true)
                }
                isGpsStatusChanged = isGpsOn
            } else {
                if (isGpsStatusChanged != isGpsOn) {
                    if (!isGpsOn) {
                        locationViewModel.updateGpsStatus(false)
                    } else {
                        locationViewModel.updateGpsStatus(true)
                    }
                    isGpsStatusChanged = isGpsOn
                }
            }

        }

        locationViewModel.gpsLocationLiveData.observe(this) {
            val isGpsOn = it.isGpsOn
            val isLocationPermissionGranted = it.isLocationPermissionGranted
            Log.d(TAG, "Values:  isGpsOn: $isGpsOn    isLocationPermissionGranted: $isLocationPermissionGranted")
            if (!isLocationPermissionGranted && !isGpsOn) {
                Log.d(TAG, "we have to take the location permission and ask to turn on the gps ")
            } else {
                Log.d(TAG, "in observer else: ")
            }
        }

        binding.btnCurrentLocation.setSafeOnClickListener {
            val isLocationPermissionGranted = locationViewModel.gpsLocationLiveData.value!!.isLocationPermissionGranted
            val isGpsOn = locationViewModel.gpsLocationLiveData.value!!.isGpsOn
            if (isLocationPermissionGranted && isGpsOn) {
                Log.d(TAG, "we can take the location: ")
                requestLocationUpdates()
            } else if (!isLocationPermissionGranted) {
                Log.d(TAG, "we can take the location permission: ")
                requestPermission.launch(Utils.permission)
            } else if (!isGpsOn) {
                turnOnGps.startGps(resultLauncher)
            } else {
                Log.d(TAG, "in Button else: ")
            }
        }

        binding.tvSearchLocation.setSafeOnClickListener {
            val bottomSheet = SearchLocationFragment.newInstance()
            bottomSheet.setLocationSelectionListener(object: SearchLocationFragment.OnLocationSelectedListener{
                override fun onLocationSelected(location: String) {
                    locationViewModel.saveLocation(location,
                        isCurrentLocation = false,
                        isSelectedLocation = true
                    )
                }

            })
            bottomSheet.show(supportFragmentManager, SearchLocationFragment.TAG)
        }

        //new implementation
        attachObservers()

    }

    private fun attachObservers() {
        locationViewModel.isLocationSavedLiveData.observe(this@LocationActivity) {saveLocationResponse ->
            when (saveLocationResponse) {
                is FirebaseResponse.Success -> {
                    GifProgressDialog.dismiss()
                    val intent = Intent(this@LocationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is FirebaseResponse.Failure -> {

                }
                FirebaseResponse.Loading -> {

                }
            }
        }
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_hourly_weather_data_dialog, null)

        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomAlertDialog1)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        // Set up the views in the dialog
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

        dialogTitle.text = "New Title"
        dialogMessage.text = "New custom message here."
        dialogButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->

        if (activityResult.resultCode == RESULT_OK) {
            requestLocationUpdates()
        } else if (activityResult.resultCode == RESULT_CANCELED) {
            Log.d(TAG, "request to turn on the gps is cancelled")
        }

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

                Log.i(TAG, "permission granted")
                locationViewModel.updateLocationPermissionStatus(true)
                if (!locationViewModel.gpsLocationLiveData.value!!.isGpsOn) {
                    turnOnGps.startGps(resultLauncher)
                } else {
                    Log.d(TAG, "we can take the location: ")
                    requestLocationUpdates()
                }
            } else {
                // Permission denied, handle accordingly
                Log.i(TAG, "permission denied")
                locationViewModel.updateLocationPermissionStatus(false)
                Utils.singleOptionAlertDialog(
                    this,
                    "Need Location Access",
                    "It looks like you have turned off permissions required for this feature. It can be enabled under\nPhone Settings > Apps > WeatherZoomer > Permissions\nPlease reopen the app.",
                    "GO TO SETTINGS",
                    true
                ) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }


    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        GifProgressDialog.initialize(this@LocationActivity)
        GifProgressDialog.show(getString(R.string.please_wait_taking_ur_location))
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)

        locationCallback = object : LocationCallback() {

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                Log.d(TAG, "onLocationAvailability: ${locationAvailability.isLocationAvailable}")
            }

            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult: $locationRequest")
                stopLocationUpdates()
                locationViewModel.saveLocation("${locationResult.locations[0].latitude},${locationResult.locations[0].longitude}",
                    isCurrentLocation = true,
                    isSelectedLocation = true
                )
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // Remember to stop updates when they're no longer needed
    private fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: ")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}