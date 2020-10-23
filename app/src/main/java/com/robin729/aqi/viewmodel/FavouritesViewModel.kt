package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.favouritesAqi.Data
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesViewModel : ViewModel() {

    private val _favouritesData = MutableLiveData<Resource<ArrayList<Data>>>()

    val favouritesData: LiveData<Resource<ArrayList<Data>>>
        get() = _favouritesData

    private val aqiRepository: AqiRepository by lazy {
        AqiRepositoryImpl()
    }

    init {
        fetchFavouritesListData()
    }

    private fun fetchFavouritesListData() {
        _favouritesData.value = Resource.Loading()
        CoroutineScope(Dispatchers.IO).launch {
            _favouritesData.postValue(aqiRepository.getFavouritesListData())
        }
    }
}