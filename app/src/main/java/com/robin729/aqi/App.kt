package com.robin729.aqi

import android.app.Application
import com.parse.Parse
import com.parse.ParseObject
import com.parse.ParseQuery
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util
import timber.log.Timber
import java.util.concurrent.TimeUnit

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        StoreSession.init(this)
        Util.initGeocoder(this)
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )

        Timber.e("${StoreSession.readString(Constants.API_KEY)} api_key")
        updateAQIKey()
    }

    private fun updateAQIKey() {
        val timeDiff =
            TimeUnit.MILLISECONDS.toHours(
                System.currentTimeMillis() - StoreSession.getTime(
                    Constants.LAST_KEY_FETCH_TIME
                )
            )
        if (timeDiff > 5) {
            val parseQuery = ParseQuery.getQuery<ParseObject>("AQI_KEY")
            parseQuery.getInBackground(
                "HoVICsIfuo"
            ) { p, e ->
                if (e == null) {
                    StoreSession.write(Constants.API_KEY, p["key"].toString())
                } else {
                    Timber.e("error")
                }
            }
        }

    }
}