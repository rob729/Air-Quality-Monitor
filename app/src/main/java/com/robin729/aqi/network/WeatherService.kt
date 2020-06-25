package com.robin729.aqi.network

import com.robin729.aqi.model.weather.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("weather")
    fun getApi(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("appid") key: String,
        @Query("units") units: String
    ): Call<WeatherData>
}