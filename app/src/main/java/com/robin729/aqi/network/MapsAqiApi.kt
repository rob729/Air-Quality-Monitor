package com.robin729.aqi.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsAqiApi {

    private val BASE_URL = "https://api.waqi.info/map/bounds/"

    fun initalizeRetrofit(): MapsAqiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        return retrofit.create(MapsAqiService::class.java)
    }
}