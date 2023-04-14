package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.*
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HistoryFragment : Fragment() {

    private var appointmentList: List<Appointments> = ArrayList()
    private lateinit var viewModel: ApiViewModel

    private var userAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var userAppointmentListAdapter: HistoriesAdapter
    private var workshopAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var workshopAppointmentListAdapter: WorkshopHistoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val view: View
        return if (user != null) {
            view = inflater.inflate(R.layout.fragment_history, container, false)
            view
        } else {
            view = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = view.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
            view
        }
    }

    override fun onStart() {
        super.onStart()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val recyclerView: RecyclerView? = requireActivity().findViewById(R.id.history_rv)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchUserInfo().addOnCompleteListener {
                if (it.isSuccessful) {
                    val userType = it.result.data?.get("userType").toString()
                    if (userType == "user") {
                        if (isAdded) {
                            val bottomNavigationView: BottomNavigationView? =
                                requireActivity().findViewById(R.id.bottom_navigation_view)
                            bottomNavigationView?.isVisible = true
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                            if (view != null) {
                                val repository = Repository()
                                val viewModelFactory = ApiViewModelFactory(repository)
                                val progressDialog = ProgressDialog(requireActivity())
                                progressDialog.startLoading()
                                if (isAdded) {
                                    viewModel =
                                        ViewModelProvider(
                                            this,
                                            viewModelFactory
                                        )[ApiViewModel::class.java]
                                    viewModel.getHistoriesByClientId(user.uid)
                                    viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                        //pass list here
                                        if (response.isSuccessful && !response.body()
                                                .isNullOrEmpty()
                                        ) {
                                            appointmentList = response.body()!!
                                            userAppointmentListAdapter =
                                                HistoriesAdapter(
                                                    this.requireActivity(),
                                                    userAppointmentList
                                                )
                                            recyclerView?.layoutManager =
                                                LinearLayoutManager(view?.context)
                                            recyclerView?.adapter = userAppointmentListAdapter
                                            recyclerView?.setHasFixedSize(true)
                                            userAppointmentListAdapter.historyListItems =
                                                appointmentList
                                            progressDialog.dismissLoading()
                                        } else if (response.isSuccessful && response.body()
                                                .isNullOrEmpty()
                                        ) {
                                            val noHistoryLayout: ConstraintLayout? =
                                                requireActivity().findViewById(R.id.noHistoryLayout)
                                            noHistoryLayout?.visibility = View.VISIBLE
                                            progressDialog.dismissLoading()
                                        } else {
                                            val socketTimeoutLayout: ConstraintLayout? =
                                                requireActivity().findViewById(R.id.socketTimeoutLayout2)
                                            socketTimeoutLayout?.visibility = View.VISIBLE
                                            progressDialog.dismissLoading()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.workshop_app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                        }
                        val repository = Repository()
                        val viewModelFactory = ApiViewModelFactory(repository)
                        val progressDialog = ProgressDialog(requireActivity())
                        progressDialog.startLoading()
                        if (isAdded) {
                            if (view != null) {
                                viewModel =
                                    ViewModelProvider(
                                        this,
                                        viewModelFactory
                                    )[ApiViewModel::class.java]
                                viewModel.getHistoriesByWorkshopId(user.uid)
                                viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                        appointmentList = response.body()!!
                                        workshopAppointmentListAdapter =
                                            WorkshopHistoriesAdapter(
                                                this.requireActivity(),
                                                workshopAppointmentList
                                            )
                                        recyclerView?.layoutManager =
                                            LinearLayoutManager(view?.context)
                                        recyclerView?.adapter = workshopAppointmentListAdapter
                                        recyclerView?.setHasFixedSize(true)
                                        workshopAppointmentListAdapter.historyListItems =
                                            appointmentList
                                        progressDialog.dismissLoading()
                                    } else {
                                        val noHistoryLayout: ConstraintLayout? =
                                            requireActivity().findViewById(R.id.noHistoryLayout)
                                        noHistoryLayout?.visibility = View.VISIBLE
                                        progressDialog.dismissLoading()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}