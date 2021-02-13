package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Result
import com.robin729.aqi.data.model.weather.WeatherData
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import com.robin729.aqi.utils.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AqiViewModel @Inject constructor(private val aqiRepository: AqiRepository) : ViewModel() {

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

    fun fetchAirQualityInfo(lat: Double, long: Double) {
        _aqi.value = Resource.Loading()
        viewModelScope.launch {
            _aqi.postValue(aqiRepository.getAirQualityInfo(lat, long))
            _location.postValue(Util.getLocationString(LatLng(lat, long)))
        }
    }

    fun fetchWeather(lat: Double, long: Double) {
        _weather.value = Resource.Loading()
        viewModelScope.launch {
            _weather.postValue(aqiRepository.getWeather(lat, long))
        }
    }

    fun fetchPrediction(lat: Double, long: Double){
        viewModelScope.launch {
            _predictionData.postValue(aqiRepository.getAqiPrediction(lat,long))
        }
    }
}