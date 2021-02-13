package com.robin729.aqi.fragment


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.robin729.aqi.R
import com.robin729.aqi.data.model.Resource
import com.robin729.aqi.data.model.mapsAqi.StationData
import com.robin729.aqi.databinding.FragmentMapsBinding
import com.robin729.aqi.utils.PermissionUtils
import com.robin729.aqi.utils.Util
import com.robin729.aqi.utils.gone
import com.robin729.aqi.utils.visible
import com.robin729.aqi.viewmodel.MapsAqiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapboxMap: MapboxMap
    private val mapsAqiViewModel: MapsAqiViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Mapbox.getInstance(context, getString(R.string.mapbox_key))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.getMapAsync(this)
        binding.mapView.onCreate(savedInstanceState)
        mapsAqiViewModel.mapsAqiData.observe(viewLifecycleOwner, { mapsAqiData ->
            when (mapsAqiData.status) {
                Resource.Status.SUCCESS -> {
                    mapsAqiData.data?.data?.let { it -> addMarkers(it) }
                    binding.progressBar.gone()
                }

                Resource.Status.LOADING -> {
                    binding.progressBar.visible()
                }

                Resource.Status.ERROR -> {
                    binding.progressBar.gone()
                    Toast.makeText(requireContext(), "Error in fetching data", Toast.LENGTH_LONG)
                        .show()
                }
            }
        })
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            enableLocationComponent(style)
        }
    }

    private fun addMarkers(list: ArrayList<StationData>) {
        for (data in list) {
            if (data.aqi == "-")
                continue
            mapboxMap.addMarker(
                MarkerOptions()
                    .position(LatLng(data.lat, data.lon))
                    .title(data.station.name)
                    .snippet("AQI: ${data.aqi}")
                    .icon(
                        IconFactory.getInstance(requireContext()).fromBitmap(
                            Util.getIconForAirQualityIndex(
                                requireContext(),
                                data.aqi.toInt()
                            )
                        )
                    )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(style: Style) {
        if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
            mapboxMap.locationComponent.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(requireContext(), style).build()
                )
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }

        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        if (!PermissionUtils.isLocationEnabled(requireContext())) {
            PermissionUtils.showGPSNotEnableDialog(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }


}
