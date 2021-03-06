package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.mapsAqi.MapsAqiData
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsAqiViewModel @Inject constructor(private val aqiRepository: AqiRepository) : ViewModel() {

    private val _mapsAqiData = MutableLiveData<Resource<MapsAqiData>>()

    val mapsAqiData: LiveData<Resource<MapsAqiData>>
        get() = _mapsAqiData

    init {
        fetchData(
            LatLng(
                35.513327, 97.39535869999999
            ), LatLng(6.4626999, 68.1097)
        )
    }

    private fun fetchData(latLngNE: LatLng, latLngSW: LatLng) {
        _mapsAqiData.value = Resource.Loading()
        viewModelScope.launch {
            _mapsAqiData.postValue(aqiRepository.getMapsAqiData(latLngNE, latLngSW))
        }
    }
}