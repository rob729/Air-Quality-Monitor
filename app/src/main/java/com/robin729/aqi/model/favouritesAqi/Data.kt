package com.robin729.aqi.model.favouritesAqi

import com.google.gson.annotations.SerializedName
import com.robin729.aqi.model.aqi.Index

data class Data(
    @SerializedName("indexes") val index: Index, var locName: String? = " ")