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
import com.example.autopia.activities.adapter.SelectServiceAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Services
import com.example.autopia.activities.utils.ProgressDialog

class SelectServiceFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    private var servicesList: List<Services> = ArrayList()
    private lateinit var servicesListAdapter: SelectServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        val recyclerView: RecyclerView? =
            parentFragment?.requireActivity()?.findViewById(R.id.view_service_rv)
        val uid = arguments?.getString("workshopId")
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        if (uid != null) {
            viewModel.getServicesByWorkshopId(uid)
        }
        viewModel.services.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                servicesList = response.body()!!
                servicesListAdapter = SelectServiceAdapter(
                    requireActivity(),
                    servicesList
                )
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = servicesListAdapter
                recyclerView?.setHasFixedSize(true)
                servicesListAdapter.serviceListItems = servicesList
                progressDialog.dismissLoading()
            }
        }
    }
}