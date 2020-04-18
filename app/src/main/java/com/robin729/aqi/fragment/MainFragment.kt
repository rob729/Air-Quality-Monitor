package com.robin729.aqi.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ferfalk.simplesearchview.SimpleSearchView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.robin729.aqi.AqiViewModel
import com.robin729.aqi.R
import com.robin729.aqi.utils.PermissionUtils
import com.robin729.aqi.utils.Util
import com.robin729.aqi.utils.Util.getColorRes
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*
import kotlin.math.roundToInt


class MainFragment : Fragment() {

    private val ANIMATION_DURATION = 1000.toLong()

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
                val newLat = p0?.locations?.get(0)?.latitude!!
                val newLong = p0.locations[0]?.longitude!!
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

        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).actionBar?.setDisplayShowTitleEnabled(false)

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
            temp.text = resources.getString(R.string.temp, it.main.temp.roundToInt().toString())
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

        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (Util.hasNetwork(context) && Geocoder.isPresent()) {
                    val gc = Geocoder(context)
                    val addresses: List<Address> = gc.getFromLocationName(query, 4)
                    for (a in addresses) {
                        if (a.hasLatitude() && a.hasLongitude()) {
                            aqiViewModel.fetchRepos(a.latitude, a.longitude, geocoder)
                            aqiViewModel.fetchWeather(a.latitude, a.longitude)
                            break
                        }
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                return false
            }

        })
    }

    override fun onStart() {
        super.onStart()
        if (PermissionUtils.isLocationEnabled(context!!)) {
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                fastestInterval = 300000
                interval = 600000
                smallestDisplacement = 800f
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        } else {
            PermissionUtils.showGPSNotEnableDialog(context!!)
        }
    }

    override fun onResume() {
        super.onResume()
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener { v, keyCode, event ->
            Log.e("TAG", "${searchView.isSearchOpen}  onBackpres")

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                if (searchView.isSearchOpen) {
                    searchView.closeSearch()
                    true
                } else {
                    false
                }
                // handle back button
            } else false
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.action_search))
    }

}
