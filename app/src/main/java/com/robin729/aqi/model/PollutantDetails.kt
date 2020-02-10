package com.robin729.aqi.model

import com.google.gson.annotations.SerializedName

class PollutantDetails (@SerializedName("display_name") val name: String, val concentration: Concentration)