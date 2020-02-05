package com.robin729.aqi.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.robin729.aqi.Network.AqiApi
import com.robin729.aqi.model.Info
import com.robin729.aqi.R
import devlight.io.library.ArcProgressStackView
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {


    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableMyLocation()

        val mLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {
                info(p0?.latitude, p0?.longitude)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

            }

            override fun onProviderEnabled(p0: String?) {
               Log.e("TAG", p0)
            }

            override fun onProviderDisabled(p0: String?) {
                checkDeviceLocationSettings(false)
            }
        }


        val mLocationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 1000f, mLocationListener)

    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            checkDeviceLocationSettings(false)
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
//                Snackbar.make(
//                    baseContext.,
//                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettings()
//                }.show()

                checkDeviceLocationSettings()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Log.e("TAG", "success")
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    fun info(lat: Double?, long: Double?){
        CoroutineScope(Dispatchers.IO).launch {
            val request = AqiApi().initalizeRetrofit()
                .getApi("current-conditions?lat=$lat&lon=$long&key=db5f91ec30974513a36466bdb2fe8c52&features=breezometer_aqi,local_aqi,health_recommendations,sources_and_effects,pollutants_concentrations,pollutants_aqi_information")

            withContext(Dispatchers.IO) {
                try {

                    request.enqueue(object : Callback<Info> {

                        override fun onResponse(call: Call<Info>, response: Response<Info>) {
                            Log.e("TAG", "${response.body()?.data?.index?.details?.aqi}")
                            val a = response.body()?.data?.index?.details?.aqi
                            aqi.text = response.body()?.data?.index?.details?.aqi.toString()
                            val models: ArrayList<ArcProgressStackView.Model> = ArrayList()
                            if (a != null) {
                                aqi.text = a.toString()
                                val fl = ((a/999)*100).toFloat()
                                models.add(ArcProgressStackView.Model(" ", fl, resources.getColor(
                                    R.color.bg
                                ), resources.getColor(R.color.colorAccent)))
                            }
                            arc.models = models
                        }

                        override fun onFailure(call: Call<Info>, t: Throwable) {
                            Log.e("TAG", "fail")
                        }


                    })
                } catch (e: Exception) {
                    Log.e(
                        "MainActicity",
                        "Exception ${e.message}"
                    )
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettings(false)
        }
        
    }
}
