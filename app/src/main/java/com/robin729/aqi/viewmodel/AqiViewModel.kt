package com.robin729.aqi.viewmodel

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robin729.aqi.model.Info
import com.robin729.aqi.model.weather.WeatherData
import com.robin729.aqi.network.AqiApi
import com.robin729.aqi.network.WeathersApi
import com.robin729.aqi.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AqiViewModel : ViewModel() {

    private val _aqi = MutableLiveData<Info>()

    val aqi: LiveData<Info>
        get() = _aqi

    private val _weather = MutableLiveData<WeatherData>()

    val weather: LiveData<WeatherData>
        get() = _weather

    private val _location = MutableLiveData<String>()

    val location: LiveData<String>
        get() = _location

    private val _loading = MutableLiveData<Boolean>()

    val loading: LiveData<Boolean>
        get() = _loading

    private val _aqiLoadError = MutableLiveData<Boolean>()

    val aqiLoadError: LiveData<Boolean>
        get() = _aqiLoadError

    fun fetchRepos(lat: Double?, long: Double?, geocoder: Geocoder) {
        _loading.value = true

        CoroutineScope(Dispatchers.IO).launch {
            val request = AqiApi().initalizeRetrofit()
                .getApi("current-conditions?lat=$lat&lon=$long&key=${Constants.API_KEY}&features=${Constants.FEATURES}")

            withContext(Dispatchers.IO) {
                try {
                    request.enqueue(object : Callback<Info> {
                        override fun onResponse(call: Call<Info>, response: Response<Info>) {
                            _aqiLoadError.value = false
                            _aqi.value = response.body()
                            _loading.value = false
                            _location.value = geocoder.getFromLocation(
                                lat!!,
                                long!!,
                                1
                            )[0].subLocality + ", " + geocoder.getFromLocation(
                                lat, long, 1
                            )[0].subAdminArea

                            Log.e("TAG", response.message() + "ViewModel")
                        }

                        override fun onFailure(call: Call<Info>, t: Throwable) {
                            _aqiLoadError.value = true
                            _loading.value = false
                            Log.e("TAG", t.message)
                        }
                    })
                } catch (e: Exception) {
                    Log.e("MainActicity", "Exception ${e.message}")
                }
            }
        }

    }

    fun fetchWeather(lat: Double?, long: Double?) {
        _loading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val request = WeathersApi().initalizeRetrofit()
                .getApi("weather?lat=$lat&lon=$long&appid=${Constants.WEATHER_KEY}&units=metric")
            withContext(Dispatchers.IO) {
                try {
                    request.enqueue(object : Callback<WeatherData> {
                        override fun onResponse(
                            call: Call<WeatherData>,
                            response: Response<WeatherData>
                        ) {
                            _weather.value = response.body()
                            _loading.value = false
                        }

                        override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                            _loading.value = false
                            Log.e("TAG", t.message)
                        }
                    })
                } catch (e: Exception) {
                    Log.e("MainActicity", "Exception ${e.message}")
                }
            }
        }
    }
}