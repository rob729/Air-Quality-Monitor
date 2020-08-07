package com.robin729.aqi

import android.app.Application
import com.robin729.aqi.utils.StoreSession
import com.robin729.aqi.utils.Util

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        StoreSession.init(this)
        Util.initGeocoder(this)
    }
}