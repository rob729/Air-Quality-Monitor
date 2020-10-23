package com.robin729.aqi.data.repository

import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.mapsAqi.MapsAqiData
import com.robin729.aqi.data.model.weather.WeatherData

interface AqiRepository {

    suspend fun getAirQualityInfo(lat: Double, long: Double): Resource<Info>
    suspend fun getWeather(lat: Double, long: Double): Resource<WeatherData>
    suspend fun getFavouritesListData(): Resource<ArrayList<com.robin729.aqi.data.model.favouritesAqi.Data>>
    suspend fun getMapsAqiData(latLngNE: LatLng, latLngSW: LatLng): Resource<MapsAqiData>
}