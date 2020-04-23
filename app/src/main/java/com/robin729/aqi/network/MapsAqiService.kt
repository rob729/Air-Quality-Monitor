package com.robin729.aqi.network

import com.robin729.aqi.model.mapsAqi.MapsAqiData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface MapsAqiService {
    @GET
    fun getData(@Url url: String): Call<MapsAqiData>
}