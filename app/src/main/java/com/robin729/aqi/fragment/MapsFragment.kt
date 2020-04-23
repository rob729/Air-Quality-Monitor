package com.robin729.aqi.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toAdaptiveIcon
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.robin729.aqi.R
import com.robin729.aqi.utils.PermissionUtils
import com.robin729.aqi.utils.Util
import com.robin729.aqi.viewmodel.MapsAqiViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_maps.*


class MapsFragment : Fragment() {

    private val mapsAqiViewModel: MapsAqiViewModel by lazy {
        ViewModelProvider(this).get(MapsAqiViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Mapbox.getInstance(this.context!!, getString(R.string.mapbox_key))
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapsAqiViewModel.loading.observe(viewLifecycleOwner, Observer { loading->
            progressBar.visibility = if(loading) View.VISIBLE else View.GONE
        })

        mapsAqiViewModel.fetchData(LatLng(35.513327,97.39535869999999
        ), LatLng(6.4626999,68.1097))
        mapView.getMapAsync {

            mapsAqiViewModel.mapsAqiData.observe(viewLifecycleOwner, Observer {mapsAqiData->
                Log.e("TAG", "success observing")
                for(data in mapsAqiData.data){
                    if(data.aqi == "-")
                        continue
                    it.addMarker(MarkerOptions()
                        .position(LatLng(data.lat, data.lon))
                        .title(data.station.name)
                        .snippet("AQI: ${data.aqi}")
                        .icon(IconFactory.getInstance(context!!).fromBitmap(Util.getIconForAirQualityIndex(context!!, data.aqi.toInt())!!)))
                }
            })

            it.setStyle(
                Style.Builder()
                    .fromUri("mapbox://styles/mapbox/streets-v9")
            ) { style -> enableLocationComponent(it, style) }


        }

    }
    
    private fun enableLocationComponent(mapboxMap: MapboxMap, style: Style) {
        if(PermissionUtils.isAccessFineLocationGranted(context!!)){
            val locationComponent: LocationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context!!, style).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if(!PermissionUtils.isLocationEnabled(context!!)){
            PermissionUtils.showGPSNotEnableDialog(context!!)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}
