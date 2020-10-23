package com.robin729.aqi.activity

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.robin729.aqi.R
import com.robin729.aqi.utils.Constants
import com.robin729.aqi.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && resources.configuration.uiMode.and(
                Configuration.UI_MODE_NIGHT_MASK
            ) == Configuration.UI_MODE_NIGHT_NO
        ) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setupViews()
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
                        Timber.e("granted permission")
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
