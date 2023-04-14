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
import com.example.autopia.activities.adapter.WorkshopFeedbacksAdapter
import com.example.autopia.activities.adapter.WorkshopServicesAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Feedbacks
import com.example.autopia.activities.model.Services

class WorkshopProfileFeedbacksFragment : Fragment() {

    private var feedbackList: List<Feedbacks> = ArrayList()
    private lateinit var feedbackListAdapter: WorkshopFeedbacksAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_profile_feedbacks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        feedbackListAdapter = WorkshopFeedbacksAdapter(requireActivity(), feedbackList)
        loadData(workshopId!!)
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(uid: String) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getFeedbacksByWorkshopId(uid)
            viewModel.feedbacks.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    feedbackList = response.body()!!
                    feedbackListAdapter = WorkshopFeedbacksAdapter(requireActivity(), feedbackList)
                    val recyclerView: RecyclerView? =
                        requireView().findViewById(R.id.workshop_feedbacks_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = feedbackListAdapter
                    recyclerView?.setHasFixedSize(true)
                    feedbackListAdapter.feedbackListItems = feedbackList
                    feedbackListAdapter.notifyDataSetChanged()
                } else {
                    val layout: ConstraintLayout? =
                        requireActivity().findViewById(R.id.noFeedbacksLayout)
                    layout?.visibility = View.VISIBLE
                }
            }
        }
    }
}