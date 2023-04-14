package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.VehiclesAdapter
import com.example.autopia.activities.api.*
import com.example.autopia.activities.model.Vehicles
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class VehiclesFragment : Fragment() {

    private var vehicleList: List<Vehicles> = ArrayList()
    private lateinit var vehicleListAdapter: VehiclesAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val root: View
        if (user != null) {
            root = inflater.inflate(R.layout.fragment_vehicles, container, false)
        } else {
            root = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = root.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
        }

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        return root
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            loadData()
        }
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onStop() {
        super.onStop()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            loadData()

            requireActivity().findViewById<FloatingActionButton>(R.id.add_vehicle_button)
                .setOnClickListener {
                    val navController = requireActivity().findNavController(R.id.nav_host_fragment)
                    navController.navigate(R.id.addVehicleFragment)
                }
        } else {
            val floatingBtn: FloatingActionButton? =
                requireActivity().findViewById(R.id.add_vehicle_button)
            floatingBtn?.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            if (uid != null) {
                viewModel.getVehiclesByClientId(uid)
            }
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel.vehicles.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    vehicleList = response.body()!!
                    vehicleListAdapter = VehiclesAdapter(this.requireActivity(), vehicleList)
                    val recyclerView: RecyclerView? = requireView().findViewById(R.id.vehicle_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = vehicleListAdapter
                    recyclerView?.setHasFixedSize(true)
                    vehicleListAdapter.vehiclesListItems = vehicleList
                    vehicleListAdapter.notifyDataSetChanged()
                    progressDialog.dismissLoading()
                } else {
                    Log.d("Error", response.message())
                }
            }
        }
    }
}