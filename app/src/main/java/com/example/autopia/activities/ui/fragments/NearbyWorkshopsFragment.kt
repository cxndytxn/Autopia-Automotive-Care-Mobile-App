package com.example.autopia.activities.ui.fragments

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.whileinuselocation.ForegroundOnlyLocationService
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopsListAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Workshops
import com.example.autopia.activities.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class NearbyWorkshopsFragment : Fragment() {

    private var foregroundOnlyLocationServiceBound = false
    private var nearbyWorkshopList: MutableList<String> = ArrayList()
    private var workshopList: List<Workshops> = ArrayList()
    private lateinit var workshopListAdapter: WorkshopsListAdapter
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        workshopListAdapter = WorkshopsListAdapter(requireContext(), workshopList)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return inflater.inflate(R.layout.fragment_nearby_service_providers, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT)
                .show()
        } else {
            mFusedLocationClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(
                            "location",
                            location.latitude.toString() + location.longitude.toString()
                        )
                        getDocumentNearBy(location.latitude, location.longitude)
                    }

                }.addOnFailureListener {
                    Log.d("Location", it.toString())
                }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        super.onStop()
        if (isAdded) {
            val appBarLayout: AppBarLayout? = requireActivity().findViewById(R.id.app_bar_layout)
            appBarLayout?.elevation = 8f
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        false
                    ) -> {
                        // Precise location access granted.
                        foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    }
                    permissions.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        false
                    ) -> {
                        //Coarse location access granted.
                    }
                    else -> {
                        // No location access granted.
                        foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
                        val locationPermissionDeniedLayout = requireActivity().findViewById<ConstraintLayout>(R.id.permissionDeniedLayout)
                        locationPermissionDeniedLayout?.visibility = View.VISIBLE
                        val permissionSettings: Button? = requireActivity().findViewById(R.id.emptyStatePermissionButton)
                        permissionSettings?.setOnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    }
                }

            }
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            mFusedLocationClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d(
                            "location",
                            location.latitude.toString() + location.longitude.toString()
                        )
                        getDocumentNearBy(location.latitude, location.longitude)
                    }

                }.addOnFailureListener {
                    Log.d("Location", it.toString())
                }
        }

        val serviceIntent = Intent(requireContext(), ForegroundOnlyLocationService::class.java)
        requireActivity().bindService(
            serviceIntent,
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )
        }
    }

    private fun getDocumentNearBy(latitude: Double, longitude: Double) {
        var lat: Double
        var lng: Double

        FirebaseFirestore.getInstance().collection(Constants.Users)
            .whereEqualTo("userType", "workshop").get().addOnSuccessListener { it ->
                for (document in it.documents) {
                    if (document.data?.get("latitude") != null && document.data?.get("longitude") != null) {
                        lat = document.data?.get("latitude").toString().toDouble()
                        lng = document.data?.get("longitude").toString().toDouble()
                        val shop = document.data?.get("workshopName").toString()
                        val distance = distance(latitude, longitude, lat, lng)
                        //Show within 10km
                        if (distance <= 10000) {
                            if (!nearbyWorkshopList.contains(shop)) {
                                nearbyWorkshopList.add(shop)
                            }
                        }
                    }
                }
                if (nearbyWorkshopList.isNotEmpty()) {
                    val noWorkshopFoundLayout: ConstraintLayout? = requireActivity().findViewById(R.id.noWorkshopFoundLayout)
                    noWorkshopFoundLayout?.visibility = View.GONE
                    FirestoreClass().fetchNearbyWorkshopsInfo(nearbyWorkshopList)
                        .addOnCompleteListener { snapshot ->
                            if (snapshot.isSuccessful) {
                                Log.d("workshops", snapshot.result.documents.toString())
                                workshopList =
                                    snapshot.result!!.toObjects(Workshops::class.java)
                                val recyclerView: RecyclerView? =
                                    requireActivity().findViewById(R.id.nearby_sp_rv)
                                recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                                recyclerView?.adapter = workshopListAdapter
                                recyclerView?.setHasFixedSize(true)
                                workshopListAdapter.workshopsListItems = workshopList
                                workshopListAdapter.notifyDataSetChanged()
                            } else {
                                Log.d("", "Error: ${snapshot.exception!!.message}")
                            }

                        }
                } else {
                    val noWorkshopFoundLayout: ConstraintLayout? = requireActivity().findViewById(R.id.noWorkshopFoundLayout)
                    noWorkshopFoundLayout?.visibility = View.VISIBLE
                }
            }
    }

    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // haversine great circle distance approximation, returns meters
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1)) * sin(deg2rad(lat2))
                + (cos(deg2rad(lat1)) * cos(deg2rad(lat2))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 // 60 nautical miles per degree of separation
        dist *= 1852 // 1852 meters per nautical mile
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}