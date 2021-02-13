package com.robin729.aqi.data.model.aqi

import com.google.gson.annotations.SerializedName

data class Index(@SerializedName("ind_cpcb") val details: Details) {
    data class Details (val aqi: Int, val category: String, val color: String)
}