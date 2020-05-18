package com.robin729.aqi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AqiApi {

    private val BASE_URL = "https://api.breezometer.com/air-quality/v2/"

    fun initalizeRetrofit(): AqiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(AqiService::class.java)
    }
}