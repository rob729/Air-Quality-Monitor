package com.robin729.aqi.network.retrofit

import com.robin729.aqi.network.WeatherService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeathersApi {
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService: WeatherService by lazy {
        retrofit.create(WeatherService::class.java)
    }
}