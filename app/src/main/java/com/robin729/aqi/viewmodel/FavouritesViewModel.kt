package com.robin729.aqi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.favouritesAqi.Data
import com.robin729.aqi.data.repository.AqiRepository
import com.robin729.aqi.data.repository.AqiRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(private val aqiRepository: AqiRepository) : ViewModel() {

    private val _favouritesData = MutableLiveData<Resource<ArrayList<Data>>>()

    val favouritesData: LiveData<Resource<ArrayList<Data>>>
        get() = _favouritesData

    init {
        fetchFavouritesListData()
    }

    private fun fetchFavouritesListData() {
        _favouritesData.value = Resource.Loading()
        viewModelScope.launch {
            _favouritesData.postValue(aqiRepository.getFavouritesListData())
        }
    }
}