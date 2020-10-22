package com.robin729.aqi.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.model.Resource
import com.robin729.aqi.model.aqi.Info
import com.robin729.aqi.model.weather.WeatherData
import com.robin729.aqi.network.AqiApi
import com.robin729.aqi.network.WeathersApi
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class AqiViewModel : ViewModel() {

    private val _aqi = MutableLiveData<Resource<Info>>()

    val aqi: LiveData<Resource<Info>>
        get() = _aqi

    private val _weather = MutableLiveData<Resource<WeatherData>>()

    val weather: LiveData<Resource<WeatherData>>
        get() = _weather

    private val _location = MutableLiveData<String>()

    val location: LiveData<String>
        get() = _location

    fun fetchRepos(lat: Double, long: Double) {
        _aqi.value = Resource.Loading()

        CoroutineScope(Dispatchers.IO).launch {
            val apiKey = StoreSession.readString(Constants.API_KEY)
            try {
                withTimeout(10000) {
                    Log.e("Main", "aqi api $apiKey")
                    val request = AqiApi().initalizeRetrofit()
                        .getApi(lat, long, apiKey, Constants.FEATURES)
                    val location = Util.getLocationString(LatLng(lat, long))

                    request.enqueue(object : Callback<Info> {
                        override fun onResponse(call: Call<Info>, response: Response<Info>) {

                            if (response.isSuccessful) {
                                _aqi.value = Resource.Success(response.body()!!)
                                _location.value = location
                                Timber.e("%sViewModel", response.message())
                            } else {
                                Log.e("Main", "response not successful ${response.code()}")
                                _aqi.value = Resource.Error(
                                    "Something went wrong ${response.message()}",
                                    null
                                )
                            }
                        }

                        override fun onFailure(call: Call<Info>, t: Throwable) {
                            Log.e("Main", "failure response")
                            _aqi.value = Resource.Error("Something went wrong ${t.message}", null)
                        }
                    })

                }
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    _aqi.value = Resource.Error("Something went wrong ${e.message}", null)
                }
            }
        }

    }

    fun fetchWeather(lat: Double, long: Double) {
        _weather.value = Resource.Loading()
        CoroutineScope(Dispatchers.IO).launch {
            val request = WeathersApi().initalizeRetrofit()
                .getApi(lat, long, Constants.WEATHER_KEY, "metric")
            withContext(Dispatchers.IO) {
                try {
                    request.enqueue(object : Callback<WeatherData> {
                        override fun onResponse(
                            call: Call<WeatherData>,
                            response: Response<WeatherData>
                        ) {
                            if (response.isSuccessful) {
                                _weather.value = Resource.Success(response.body()!!)
                            } else {
                                Log.e("Main", "onFailure: ${response.errorBody()}")
                            }
                        }

                        override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                            _weather.value =
                                Resource.Error("Something went wrong ${t.message}", null)
                            Log.e("Main", "onFailure: ${t.message}")
                        }
                    })
                } catch (e: Exception) {
                    Log.e("MainActicity", "Exception ${e.message}")
                }
            }
        }
    }
}