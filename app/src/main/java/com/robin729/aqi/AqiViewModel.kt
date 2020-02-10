package com.robin729.aqi

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robin729.aqi.model.Info
import com.robin729.aqi.network.AqiApi
import com.robin729.aqi.util.Util
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
                .getApi("current-conditions?lat=$lat&lon=$long&key=${Util.apiKey}&features=${Util.features}")

            withContext(Dispatchers.IO) {

                try {

                    request.enqueue(object : Callback<Info> {
                        override fun onResponse(call: Call<Info>, response: Response<Info>) {
                            _aqiLoadError.value = false
                            _aqi.value = response.body()
                            _loading.value = false
                            _location.value =  geocoder.getFromLocation(lat!!, long!!, 1)[0].subLocality + ", " + geocoder.getFromLocation(
                                lat, long, 1)[0].subAdminArea

                            Log.e("TAG", response.message())
                        }

                        override fun onFailure(call: Call<Info>, t: Throwable) {
                            _aqiLoadError.value = true
                            _loading.value = false
                            Log.e("TAG", t.message)
                        }
                    })
                } catch (e: Exception) {
                    Log.e(
                        "MainActicity",
                        "Exception ${e.message}"
                    )
                }
            }
        }

    }
}