package com.example.autopia.activities.ui.fragments

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
import com.example.autopia.activities.adapter.RescheduleAppointmentsAdapter
import com.example.autopia.activities.adapter.WorkshopRescheduleAppointmentsAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.google.firebase.auth.FirebaseAuth

class ReschedulingAppointmentsFragment : Fragment() {

    private var appointmentList: List<Appointments> = ArrayList()
    private lateinit var viewModel: ApiViewModel

    private var userAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var userAppointmentListAdapter: RescheduleAppointmentsAdapter
    private var workshopAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var workshopAppointmentListAdapter: WorkshopRescheduleAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rescheduling_appointments, container, false)
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
        val recyclerView: RecyclerView? =
            parentFragment?.requireActivity()?.findViewById(R.id.reschedule_appointment_rv)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchUserInfo().addOnCompleteListener {
                if (it.isSuccessful) {
                    val userType = it.result.data?.get("userType").toString()
                    if (userType == "user") {
                        val repository = Repository()
                        val viewModelFactory = ApiViewModelFactory(repository)
                        if (isAdded) {
                            if (view != null) {
                                viewModel =
                                    ViewModelProvider(
                                        this,
                                        viewModelFactory
                                    )[ApiViewModel::class.java]
                                viewModel.getRescheduleAppointmentsByClientId(user.uid)
                                viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                    //pass list here
                                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                        appointmentList = response.body()!!
                                        userAppointmentListAdapter = RescheduleAppointmentsAdapter(
                                            requireActivity(),
                                            userAppointmentList
                                        )
                                        recyclerView?.layoutManager =
                                            LinearLayoutManager(view?.context)
                                        recyclerView?.adapter = userAppointmentListAdapter
                                        recyclerView?.setHasFixedSize(true)
                                        userAppointmentListAdapter.appointmentsListItems =
                                            appointmentList
                                    } else {
                                        val noAppointmentLayout: ConstraintLayout? =
                                            requireActivity().findViewById(R.id.noRescheduleAppointmentLayout)
                                        noAppointmentLayout?.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    } else {
                        if (view != null) {
                            val repository = Repository()
                            val viewModelFactory = ApiViewModelFactory(repository)
                            if (isAdded) {
                                viewModel =
                                    ViewModelProvider(
                                        this,
                                        viewModelFactory
                                    )[ApiViewModel::class.java]
                                viewModel.getRescheduleAppointmentsByWorkshopId(user.uid)
                                viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                    //pass list here
                                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                        appointmentList = response.body()!!
                                        workshopAppointmentListAdapter =
                                            WorkshopRescheduleAppointmentsAdapter(
                                                requireActivity(),
                                                workshopAppointmentList
                                            )
                                        recyclerView?.layoutManager =
                                            LinearLayoutManager(view?.context)
                                        recyclerView?.adapter = workshopAppointmentListAdapter
                                        recyclerView?.setHasFixedSize(true)
                                        workshopAppointmentListAdapter.appointmentsListItems =
                                            appointmentList
                                    } else {
                                        val noAppointmentLayout: ConstraintLayout? =
                                            requireActivity().findViewById(R.id.noRescheduleAppointmentLayout)
                                        noAppointmentLayout?.visibility = View.VISIBLE
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