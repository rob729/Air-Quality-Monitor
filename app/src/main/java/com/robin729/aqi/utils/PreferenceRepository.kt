package com.robin729.aqi.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferenceRepository (context: Context) {

    private val type = object : TypeToken<HashSet<LatLng>>(){}.type

    private val dataStore = context.createDataStore(
        name = "app_preference"
    )

    private val apiKeyPref = stringPreferencesKey(name = Constants.API_KEY)
    private val favLatLngList = stringPreferencesKey(name = Constants.FAV_LAT_LNG)
    private val keyFetchTime = longPreferencesKey(name = Constants.LAST_KEY_FETCH_TIME)

    suspend fun setApiKey(apiKey: String) = dataStore.edit {
        it[apiKeyPref] = apiKey
    }

    fun getApiKey() = dataStore.data.map {
        it[apiKeyPref]
    }

    suspend fun setFavLatLngList(list: HashSet<LatLng>) = dataStore.edit { pref ->
        pref[favLatLngList] = Gson().toJson(list)
    }

    fun getFavLatLngList(): Flow<HashSet<LatLng>> = dataStore.data.map {
        Gson().fromJson(it[favLatLngList], type)
    }

    suspend fun setKeyFetchTime(value: Long) = dataStore.edit { pref ->
        pref[keyFetchTime] = value
    }

    fun getKeyFetchTime() = dataStore.data.map {
        it[keyFetchTime]
    }
}