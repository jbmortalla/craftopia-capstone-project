package com.capstone.craftopiaproject

import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private lateinit var locationSearch: EditText

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        val cebu = LatLng(10.3157, 123.8854)
        mMap.addMarker(MarkerOptions().position(cebu).title("Marker in Cebu, Philippines"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cebu, 10f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        locationSearch = view.findViewById(R.id.location_search)
        val startNavigationButton: Button = view.findViewById(R.id.start_navigation_button)

        startNavigationButton.setOnClickListener {
            val location = locationSearch.text.toString()
            if (location.isNotEmpty()) {
                searchLocation(location)
            } else {
                Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(requireContext())
        var addressList: List<android.location.Address>? = null
        try {
            addressList = geocoder.getFromLocationName(location, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (addressList.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
            return
        }

        val address = addressList[0]
        val latLng = LatLng(address.latitude, address.longitude)
        mMap.addMarker(MarkerOptions().position(latLng).title(location))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }
}