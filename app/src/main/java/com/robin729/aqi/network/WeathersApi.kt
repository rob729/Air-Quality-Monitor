package com.robin729.aqi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeathersApi {
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    fun initalizeRetrofit(): WeatherService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(WeatherService::class.java)
    }
}