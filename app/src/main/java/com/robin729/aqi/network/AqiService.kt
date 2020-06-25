package com.robin729.aqi.network

import com.robin729.aqi.model.aqi.Info
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AqiService {

    @GET("current-conditions")
    fun getApi(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("key") apiKey: String,
        @Query("features") features: String
    ): Call<Info>
}