package com.robin729.aqi.network

import com.robin729.aqi.model.weather.WeatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface WeatherService {

    @GET
    fun getApi(@Url url: String): Call<WeatherData>
}