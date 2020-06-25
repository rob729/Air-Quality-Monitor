package com.robin729.aqi.activity

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.robin729.aqi.R
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig
    }
    private val configSettings: FirebaseRemoteConfigSettings by lazy {
        remoteConfigSettings {
            minimumFetchIntervalInSeconds = 86400
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        setupViews()
        updateAPIKey()
    }

    override fun onStart() {
        super.onStart()
        locationCheck()
    }

    private fun setupViews() {
        val navController = Navigation.findNavController(this, R.id.myNavHostFragment)
        bottomNavView.setupWithNavController(navController)
    }

    private fun locationCheck() {
        if (!PermissionUtils.isAccessFineLocationGranted(this)) {
            PermissionUtils.requestAccessFindLocationPermission(
                this,
                Constants.REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun updateAPIKey(){
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.

        when (requestCode) {
            Constants.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (PermissionUtils.isAccessFineLocationGranted(this)) {
                        Log.e("TAG", "granted permission")
                    } else {
                        PermissionUtils.requestAccessFindLocationPermission(
                            this,
                            Constants.REQUEST_LOCATION_PERMISSION
                        )
                    }
                } else {
                    PermissionUtils.showPermissionLocationNotEnableDialog(
                        this,
                        Constants.REQUEST_LOCATION_PERMISSION
                    )
                }
            }
        }
    }

}
