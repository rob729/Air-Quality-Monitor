package com.robin729.aqi.network

import com.robin729.aqi.data.model.aqi.Info
import com.robin729.aqi.data.model.favouritesAqi.Response
import com.robin729.aqi.data.model.favouritesAqi.Result
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AqiService {

    @GET("current-conditions")
    suspend fun getAqiData(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("key") apiKey: String,
        @Query("features") features: String
    ): Response

    @GET("current-conditions")
    suspend fun getAqiDataResponse(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("key") apiKey: String,
        @Query("features") features: String
    ): retrofit2.Response<Info>

    @GET("forecast/hourly")
    suspend fun getAqiForecastData(
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("key") apiKey: String,
        @Query("hours") hours: Int,
        @Query("features") features: String
    ): retrofit2.Response<Result>
}