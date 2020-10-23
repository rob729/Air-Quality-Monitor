package com.robin729.aqi.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.robin729.aqi.R
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.utils.PermissionUtils
import com.robin729.aqi.utils.Util
import com.robin729.aqi.viewmodel.MapsAqiViewModel
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
        Mapbox.getInstance(requireContext(), getString(R.string.mapbox_key))
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync {

            mapsAqiViewModel.mapsAqiData.observe(viewLifecycleOwner, Observer { mapsAqiData ->
                when (mapsAqiData.status) {
                    Resource.Status.SUCCESS -> {
                        for (data in mapsAqiData.data?.data!!) {
                            if (data.aqi == "-")
                                continue
                            it.addMarker(
                                MarkerOptions()
                                    .position(LatLng(data.lat, data.lon))
                                    .title(data.station.name)
                                    .snippet("AQI: ${data.aqi}")
                                    .icon(
                                        IconFactory.getInstance(requireContext()).fromBitmap(
                                            Util.getIconForAirQualityIndex(
                                                requireContext(),
                                                data.aqi.toInt()
                                            )!!
                                        )
                                    )
                            )
                        }
                        progressBar.visibility = View.GONE
                    }

                    Resource.Status.LOADING -> {
                        progressBar.visibility = View.VISIBLE
                    }

                    Resource.Status.ERROR -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "Error in fetching data",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            })

            it.setStyle(
                Style.Builder()
                    .fromUri("mapbox://styles/mapbox/streets-v9")
            ) { style -> enableLocationComponent(it, style) }


        }

    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(mapboxMap: MapboxMap, style: Style) {
        if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
            val locationComponent: LocationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(requireContext(), style).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        if (!PermissionUtils.isLocationEnabled(requireContext())) {
            PermissionUtils.showGPSNotEnableDialog(requireContext())
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
