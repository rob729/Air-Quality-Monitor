package com.robin729.aqi.model.aqi

import com.google.gson.annotations.SerializedName
import com.robin729.aqi.model.aqi.Index
import com.robin729.aqi.model.aqi.Pollutants
import com.robin729.aqi.model.aqi.Recommendation

class Data(
    @SerializedName("indexes") val index: Index, val pollutants: Pollutants,
    @SerializedName("health_recommendations") val recommendations: Recommendation
)