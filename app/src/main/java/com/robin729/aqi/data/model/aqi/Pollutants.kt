package com.robin729.aqi.data.model.aqi

import com.google.gson.annotations.SerializedName

class Pollutants(
    @SerializedName("co") val co: PollutantDetails, @SerializedName("no2") val no2: PollutantDetails,
    @SerializedName("o3") val o3: PollutantDetails, @SerializedName("pm10") val pm10: PollutantDetails, @SerializedName(
        "pm25") val pm25: PollutantDetails, @SerializedName("so2") val so2: PollutantDetails
)