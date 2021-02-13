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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
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
import com.robin729.aqi.databinding.FragmentMainBinding
import com.robin729.aqi.utils.*
import com.robin729.aqi.utils.Constants.AUTOCOMPLETE_REQUEST_CODE
import com.robin729.aqi.utils.Util.getColorRes
import com.robin729.aqi.viewmodel.AqiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainFragment : Fragment() {


    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val aqiViewModel: AqiViewModel by viewModels()

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        //FusedLocationProviderClient(requireContext())
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val favouritesLatLngList: HashSet<LatLng> = hashSetOf()

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

    private var lat: Double = 0.00
    private var long: Double = 0.00

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
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).actionBar?.setDisplayShowTitleEnabled(false)

        ParseInstallation.getCurrentInstallation().saveInBackground()

        binding.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtNo2.text =
                    Html.fromHtml("NO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
                txtSo2.text =
                    Html.fromHtml("SO<sub><small>2</small></sub>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else {
                txtNo2.text = Html.fromHtml("NO<sub><small>2</small></sub>")
                txtSo2.text = Html.fromHtml("SO<sub><small>2</small></sub>")
            }

        }

        handleNetworkChanges()
        if (arguments?.containsKey(Constants.FAV_LAT_LNG) == true) {
            val favLatLng = arguments?.get(Constants.FAV_LAT_LNG) as LatLng
            fetchData(favLatLng)
            placeSearched = true
        }

        aqiViewModel.location.observe(viewLifecycleOwner, {
            binding.location.text = resources.getString(R.string.location, it)
        })

        aqiViewModel.aqi.observe(viewLifecycleOwner, {
            binding.apply {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        setAQIData(it.data!!)
                        parentLayout.visible()
                        loading.gone()
                        error.gone()
                        favButton.isLiked = favouritesLatLngList.contains(LatLng(lat, long))

                    }

                    Resource.Status.LOADING -> {
                        error.gone()
                        parentLayout.gone()
                        loading.visible()
                    }

                    Resource.Status.ERROR -> {
                        parentLayout.gone()
                        loading.gone()
                        error.visible()
                    }
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

        binding.favButton.setOnLikeListener(object : OnLikeListener {
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

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            favouritesLatLngList.apply {
                clear()
                addAll(preferenceRepository.getFavLatLngList().first())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(NonCancellable) {
                preferenceRepository.setFavLatLngList(favouritesLatLngList)
            }
        }
    }

    private fun setAQIData(info: Info) {
        binding.apply {
            aqi.text = info.data.index.details.aqi.toString()
            category.text = info.data.index.details.category
            cardView.setCardBackgroundColor(Color.parseColor(info.data.index.details.color))
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
            generalRecom.text = info.data.recommendations.general
        }

    }

    private fun setWeatherData(weatherData: WeatherData) {
        binding.apply {
            weatherIcon.setImageResource(Util.getArtForWeatherCondition(weatherData.weather[0].id))
            temp.text =
                resources.getString(R.string.temp, weatherData.main.temp.roundToInt().toString())
            date.text = Util.formatDate(weatherData.time)
            weatherDescription.text = weatherData.weather[0].desp
        }
    }

    private fun setPredictionData(predictionData: Resource<Result>) {
        predictionData.data?.let {
            binding.apply {
                predTxt1.text = it.data[0].index.details.aqi.toString()
                predTxt2.text = it.data[3].index.details.aqi.toString()
                predTxt3.text = it.data[7].index.details.aqi.toString()
                predTxt4.text = it.data[11].index.details.aqi.toString()
                predTxt1.setBackgroundColor(Color.parseColor(it.data[0].index.details.color))
                predTxt2.setBackgroundColor(Color.parseColor(it.data[3].index.details.color))
                predTxt3.setBackgroundColor(Color.parseColor(it.data[7].index.details.color))
                predTxt4.setBackgroundColor(Color.parseColor(it.data[11].index.details.color))
                predTimeTxt1.text = output.format(input.parse(it.data[0].time))
                predTimeTxt2.text = output.format(input.parse(it.data[3].time))
                predTimeTxt3.text = output.format(input.parse(it.data[7].time))
                predTimeTxt4.text = output.format(input.parse(it.data[11].time))
            }
        }
    }

    private fun onSearchCalled() {
        // Set the fields to specify which types of place data to return.
        val placeOptions = PlaceOptions.builder()
            .toolbarColor(ContextCompat.getColor(requireContext(), R.color.bgColor))
            .backgroundColor(ContextCompat.getColor(requireContext(), R.color.bgColor))
            .statusbarColor(ContextCompat.getColor(requireContext(), R.color.bgColor))
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
                binding.apply {
                    if (!isConnected) {
                        loading.gone()
                        textViewNetworkStatus.text = getString(R.string.text_no_connectivity)
                        networkStatusLayout.apply {
                            alpha = 0f
                            visible()
                            setBackgroundColor(getColorRes(R.color.colorStatusNotConnected))
                            animate()
                                .alpha(1f)
                                .setDuration(Constants.ANIMATION_DURATION)
                                .setListener(null)
                        }
                    } else {
                        if (parentLayout.visibility == View.INVISIBLE) {
                            loading.visible()
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
                                        gone()
                                    }
                                })
                        }

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
        aqiViewModel.apply {
            fetchAirQualityInfo(latLng.latitude, latLng.longitude)
            fetchWeather(latLng.latitude, latLng.longitude)
            fetchPrediction(latLng.latitude, latLng.longitude)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
        Timber.e("destroyed")
    }
}
