package com.robin729.aqi.network

import com.robin729.aqi.data.model.mapsAqi.MapsAqiData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface MapsAqiService {

    @GET
    suspend fun getMapsData(@Url url: String): Response<MapsAqiData>
}