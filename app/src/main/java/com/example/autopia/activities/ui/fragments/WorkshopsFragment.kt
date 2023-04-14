package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopsListAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Workshops

class WorkshopsFragment : Fragment() {

    private var workshopList: List<Workshops> = ArrayList()
    private lateinit var workshopListAdapter: WorkshopsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        workshopListAdapter = WorkshopsListAdapter(requireContext(), workshopList)

        return inflater.inflate(R.layout.fragment_all_service_providers, container, false)
    }

    override fun onStart() {
        super.onStart()
        loadAllData()
    }

    override fun onResume() {
        super.onResume()
        loadAllData()
    }

    private fun loadAllData() {
        FirestoreClass().fetchAllWorkshopsInfo().addOnCompleteListener {
            if (it.isSuccessful) {
                val recyclerView: RecyclerView? = parentFragment?.requireActivity()?.findViewById(R.id.All_SP_RV)
                recyclerView?.adapter = workshopListAdapter
                workshopList = it.result!!.toObjects(Workshops::class.java)
                workshopListAdapter.workshopsListItems = workshopList
            } else {
                Log.d("", "Error: ${it.exception!!.message}")
            }
        }
    }
}