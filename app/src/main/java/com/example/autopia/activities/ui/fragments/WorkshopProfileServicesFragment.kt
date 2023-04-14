package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopServicesAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Services

class WorkshopProfileServicesFragment : Fragment() {

    private var serviceList: List<Services> = ArrayList()
    private lateinit var serviceListAdapter: WorkshopServicesAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_profile_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        serviceListAdapter = WorkshopServicesAdapter(this.requireActivity(), serviceList)
        loadData(workshopId!!)
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(uid: String) {
        Log.d("debug2", uid)
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getServicesByWorkshopId(uid)
            viewModel.services.observe(viewLifecycleOwner) { response ->
                //pass list here
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    serviceList = response.body()!!
                    serviceListAdapter =
                        WorkshopServicesAdapter(this.requireActivity(), serviceList)
                    val recyclerView: RecyclerView? =
                        requireView().findViewById(R.id.workshop_services_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = serviceListAdapter
                    recyclerView?.setHasFixedSize(true)
                    serviceListAdapter.serviceListItems = serviceList
                    serviceListAdapter.notifyDataSetChanged()
                } else {
                    val layout: ConstraintLayout? =
                        requireActivity().findViewById(R.id.noServicesLayout)
                    layout?.visibility = View.VISIBLE
                }
            }
        }
    }
}