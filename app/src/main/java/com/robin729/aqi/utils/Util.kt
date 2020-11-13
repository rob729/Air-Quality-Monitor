package com.robin729.aqi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.R
import com.robin729.aqi.data.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

object Util {

    lateinit var geocoder: Geocoder

    fun initGeocoder(context: Context){
        if(!this::geocoder.isInitialized){
            geocoder = Geocoder(context, Locale.getDefault())
        }
    }

    fun hasNetwork(ctx: Context?): Boolean {
        val connectivityManager =
            ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private val networkLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    fun Fragment.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(context!!, id)

    fun getNetworkLiveData(context: Context): LiveData<Boolean> {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network?) {
                networkLiveData.postValue(true)
            }

            override fun onLost(network: Network?) {
                networkLiveData.postValue(false)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        }

        return networkLiveData
    }

    fun getArtForWeatherCondition(weatherId: Int): Int {

        if (weatherId in 200..232) {
            return R.drawable.art_storm
        } else if (weatherId in 300..321) {
            return R.drawable.art_light_rain
        } else if (weatherId in 500..504) {
            return R.drawable.art_rain
        } else if (weatherId == 511) {
            return R.drawable.art_snow
        } else if (weatherId in 520..531) {
            return R.drawable.art_rain
        } else if (weatherId in 600..622) {
            return R.drawable.art_snow
        } else if (weatherId in 701..761) {
            return R.drawable.art_fog
        } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
            return R.drawable.art_storm
        } else if (weatherId == 800) {
            return R.drawable.art_clear
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds
        } else if (weatherId in 802..804) {
            return R.drawable.art_clouds
        } else if (weatherId in 900..906) {
            return R.drawable.art_storm
        } else if (weatherId in 958..962) {
            return R.drawable.art_storm
        } else if (weatherId in 951..957) {
            return R.drawable.art_clear
        }

        return R.drawable.art_storm
    }

    fun formatDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds * 1000L
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(calendar.time)
    }

    fun getIconForAirQualityIndex(context: Context, aqi: Int): Bitmap? {
        val iconDrawable: Int = when (aqi) {
            in 0..50 -> {
                R.drawable.good_map_marker
            }
            in 51..100 -> {
                R.drawable.satisfactory_map_marker
            }
            in 101..200 -> {
                R.drawable.moderate_map_marker
            }
            in 201..300 -> {
                R.drawable.poor_map_marker
            }
            in 301..400 -> {
                R.drawable.very_poor_map_marker
            }
            in 401..500 -> {
                R.drawable.severe_map_marker
            }
            else -> {
                R.drawable.severe_map_marker
            }
        }
        return BitmapFactory.decodeResource(context.resources, iconDrawable)
    }

    fun getFaceBasedOnAqi(aqi: Int): Int {
        return when (aqi) {
            in 0..50 -> {
                R.drawable.ic_face_green
            }
            in 51..100 -> {
                R.drawable.ic_face_green
            }
            in 101..200 -> {
                R.drawable.ic_face_yellow
            }
            in 201..300 -> {
                R.drawable.ic_face_orange
            }
            in 301..400 -> {
                R.drawable.ic_face_maroon
            }
            in 401..500 -> {
                R.drawable.ic_face_purple
            }
            else -> { R.drawable.ic_face_purple }
        }
    }

    fun getAQICategory(aqi: Int): String{
        return when (aqi) {
            in 0..50 -> {
                "Good"
            }
            in 51..100 -> {
                "Satisfactory"
            }
            in 101..200 -> {
                "Moderate"
            }
            in 201..300 -> {
                "Poor"
            }
            in 301..400 -> {
                "Very poor"
            }
            in 401..500 -> {
                "Severe"
            }
            else -> { "Severe" }
        }
    }

    fun getBackgroundColourBasedOnAqi(aqi: Int): Int{
        return when (aqi) {
            in 0..50 -> {
                R.color.ic_dark_green
            }
            in 51..100 -> {
                R.color.ic_green
            }
            in 101..200 -> {
                R.color.ic_yellow
            }
            in 201..300 -> {
                R.color.ic_orange
            }
            in 301..400 -> {
                R.color.ic_maroon
            }
            in 401..500 -> {
                R.color.ic_purple
            }
            else -> { R.color.ic_purple }
        }
    }

    suspend fun getLocationString(latLng: LatLng): String = withContext(Dispatchers.IO){
        return@withContext try {
            val location = geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )[0]
            if((location.subLocality.isNullOrBlank())){
                location.subAdminArea + ", " + location.adminArea
            } else {
                location.subLocality.substring(0, min(location.subLocality.length, 20)) + ", " + location.subAdminArea
            }
        } catch (exception: Exception) {
           "  "
        }
    }

}