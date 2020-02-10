package com.robin729.aqi.fragment


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.robin729.aqi.AqiViewModel
import com.robin729.aqi.R
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

private val REQUEST_LOCATION_PERMISSION = 1
private val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class MainFragment : Fragment() {

    private val aqiViewModel: AqiViewModel by lazy {
        ViewModelProvider(this).get(AqiViewModel::class.java)
    }

    val geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableMyLocation()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txt_no2.text = Html.fromHtml("NO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            //txt_o3.text = Html.fromHtml("O<sub><small>3</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            txt_so2.text = Html.fromHtml("SO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            txt_no2.text = Html.fromHtml("NO<sub><small>2</small></sub>")
            //txt_o3.text = Html.fromHtml("O<sub><small>3</small></sub>")
            txt_so2.text = Html.fromHtml("SO<sub><small>2</small></sub>")
        }

        val mLocationManager: LocationManager =
            context?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        val mLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {
                aqiViewModel.fetchRepos(p0?.latitude, p0?.longitude, geocoder)
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

        aqiViewModel.loading.observe(viewLifecycleOwner, Observer {
            swipe_refresh.isRefreshing = it
            parent_layout.visibility = if(it) View.INVISIBLE else View.VISIBLE
        })

        aqiViewModel.location.observe(viewLifecycleOwner, Observer {
            location.text = resources.getString(R.string.location, it)
        })

        aqiViewModel.aqi.observe(viewLifecycleOwner, Observer {
            Log.e("TAG", "observe")
            aqi.text = it.data.index.details.aqi.toString()
            category.text = it.data.index.details.category
            card_view.setCardBackgroundColor(Color.parseColor(it.data.index.details.color))
            co.text = resources.getString(
                R.string.conc,
                it.data.pollutants.co.concentration.value.toString(),
                it.data.pollutants.co.concentration.units
            )
            no2.text = resources.getString(
                R.string.conc,
                it.data.pollutants.no2.concentration.value.toString(),
                it.data.pollutants.no2.concentration.units
            )
            pm10.text = resources.getString(
                R.string.conc,
                it.data.pollutants.pm10.concentration.value.toString(),
                it.data.pollutants.pm10.concentration.units
            )
            pm25.text = resources.getString(
                R.string.conc,
                it.data.pollutants.pm25.concentration.value.toString(),
                it.data.pollutants.pm25.concentration.units
            )
            so2.text = resources.getString(
                R.string.conc,
                it.data.pollutants.so2.concentration.value.toString(),
                it.data.pollutants.so2.concentration.units
            )
            general_recom.text = it.data.recommendations.general
            Log.e("TAG", it.data.index.details.color + "vv")
        })

        mLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            1000f,
            mLocationListener
        )

    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            checkDeviceLocationSettings(false)
        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(activity!!)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activity!!,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("TAG", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    parent_layout,
                    resources.getString(R.string.location_required_error),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.setActionTextColor(Color.rgb(17, 122, 101)).show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("TAG", "success")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
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
