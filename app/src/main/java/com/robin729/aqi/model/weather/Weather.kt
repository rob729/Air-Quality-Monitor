package com.robin729.aqi.model.weather

import com.google.gson.annotations.SerializedName

class Weather (@SerializedName("main")
               val title: String,
               @SerializedName("description")
               val desp: String,
               @SerializedName("id")
               val id: Int)