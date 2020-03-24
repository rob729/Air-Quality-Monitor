package com.robin729.aqi.fragment


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.robin729.aqi.AqiViewModel
import com.robin729.aqi.R
import com.robin729.aqi.util.Util
import com.robin729.aqi.util.Util.getColorRes
import kotlinx.android.synthetic.main.fragment_main.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
val ANIMATION_DURATION = 1000.toLong()

class MainFragment : Fragment() {

    private val aqiViewModel: AqiViewModel by lazy {
        ViewModelProvider(this).get(AqiViewModel::class.java)
    }

    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(context!!)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (Util.hasNetwork(context)) {
                val newLat = BigDecimal(p0?.locations?.get(0)?.latitude!!).setScale(
                    2,
                    RoundingMode.HALF_EVEN
                ).toDouble()
                val newLong =
                    BigDecimal(p0.locations[0]?.longitude!!).setScale(2, RoundingMode.HALF_EVEN)
                        .toDouble()
                if (lat != newLat && long != newLong) {
                    Log.e("TAG", "$lat and $newLat")
                    Log.e("TAG", "$long and $newLong")
                    lat = newLat
                    long = newLong
                    aqiViewModel.fetchRepos(lat, long, geocoder)
                    aqiViewModel.fetchWeather(lat, long)
                }
            }
        }
    }

    var lat: Double = 0.00
    var long: Double = 0.00

    private val locationRequest: LocationRequest by lazy { LocationRequest() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        locationCheck()
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txt_no2.text =
                Html.fromHtml("NO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            txt_so2.text =
                Html.fromHtml("SO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            txt_no2.text = Html.fromHtml("NO<sub><small>2</small></sub>")
            txt_so2.text = Html.fromHtml("SO<sub><small>2</small></sub>")
        }

        handleNetworkChanges()

        aqiViewModel.loading.observe(viewLifecycleOwner, Observer {
            parent_layout.visibility = if (it) View.INVISIBLE else View.VISIBLE
            loading.visibility = if (it) View.VISIBLE else View.INVISIBLE
        })

        aqiViewModel.location.observe(viewLifecycleOwner, Observer {
            location.text = resources.getString(R.string.location, it)
        })

        aqiViewModel.weather.observe(viewLifecycleOwner, Observer {
            Log.e("TAG", "${it.time} time")
            weather_icon.setImageResource(Util.getArtForWeatherCondition(it.weather[0].id))
            temp.text = resources.getString(R.string.temp, it.main.temp.toString())
            date.text = Util.formatDate(it.time)
            weather_description.text = it.weather[0].desp
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
    }

    private fun locationCheck() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            checkDeviceLocationSettings()
        } else {
            Log.e("TAG", "failed")
        }
    }


    private fun checkDeviceLocationSettings() {
        locationRequest.run {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            fastestInterval = 30000
            interval = 60000
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsResponseTask = LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnCompleteListener {
            Log.e("TAG", "location success")
            it.addOnSuccessListener { it1 ->
                val states = it1.locationSettingsStates
                if (states.isLocationPresent) {
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    Log.e("TAG", "success")
                }
            }
        }


        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
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

//        locationSettingsResponseTask.addOnCompleteListener {
//
//            Log.e("TAG", "complete")
//            if (it.isSuccessful) {
//                fusedLocationProviderClient.requestLocationUpdates(
//                    locationRequest,
//                    locationCallback,
//                    Looper.getMainLooper()
//                )
//                Log.e("TAG", "success")
//            } else {
//                Snackbar.make(
//                    parent_layout,
//                    resources.getString(R.string.location_required_error),
//                    Snackbar.LENGTH_INDEFINITE
//                ).setAction(android.R.string.ok) {
//                    checkDeviceLocationSettings()
//                }.setActionTextColor(Color.rgb(17, 122, 101)).show()
//            }
//        }
    }

    private fun handleNetworkChanges() {
        Util.getNetworkLiveData(context!!).observe(viewLifecycleOwner, Observer { isConnected ->
            if (!isConnected) {

                loading.visibility = View.GONE
                textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                networkStatusLayout.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                    animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(null)
                }
            } else {

                loading.visibility = View.VISIBLE
                textViewNetworkStatus.text = getString(R.string.text_connectivity)
                networkStatusLayout.apply {
                    setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                    animate()
                        .alpha(0f)
                        .setStartDelay(ANIMATION_DURATION)
                        .setDuration(ANIMATION_DURATION)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                visibility = View.GONE
                            }
                        })
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("TAG", "result")
//        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
//            Log.e("TAG", " Activity Result")
//        } else {
//            Log.e("TAG", " Activity Result not")
//        }

        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.e("TAG", " Activity Result")
                }


                Activity.RESULT_CANCELED -> {
                    Log.e("TAG", " Activity Result not")
                }
            }
            else -> {
                Log.e("TAG", " Activity Result")
            }
        }

    }

}
