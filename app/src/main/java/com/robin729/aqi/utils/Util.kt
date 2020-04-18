package com.robin729.aqi.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.robin729.aqi.R
import java.text.SimpleDateFormat
import java.util.*

object Util {

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

    fun formatDate(milliseconds: Long) : String {
        val sdf = SimpleDateFormat("EEE, d MMM yyyy", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds*1000L
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(calendar.time)
    }
}