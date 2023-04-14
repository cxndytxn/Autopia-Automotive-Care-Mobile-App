package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopFeedbacksAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Feedbacks
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class WorkshopFeedbacksFragment : Fragment() {

    private var feedbackList: List<Feedbacks> = ArrayList()
    private lateinit var feedbackListAdapter: WorkshopFeedbacksAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = false

        return inflater.inflate(R.layout.fragment_workshop_feedback, container, false)
    }

    override fun onPause() {
        super.onPause()
        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        if (uid != null) {
            viewModel.getFeedbacksByWorkshopId(uid)
        }
        viewModel.feedbacks.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                feedbackList = response.body()!!
                feedbackListAdapter = WorkshopFeedbacksAdapter(requireActivity(), feedbackList)
                val recyclerView: RecyclerView? = requireView().findViewById(R.id.feedback_rv)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = feedbackListAdapter
                recyclerView?.setHasFixedSize(true)
                feedbackListAdapter.feedbackListItems = feedbackList
                progressDialog.dismissLoading()
            } else {
                val noNotificationLayout: ConstraintLayout? =
                    requireActivity().findViewById(R.id.noFeedbackLayout)
                noNotificationLayout?.visibility = View.VISIBLE
                progressDialog.dismissLoading()
            }
        }
    }
}