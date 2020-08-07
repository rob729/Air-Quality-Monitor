package com.robin729.aqi.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng

object StoreSession {
    lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        if (!this::sharedPreferences.isInitialized) {
            sharedPreferences =
                context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun write(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun write(key: String, value: HashSet<LatLng>) {
        val editor = sharedPreferences.edit()
        val info = Gson().toJson(value)
        editor.putString(key, info)
        editor.apply()
    }


    fun read(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun write(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun readString(key: String): String {
        return sharedPreferences.getString(key, " ").toString()
    }

    fun readFavouritesLatLng(key: String): HashSet<LatLng> {
        val type = object : TypeToken<HashSet<LatLng>>(){}.type
        return Gson().fromJson(sharedPreferences.getString(key, " ").toString(),type) ?: hashSetOf()
    }

}