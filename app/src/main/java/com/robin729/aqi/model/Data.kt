package com.robin729.aqi.model

import com.google.gson.annotations.SerializedName

class Data(
    @SerializedName("indexes") val index: Index, val pollutants: Pollutants,
    @SerializedName("health_recommendations") val recommendations: Recommendation
)