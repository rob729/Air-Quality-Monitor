package com.robin729.aqi.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Html
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.like.LikeButton
import com.like.OnLikeListener
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.parse.ParseInstallation
import com.robin729.aqi.R
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Result
import com.robin729.aqi.data.model.weather.WeatherData
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.Constants.AUTOCOMPLETE_REQUEST_CODE
import com.robin729.aqi.utils.PermissionUtils
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util
import com.robin729.aqi.utils.Util.getColorRes
import com.robin729.aqi.viewmodel.AqiViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MainFragment : Fragment() {

    private val aqiViewModel: AqiViewModel by lazy {
        ViewModelProvider(this).get(AqiViewModel::class.java)
    }

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(requireContext())
    }

    private val favouritesLatLngList: HashSet<LatLng> by lazy {
        StoreSession.readFavouritesLatLng(Constants.FAVOURITES_LIST)
    }

    private val input: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    }.apply {
        this.value.timeZone = TimeZone.getTimeZone("UTC")
    }
    private val output: SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm", Locale.ENGLISH)
    }.apply {
        this.value.timeZone = Calendar.getInstance().timeZone
    }

    private var placeSearched = false

    var lat: Double = 0.00
    var long: Double = 0.00

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (Util.hasNetwork(context) && !placeSearched) {
                val newLat =
                    (p0?.locations?.get(0)?.latitude!!).toBigDecimal().setScale(2, RoundingMode.UP)
                        .toDouble()
                val newLong =
                    (p0.locations[0]?.longitude!!).toBigDecimal().setScale(2, RoundingMode.UP)
                        .toDouble()
                if (lat != newLat && long != newLong) {
                    lat = newLat
                    long = newLong
                    fetchData(LatLng(lat, long))
                }
            }
        }
    }

    private val locationRequest: LocationRequest by lazy { LocationRequest() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).actionBar?.setDisplayShowTitleEnabled(false)

        ParseInstallation.getCurrentInstallation().saveInBackground()

        @Suppress("DEPRECATION")
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

        aqiViewModel.location.observe(viewLifecycleOwner, {
            location.text = resources.getString(R.string.location, it)
        })

        aqiViewModel.aqi.observe(viewLifecycleOwner, {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    setAQIData(it.data!!)
                    parent_layout.visibility = View.VISIBLE
                    loading.visibility = View.GONE
                    error.visibility = View.GONE
                    favButton.isLiked = favouritesLatLngList.contains(LatLng(lat, long))
                }

                Resource.Status.LOADING -> {
                    error.visibility = View.GONE
                    parent_layout.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                }

                Resource.Status.ERROR -> {
                    parent_layout.visibility = View.GONE
                    loading.visibility = View.GONE
                    error.visibility = View.VISIBLE
                }
            }
        })

        aqiViewModel.predictionData.observe(viewLifecycleOwner, { predictionData ->
            when (predictionData.status) {
                Resource.Status.SUCCESS -> {
                    setPredictionData(predictionData)
                }
                else -> {
                }
            }

        })

        aqiViewModel.weather.observe(viewLifecycleOwner, {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    setWeatherData(it.data!!)
                }
                else -> {
                }
            }
        })

        favButton.setOnLikeListener(object : OnLikeListener {
            override fun liked(likeButton: LikeButton?) {
                favouritesLatLngList.add(LatLng(lat, long))
            }

            override fun unLiked(likeButton: LikeButton?) {
                favouritesLatLngList.remove(LatLng(lat, long))
            }

        })

    }

    override fun onStart() {
        super.onStart()
        if (PermissionUtils.isLocationEnabled(requireContext())) {
            getLocationUpdates()
        } else {
            PermissionUtils.showGPSNotEnableDialog(requireContext())
        }
    }

    override fun onPause() {
        super.onPause()
        StoreSession.write(Constants.FAVOURITES_LIST, favouritesLatLngList)
    }

    private fun setAQIData(info: Info) {
        aqi.text = info.data.index.details.aqi.toString()
        category.text = info.data.index.details.category
        card_view.setCardBackgroundColor(Color.parseColor(info.data.index.details.color))
        co.text = resources.getString(
            R.string.conc,
            info.data.pollutants.co.concentration.value.toString(),
            info.data.pollutants.co.concentration.units
        )
        no2.text = resources.getString(
            R.string.conc,
            info.data.pollutants.no2.concentration.value.toString(),
            info.data.pollutants.no2.concentration.units
        )
        pm10.text = resources.getString(
            R.string.conc,
            info.data.pollutants.pm10.concentration.value.toString(),
            info.data.pollutants.pm10.concentration.units
        )
        pm25.text = resources.getString(
            R.string.conc,
            info.data.pollutants.pm25.concentration.value.toString(),
            info.data.pollutants.pm25.concentration.units
        )
        so2.text = resources.getString(
            R.string.conc,
            info.data.pollutants.so2.concentration.value.toString(),
            info.data.pollutants.so2.concentration.units
        )
        general_recom.text = info.data.recommendations.general
    }

    private fun setWeatherData(weatherData: WeatherData) {
        weather_icon.setImageResource(Util.getArtForWeatherCondition(weatherData.weather[0].id))
        temp.text =
            resources.getString(R.string.temp, weatherData.main.temp.roundToInt().toString())
        date.text = Util.formatDate(weatherData.time)
        weather_description.text = weatherData.weather[0].desp
    }

    private fun setPredictionData(predictionData: Resource<Result>) {
        predictionData.data?.let {
            predTxt1.text = it.data[0].index.details.aqi.toString()
            predTxt2.text = it.data[1].index.details.aqi.toString()
            predTxt3.text = it.data[2].index.details.aqi.toString()
            predTxt4.text = it.data[3].index.details.aqi.toString()
            predTxt1.setBackgroundColor(Color.parseColor(it.data[0].index.details.color))
            predTxt2.setBackgroundColor(Color.parseColor(it.data[1].index.details.color))
            predTxt3.setBackgroundColor(Color.parseColor(it.data[2].index.details.color))
            predTxt4.setBackgroundColor(Color.parseColor(it.data[3].index.details.color))
            predTimeTxt1.text = output.format(input.parse(it.data[0].time))
            predTimeTxt2.text = output.format(input.parse(it.data[1].time))
            predTimeTxt3.text = output.format(input.parse(it.data[2].time))
            predTimeTxt4.text = output.format(input.parse(it.data[3].time))
        }
    }

    private fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val placeOptions = PlaceOptions.builder()
            .toolbarColor(ContextCompat.getColor(requireContext(), R.color.textColor))
            .backgroundColor(ContextCompat.getColor(requireContext(), R.color.textColor))
            .hint("Enter the location...")
            .country(Locale.getDefault())
            .build()

        val intent = PlaceAutocomplete.IntentBuilder()
            .accessToken(getString(R.string.mapbox_key))
            .placeOptions(placeOptions)
            .build(activity)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun handleNetworkChanges() {
        Util.getNetworkLiveData(requireContext())
            .observe(viewLifecycleOwner, { isConnected ->
                if (!isConnected) {
                    loading.visibility = View.GONE
                    textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                    networkStatusLayout.apply {
                        alpha = 0f
                        visibility = View.VISIBLE
                        setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                        animate()
                            .alpha(1f)
                            .setDuration(Constants.ANIMATION_DURATION)
                            .setListener(null)
                    }
                } else {
                    if (parent_layout.visibility == View.INVISIBLE) {
                        loading.visibility = View.VISIBLE
                        getLocationUpdates()
                    }
                    textViewNetworkStatus.text = getString(R.string.text_connectivity)
                    networkStatusLayout.apply {
                        setBackgroundColor(getColorRes(R.color.colorStatusConnected))

                        animate()
                            .alpha(0f)
                            .setStartDelay(Constants.ANIMATION_DURATION)
                            .setDuration(Constants.ANIMATION_DURATION)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    visibility = View.GONE
                                }
                            })
                    }

                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdates() {
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fastestInterval = 10000
            interval = 10000
            smallestDisplacement = 800f
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun fetchData(latLng: LatLng) {
        aqiViewModel.fetchAirQualityInfo(latLng.latitude, latLng.longitude)
        aqiViewModel.fetchWeather(latLng.latitude, latLng.longitude)
        aqiViewModel.fetchPrediction(latLng.latitude, latLng.longitude)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> onSearchCalled()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            val feature = PlaceAutocomplete.getPlace(data)
            lat = feature.center()?.latitude()!!
            long = feature.center()?.longitude()!!
            fetchData(LatLng(lat, long))
            placeSearched = true
        }
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
        Timber.e("destroyed")
    }

}
