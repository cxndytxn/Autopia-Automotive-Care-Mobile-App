package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.UpcomingAppointmentsAdapter
import com.example.autopia.activities.adapter.WorkshopUpcomingAppointmentsAdapter
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UpcomingAppointmentsFragment : Fragment() {

    private var appointmentList: List<Appointments> = ArrayList()
    private lateinit var viewModel: ApiViewModel

    private var userAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var userAppointmentListAdapter: UpcomingAppointmentsAdapter
    private var workshopAppointmentList: MutableList<Appointments> = mutableListOf()
    private lateinit var workshopAppointmentListAdapter: WorkshopUpcomingAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upcoming_appointments, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val recyclerView: RecyclerView? =
            parentFragment?.requireActivity()?.findViewById(R.id.upcoming_appointment_rv)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchUserInfo().addOnCompleteListener {
                if (it.isSuccessful) {
                    val userType = it.result.data?.get("userType").toString()
                    if (userType == "user") {
                        val repository = Repository()
                        val viewModelFactory = ApiViewModelFactory(repository)
                        val progressDialog = ProgressDialog(requireActivity())
                        progressDialog.startLoading()
                        viewModel =
                            ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                        viewModel.getAcceptedAppointmentsByClientId(user.uid)
                        if (view != null) {
                            viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                    appointmentList = response.body()!!
                                    userAppointmentListAdapter = UpcomingAppointmentsAdapter(
                                        this.requireActivity(),
                                        userAppointmentList
                                    )
                                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                                    recyclerView?.adapter = userAppointmentListAdapter
                                    recyclerView?.setHasFixedSize(true)
                                    userAppointmentListAdapter.appointmentsListItems =
                                        appointmentList
                                    progressDialog.dismissLoading()
                                } else if (response.isSuccessful && response.body()
                                        .isNullOrEmpty()
                                ) {
                                    val noAppointmentLayout: ConstraintLayout? =
                                        requireActivity().findViewById(R.id.noUpcomingAppointmentLayout)
                                    noAppointmentLayout?.visibility = View.VISIBLE
                                    progressDialog.dismissLoading()
                                } else {
                                    Log.d("error", response.errorBody().toString())
//                                val socketTimeoutLayout =
//                                    requireActivity().findViewById<ConstraintLayout>(R.id.socketTimeoutLayout)
//                                socketTimeoutLayout?.visibility = View.VISIBLE
                                    progressDialog.dismissLoading()
                                }
                            }
                        }
                    } else {
//                        val progressDialog = ProgressDialog(requireActivity())
//                        progressDialog.startLoading()
//                        val apiInterface =
//                            ApiInterface.create().getAcceptedAppointmentsByWorkshopId(user.uid)
//                        apiInterface.enqueue(object : Callback<List<Appointments>> {
//                            override fun onResponse(
//                                call: Call<List<Appointments>>,
//                                response: Response<List<Appointments>>
//                            ) {
//                                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
//                                    appointmentList = response.body()!!
//                                    if (isAdded) {
//                                        workshopAppointmentListAdapter =
//                                            WorkshopUpcomingAppointmentsAdapter(
//                                                requireActivity(),
//                                                workshopAppointmentList
//                                            )
//                                        recyclerView?.layoutManager =
//                                            LinearLayoutManager(view?.context)
//                                        recyclerView?.adapter = workshopAppointmentListAdapter
//                                        recyclerView?.setHasFixedSize(true)
//                                        workshopAppointmentListAdapter.appointmentsListItems =
//                                            appointmentList
//                                    }
//                                    progressDialog.dismissLoading()
//                                } else if (response.isSuccessful && response.body()
//                                        .isNullOrEmpty()
//                                ) {
//                                    val noAppointmentLayout =
//                                        requireActivity().findViewById<ConstraintLayout>(R.id.noUpcomingAppointmentLayout)
//                                    noAppointmentLayout?.visibility = View.VISIBLE
//                                    progressDialog.dismissLoading()
//                                } else {
//                                    val socketTimeoutLayout =
//                                        requireActivity().findViewById<ConstraintLayout>(R.id.socketTimeoutLayout)
//                                    socketTimeoutLayout?.visibility = View.VISIBLE
//                                    progressDialog.dismissLoading()
//                                }
//                            }
//
//                            override fun onFailure(call: Call<List<Appointments>>, t: Throwable) {
//
//                            }
//                        })
                        val repository = Repository()
                        val viewModelFactory = ApiViewModelFactory(repository)
                        val progressDialog = ProgressDialog(requireActivity())
                        progressDialog.startLoading()
                        viewModel =
                            ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                        viewModel.getAcceptedAppointmentsByWorkshopId(user.uid)
                        if (view != null) {
                            viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                    appointmentList = response.body()!!
                                    workshopAppointmentListAdapter =
                                        WorkshopUpcomingAppointmentsAdapter(
                                            requireActivity(),
                                            workshopAppointmentList
                                        )
                                    recyclerView?.layoutManager =
                                        LinearLayoutManager(view?.context)
                                    recyclerView?.adapter = workshopAppointmentListAdapter
                                    recyclerView?.setHasFixedSize(true)
                                    workshopAppointmentListAdapter.appointmentsListItems =
                                        appointmentList
                                    progressDialog.dismissLoading()
                                } else if (response.isSuccessful && response.body()
                                        .isNullOrEmpty()
                                ) {
                                    val noAppointmentLayout: ConstraintLayout? =
                                        requireActivity().findViewById(R.id.noUpcomingAppointmentLayout)
                                    noAppointmentLayout?.visibility = View.VISIBLE
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