package com.robin729.aqi.network.retrofit

import com.robin729.aqi.network.MapsAqiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MapsAqiApi {

    private val BASE_URL = "https://api.waqi.info/map/bounds/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService: MapsAqiService by lazy {
        retrofit.create(MapsAqiService::class.java)
    }
}