package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Result
import com.robin729.aqi.data.model.weather.WeatherData
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import com.robin729.aqi.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AqiViewModel : ViewModel() {

    private val _aqi = MutableLiveData<Resource<Info>>()

    val aqi: LiveData<Resource<Info>>
        get() = _aqi

    private val _predictionData = MutableLiveData<Resource<Result>>()

    val predictionData: LiveData<Resource<Result>>
        get() = _predictionData

    private val _weather = MutableLiveData<Resource<WeatherData>>()

    val weather: LiveData<Resource<WeatherData>>
        get() = _weather

    private val _location = MutableLiveData<String>()

    val location: LiveData<String>
        get() = _location

    private val aqiRepository: AqiRepository by lazy {
        AqiRepositoryImpl()
    }

    fun fetchAirQualityInfo(lat: Double, long: Double) {
        _aqi.value = Resource.Loading()
        CoroutineScope(Dispatchers.IO).launch {
            _aqi.postValue(aqiRepository.getAirQualityInfo(lat, long))
            _location.postValue(Util.getLocationString(LatLng(lat, long)))
        }
    }

    fun fetchWeather(lat: Double, long: Double) {
        _weather.value = Resource.Loading()
        CoroutineScope(Dispatchers.IO).launch {
            _weather.postValue(aqiRepository.getWeather(lat, long))
        }
    }

    fun fetchPrediction(lat: Double, long: Double){
        CoroutineScope(Dispatchers.IO).launch {
            _predictionData.postValue(aqiRepository.getAqiPrediction(lat,long))
        }
    }
}