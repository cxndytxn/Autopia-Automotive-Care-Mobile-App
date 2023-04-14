package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.SelectVehicleAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Vehicles
import com.example.autopia.activities.utils.ProgressDialog

class SelectVehicleFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    private var vehiclesList: List<Vehicles> = ArrayList()
    private lateinit var vehiclesListAdapter: SelectVehicleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_vehicles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        val recyclerView: RecyclerView? =
            parentFragment?.requireActivity()?.findViewById(R.id.view_vehicle_rv)
        val uid = arguments?.getString("userId")
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        if (uid != null) {
            viewModel.getVehiclesByClientId(uid)
        }
        viewModel.vehicles.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                vehiclesList = response.body()!!
                vehiclesListAdapter = SelectVehicleAdapter(
                    requireActivity(),
                    vehiclesList
                )
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = vehiclesListAdapter
                recyclerView?.setHasFixedSize(true)
                vehiclesListAdapter.vehiclesListItems = vehiclesList
                progressDialog.dismissLoading()
            }
        }
    }
}