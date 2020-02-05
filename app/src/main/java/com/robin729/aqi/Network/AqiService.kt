package com.robin729.aqi.Network

import com.robin729.aqi.model.Info
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface AqiService {

    @GET
    fun getApi(@Url url: String): Call<Info>
}