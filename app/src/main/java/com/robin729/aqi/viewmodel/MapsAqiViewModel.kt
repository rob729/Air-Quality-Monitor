package com.robin729.aqi.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.model.Resource
import com.robin729.aqi.model.mapsAqi.MapsAqiData
import com.robin729.aqi.network.MapsAqiApi
import com.robin729.aqi.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsAqiViewModel: ViewModel() {

    private val _mapsAqiData = MutableLiveData<Resource<MapsAqiData>>()

    val mapsAqiData: LiveData<Resource<MapsAqiData>>
        get() = _mapsAqiData

    private val _aqiLoadError = MutableLiveData<Boolean>()

    val aqiLoadError: LiveData<Boolean>
        get() = _aqiLoadError

    private val _loading = MutableLiveData<Boolean>()

    val loading: LiveData<Boolean>
        get() = _loading

    fun fetchData(latLngNE: LatLng, latLngSW: LatLng){
        _mapsAqiData.value = Resource.Loading()

        viewModelScope.launch {
            val request = MapsAqiApi().initalizeRetrofit()
                .getData("?token=${Constants.MAPS_AQI_KEY}&latlng=${latLngNE.latitude},${latLngNE.longitude},${latLngSW.latitude},${latLngSW.longitude}")

            withContext(Dispatchers.IO) {
                try{
                    request.enqueue(object : Callback<MapsAqiData>{
                        override fun onFailure(call: Call<MapsAqiData>, t: Throwable) {
                            _mapsAqiData.value = Resource.Error("Something went wrong ${t.message}", null)
                        }

                        override fun onResponse(
                            call: Call<MapsAqiData>,
                            response: Response<MapsAqiData>
                        ) {
                            if(response.isSuccessful){
                                _mapsAqiData.value = Resource.Success(response.body()!!)
                            } else {
                                _mapsAqiData.value = Resource.Error("Something went wrong ${response.message()}", null)
                            }
                        }
                    })

                }catch (e: Exception) {
                    Log.e(
                        "MainActicity",
                        "Exception ${e.message}"
                    )
                }
            }
        }

    }
}