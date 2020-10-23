package com.robin729.aqi.data.model.weather

import com.google.gson.annotations.SerializedName

class WeatherData(@SerializedName("main")
                  val main: Main,
                  @SerializedName("weather")
                  val weather: ArrayList<Weather>,
                  @SerializedName("dt")
                  val time: Long)