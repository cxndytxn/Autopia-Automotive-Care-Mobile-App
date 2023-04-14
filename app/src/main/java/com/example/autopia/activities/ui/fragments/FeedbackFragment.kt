package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Feedbacks
import com.example.autopia.activities.utils.Constants
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_feedback.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedbackFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onStart() {
        super.onStart()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appointmentId = arguments?.getInt("appointment_id")
        val workshopId = arguments?.getString("workshop_id")
        val workshopName = arguments?.getString("workshop_name")
        val vehicle = arguments?.getString("vehicle")
        val date = arguments?.getString("date")
        val time = arguments?.getString("time")
        val service = arguments?.getString("service")

        feedback_workshop?.text = workshopName
        feedback_service?.text = service
        feedback_vehicle?.text = vehicle
        "$date $time".also { feedback_date_time?.text = it }
        FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                if (workshop_image != null) {
                    Glide.with(requireContext()).load(snapshot.data?.get("imageLink"))
                        .into(workshop_image)
                }
            }
        }

        ratingBar?.setOnRatingBarChangeListener { ratingBar, fl, b ->
            when (fl) {
                1f -> {
                    rating_desc?.text = requireActivity().resources.getString(R.string.worst)
                }
                2f -> {
                    rating_desc?.text = requireActivity().resources.getString(R.string.bad)
                }
                3f -> {
                    rating_desc?.text = requireActivity().resources.getString(R.string.neutral)
                }
                4f -> {
                    rating_desc?.text = requireActivity().resources.getString(R.string.good)
                }
                5f -> {
                    rating_desc?.text = requireActivity().resources.getString(R.string.best)
                }
            }
        }

        timeliness?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                timeliness?.setTextColor(resources.getColor(R.color.white))
            else
                timeliness?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        politeness?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                politeness?.setTextColor(resources.getColor(R.color.white))
            else
                politeness?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        speed?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                speed?.setTextColor(resources.getColor(R.color.white))
            else
                speed?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        payment?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                payment?.setTextColor(resources.getColor(R.color.white))
            else
                payment?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        service_toggle?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                service_toggle?.setTextColor(resources.getColor(R.color.white))
            else
                service_toggle?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        explanation?.setOnCheckedChangeListener { compoundButton, b ->
            if (b)
                explanation?.setTextColor(resources.getColor(R.color.white))
            else
                explanation?.setTextColor(resources.getColor(R.color.indigo_300))
        }

        submitButton?.setOnClickListener {
            val isValid = validateInputFields()
            if (isValid) {
                val feedback = Feedbacks(
                    null,
                    ratingBar.rating.toDouble(),
                    comments.text.toString(),
                    if (timeliness.isChecked) "true" else "false",
                    if (politeness.isChecked) "true" else "false",
                    if (speed.isChecked) "true" else "false",
                    if (payment.isChecked) "true" else "false",
                    if (explanation.isChecked) "true" else "false",
                    if (service_toggle.isChecked) "true" else "false",
                    appointmentId!!,
                    workshopId,
                    FirebaseAuth.getInstance().currentUser?.uid!!
                )

                FirebaseFirestore.getInstance().collection(Constants.Feedbacks)
                    .whereEqualTo("workshopId", workshopId).get().addOnCompleteListener { task ->
                        if (task.isSuccessful && !task.result.isEmpty) {
                            var avgRating = 0.0
                            for (doc in task.result.documents) {
                                avgRating += (doc.data?.get("total").toString()
                                    .toFloat() + ratingBar.rating) / (doc.data?.get("count")
                                    .toString()
                                    .toInt() + 1)
                                val feedbacks = hashMapOf(
                                    "workshopId" to workshopId,
                                    "rating" to avgRating,
                                    "total" to doc.data?.get("total").toString()
                                        .toDouble() + ratingBar.rating.toDouble(),
                                    "count" to doc.data?.get("count").toString().toInt() + 1
                                )
                                FirebaseFirestore.getInstance().collection(Constants.Feedbacks)
                                    .document(doc.id).set(feedbacks)
                            }
                        } else {
                            val feedbacks = hashMapOf(
                                "workshopId" to workshopId,
                                "rating" to ratingBar.rating.toDouble(),
                                "total" to ratingBar.rating.toDouble(),
                                "count" to 1
                            )
                            FirebaseFirestore.getInstance().collection(Constants.Feedbacks)
                                .add(feedbacks)
                        }
                    }

                val apiInterface = ApiInterface.create().postFeedbacks(feedback)
                apiInterface.enqueue(object : Callback<Feedbacks> {
                    override fun onResponse(
                        call: Call<Feedbacks>,
                        response: Response<Feedbacks>,
                    ) {
                        Log.d("error", response.errorBody().toString())
                        if (response.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Feedback was sent successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            OneSignalNotificationService().createAppointmentNotification(
                                workshopId,
                                "Your client had provided feedback!",
                                "${response.body()?.comment}"
                            )
                            val navController =
                                requireActivity().findNavController(R.id.nav_host_fragment)
                            navController.popBackStack()
                        }
                    }

                    override fun onFailure(call: Call<Feedbacks>, t: Throwable) {
                        Toast.makeText(
                            context,
                            "Error. Feedback could not be added. " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateInputFields(): Boolean {
        val comments: String = comments.text.toString().trim { it <= ' ' }
        val ratings: Float = ratingBar.rating

        return !(comments.isEmpty() || ratings == 0.0f)
    }
}