package com.robin729.aqi.network.retrofit

import com.robin729.aqi.network.AqiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AqiApi {
    private val BASE_URL = "https://api.breezometer.com/air-quality/v2/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService: AqiService by lazy {
        retrofit.create(AqiService::class.java)
    }
}