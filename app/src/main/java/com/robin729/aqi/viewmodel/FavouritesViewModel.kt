package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.model.Resource
import com.robin729.aqi.model.favouritesAqi.Data
import com.robin729.aqi.model.favouritesAqi.Response
import com.robin729.aqi.network.AqiApi
import com.robin729.aqi.network.AqiService
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util
import kotlinx.coroutines.*

class FavouritesViewModel : ViewModel() {

    private val _favouritesData = MutableLiveData<Resource<ArrayList<Data>>>()

    val favouritesData: LiveData<Resource<ArrayList<Data>>>
        get() = _favouritesData

    private val favouritesLatLngList: HashSet<LatLng> by lazy {
        StoreSession.readFavouritesLatLng(Constants.FAVOURITES_LIST)
    }

    private val aqiService: AqiService by lazy {
        AqiApi().initalizeRetrofit()
    }

    init {
        fetchFavouritesListData()
    }

    private fun fetchFavouritesListData() {
        _favouritesData.value = Resource.Loading()
        //val apiKey = Firebase.remoteConfig[Constants.REMOTE_CONFIG_API_KEY].asString()
        val apiKey = StoreSession.readString(Constants.API_KEY)
        val calls: ArrayList<Deferred<Response>> = ArrayList()
        val favouritesData: ArrayList<Data> = ArrayList()
        val locationNames: ArrayList<String> = ArrayList()
        try {
            CoroutineScope(Dispatchers.IO).launch {
                favouritesLatLngList.forEach {
                    calls.add(async {
                        aqiService.getAqiData(
                            it.latitude,
                            it.longitude,
                            apiKey,
                            Constants.FAVOURITES_FEATURES
                        )
                    })
                    locationNames.add(Util.getLocationString(it))
                }

                calls.forEach {
                    favouritesData.add(it.await().data)
                    it.await().data.locName = locationNames[calls.indexOf(it)]
                }

                _favouritesData.postValue(Resource.Success(favouritesData))
            }
        } catch (e: Exception) {
            _favouritesData.postValue(Resource.Error("Something Went Wrong", null))
        }
    }
}