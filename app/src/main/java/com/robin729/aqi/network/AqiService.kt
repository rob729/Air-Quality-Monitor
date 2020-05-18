package com.robin729.aqi.network

import com.robin729.aqi.model.aqi.Info
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface AqiService {

    @GET
    fun getApi(@Url url: String): Call<Info>
}