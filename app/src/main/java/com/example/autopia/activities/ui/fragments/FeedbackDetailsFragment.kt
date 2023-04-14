package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_feedback_details.*

class FeedbackDetailsFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback_details, container, false)
    }

    override fun onStart() {
        super.onStart()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val feedbackId = arguments?.getInt("feedback_id")
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        if (feedbackId != null) {
            viewModel.getFeedback(feedbackId)
            viewModel.feedback.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val image: ImageView? = requireActivity().findViewById(R.id.client_image)
                    val name: TextView? =
                        requireActivity().findViewById(R.id.workshop_feedback_client)
                    val date: TextView? =
                        requireActivity().findViewById(R.id.workshop_feedback_date_time)
                    val vehicle: TextView? =
                        requireActivity().findViewById(R.id.workshop_feedback_vehicle)
                    val service: TextView? =
                        requireActivity().findViewById(R.id.workshop_feedback_service)
                    val rating: RatingBar? = requireActivity().findViewById(R.id.workshop_rating_bar)
                    val timeliness: ToggleButton? =
                        requireActivity().findViewById(R.id.workshop_timeliness)
                    val politeness: ToggleButton? =
                        requireActivity().findViewById(R.id.workshop_politeness)
                    val speed: ToggleButton? = requireActivity().findViewById(R.id.workshop_speed)
                    val payment: ToggleButton? =
                        requireActivity().findViewById(R.id.workshop_payment)
                    val serviceToggle: ToggleButton? =
                        requireActivity().findViewById(R.id.workshop_service_toggle)
                    val explanation: ToggleButton? =
                        requireActivity().findViewById(R.id.workshop_explanation)
                    val comments: EditText? = requireActivity().findViewById(R.id.workshop_comments)
                    val body = response.body()!!
                    rating?.setIsIndicator(true)
                    FirestoreClass().fetchUserByID(body.clientId)
                        .addOnSuccessListener { documentSnapshot ->
                            if (image != null) {
                                Glide.with(requireContext()).load(documentSnapshot.data?.get("imageLink"))
                                    .into(image)
                            }
                            name?.text = documentSnapshot.data?.get("username").toString()
                        }
                    viewModel.getAppointmentById(body.appointmentId)
                    viewModel.appointment.observe(viewLifecycleOwner) { appointmentResponse ->
                        if (appointmentResponse.isSuccessful) {
                            val appointmentBody = appointmentResponse.body()!!
                            date?.text = appointmentBody.date
                            vehicle?.text = appointmentBody.vehicle
                            service?.text = appointmentBody.services
                        }
                    }
                    rating?.rating = body.rating.toFloat()
                    when (body.rating.toFloat()) {
                        1f -> {
                            workshop_rating_desc.text =
                                requireActivity().resources.getString(R.string.worst)
                        }
                        2f -> {
                            workshop_rating_desc.text =
                                requireActivity().resources.getString(R.string.bad)
                        }
                        3f -> {
                            workshop_rating_desc.text =
                                requireActivity().resources.getString(R.string.neutral)
                        }
                        4f -> {
                            workshop_rating_desc.text =
                                requireActivity().resources.getString(R.string.good)
                        }
                        5f -> {
                            workshop_rating_desc.text =
                                requireActivity().resources.getString(R.string.best)
                        }
                    }
                    if (body.timeliness == "true") {
                        timeliness?.isChecked = true
                        timeliness?.setTextColor(resources.getColor(R.color.white))
                    }
                    if (body.politeness == "true") {
                        politeness?.isChecked = true
                        politeness?.setTextColor(resources.getColor(R.color.white))
                    }
                    if (body.speed == "true") {
                        speed?.isChecked = true
                        speed?.setTextColor(resources.getColor(R.color.white))
                    }
                    if (body.service == "true") {
                        serviceToggle?.isChecked = true
                        serviceToggle?.setTextColor(resources.getColor(R.color.white))
                    }
                    if (body.explanation == "true") {
                        explanation?.isChecked = true
                        explanation?.setTextColor(resources.getColor(R.color.white))
                    }
                    if (body.payment == "true") {
                        payment?.isChecked = true
                        payment?.setTextColor(resources.getColor(R.color.white))
                    }
                    comments?.setText(body.comment)
                    progressDialog.dismissLoading()
                }
            }
        }
    }
}