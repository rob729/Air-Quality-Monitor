package com.robin729.aqi.utils

object Constants {
    const val ANIMATION_DURATION = 1000L
    const val REQUEST_LOCATION_PERMISSION = 1
    const val AUTOCOMPLETE_REQUEST_CODE = 2
    const val API_KEY = "api_key"
    const val FEATURES =
        "local_aqi,health_recommendations,pollutants_concentrations"
    const val FAVOURITES_FEATURES =
        "local_aqi"
    const val PREF_NAME = "aqi_pref"
    const val FAVOURITES_LIST = "favourites_list"
    const val LAST_KEY_FETCH_TIME = "last_key_fetch_time"
    const val FAV_LAT_LNG = "fav_lat_lng"
    const val AQI_BASE_URL = "https://api.breezometer.com/air-quality/v2/"
    const val MAPS_BASE_URL = "https://api.waqi.info/map/bounds/"
    const val WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
}