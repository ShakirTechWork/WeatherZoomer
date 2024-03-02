package com.example.weatherwish.ui.takelocation

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.weatherwish.R
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.Application
import com.example.weatherwish.MainActivity
import com.example.weatherwish.TurnOnGps
import com.example.weatherwish.broadcastReceivers.GpsStatusListener
import com.example.weatherwish.databinding.ActivityLocationBinding
import com.example.weatherwish.utils.Utils
import com.google.android.gms.location.*


private const val TAG = "LocationActivity"
class LocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationBinding

    private lateinit var locationViewModel: LocationViewModel

    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    val turnOnGps = TurnOnGps(this)

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
            Log.d(TAG, "Values:  isGpsOn: ${isGpsOn}    isLocationPermissionGranted: ${isLocationPermissionGranted}")
            if (!isLocationPermissionGranted && !isGpsOn) {
                Log.d(TAG, "we have to take the location permission and ask to turn on the gps ")
            } else {
                Log.d(TAG, "in observer else: ")
            }
        }

        binding.btnCurrentLocation.setOnClickListener {
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

        binding.btnEnterLocation.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_manual_location_dialog, null)
            dialogView.setBackgroundResource(R.drawable.dialog_background)
            val builder = AlertDialog.Builder(this)
            val dialog = builder.setView(dialogView).create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val textTitle = dialogView.findViewById<TextView>(R.id.tv_title)
            val edtLocation = dialogView.findViewById<EditText>(R.id.edt_location)
            val btnOK = dialogView.findViewById<Button>(R.id.btn_set_location)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            textTitle.text = "Type your location"
            btnOK.setOnClickListener {
                val location = edtLocation.text.toString()
                if (location.isNotBlank()) {
                    storeLocationAndNavigate(location)
                    dialog.dismiss()
                } else {
                    Utils.showLongToast(this@LocationActivity, "Please enter the location first.")
                }
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

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
                    "It looks like you have turned off permissions required for this feature. It can be enabled under\nPhone Settings > Apps > WeatherWish > Permissions\nPlease reopen the app.",
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
        Utils.showLongToast(this@LocationActivity,"Please wait while we take your current location")
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
                storeLocationAndNavigate("${locationResult.locations[0].latitude},${locationResult.locations[0].longitude}")
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun storeLocationAndNavigate(location: String) {
        locationViewModel.updateUserPrimaryLocation(location)
        val intent = Intent(this@LocationActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Remember to stop updates when they're no longer needed
    private fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: ")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}