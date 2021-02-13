package com.robin729.aqi.data.model.favouritesAqi

import com.google.gson.annotations.SerializedName
import com.mapbox.mapboxsdk.geometry.LatLng
import com.robin729.aqi.data.model.aqi.Index

data class Data(
    @SerializedName("indexes") val index: Index, @SerializedName("datetime") val time: String, var locName: String? = " ", var latLng: LatLng)