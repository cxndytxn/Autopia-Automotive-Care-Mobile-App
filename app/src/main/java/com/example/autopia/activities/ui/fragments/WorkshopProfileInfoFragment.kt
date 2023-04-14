package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.autopia.activities.firestore.FirestoreClass
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.autopia.R

class WorkshopProfileInfoFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_profile_info, container, false)
    }

    private fun loadData() {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnCompleteListener { it ->

            val email: TextView? = requireActivity().findViewById(R.id.w_profile_email)
            if (it.result.data?.get("email") == null)
                email?.text = "-"
            else
                email?.text = it.result.data?.get("email").toString()

            val description: TextView? = requireActivity().findViewById(R.id.w_profile_description)
            if (it.result.data?.get("description") == null)
                description?.text = "-"
            else
                description?.text = it.result.data?.get("description").toString()

            val address: TextView? = requireActivity().findViewById(R.id.w_profile_address)
            if (it.result.data?.get("address") == null)
                address?.text = "-"
            else
                address?.text = it.result.data?.get("address").toString()

            val phoneNo: TextView? = requireActivity().findViewById(R.id.w_profile_phone)
            if (it.result.data?.get("contactNumber") == null)
                phoneNo?.text = "-"
            else
                phoneNo?.text = it.result.data?.get("contactNumber").toString()

            val vehicleBrands: TextView? =
                requireActivity().findViewById(R.id.w_profile_vehicle_brands)
            Log.d("huh", it.result.data?.get("vehicleBrands").toString())

            if (it.result.data?.get("vehicleBrands").toString() == "" || it.result.data?.get("vehicleBrands") == null || it.result.data?.get("vehicleBrands").toString() == "[]") {
                "All vehicle brands are accepted".also { vehicleBrands?.text = it }
            }
            else {
                val str: String = it.result.data?.get("vehicleBrands").toString()
                vehicleBrands?.text = str.drop(1).dropLast(1)
            }
            val businessHours: TextView? =
                requireActivity().findViewById(R.id.w_profile_business_hours)
            if (it.result.data?.get("openHours") == null || it.result.data?.get("closeHours") == null)
                businessHours?.text = "-"
            else
                (it.result.data?.get("openHours")
                    .toString() + " - " + it.result.data?.get("closeHours")
                    .toString()).also { businessHours?.text = it }
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.w_location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        loadData()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.w_location_map) as SupportMapFragment
        childFragmentManager.beginTransaction().attach(mapFragment).commit()
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        loadData()
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.w_location_map) as SupportMapFragment
        //childFragmentManager.beginTransaction().detach(mapFragment).commit()
        childFragmentManager.beginTransaction().attach(mapFragment).commit()
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnCompleteListener { it ->
            if (it.result.data?.get("latitude")
                    .toString() != "" && it.result.data?.get("longitude") != "" && it.result.data?.get("latitude") != null && it.result.data?.get("longitude") != null
            ) {
                val lat: Double = it.result.data?.get("latitude").toString().toDouble()
                val lng: Double = it.result.data?.get("longitude").toString().toDouble()

                val location = LatLng(lat, lng)

                map.addMarker(
                    MarkerOptions().position(location)
                        .title(it.result.data?.get("workshopName").toString())
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))

            }
        }
    }
}