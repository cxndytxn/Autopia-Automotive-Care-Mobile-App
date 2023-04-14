package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.model.BookingStatistics
import com.example.autopia.activities.model.Customers
import com.example.autopia.activities.utils.Constants
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProgressDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class BookingReviewFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val pref: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = pref?.getString("workshop_id", "")

        if (workshopId != null) {
            FirestoreClass().fetchWorkshopInfo(workshopId).addOnCompleteListener {
                val workshopName: TextView? =
                    requireActivity().findViewById(R.id.booking_review_sp_name)
                workshopName?.text = it.result.data?.get("workshopName").toString()

                val description: TextView? =
                    requireActivity().findViewById(R.id.booking_review_sp_details)
                description?.text = it.result.data?.get("description").toString()
            }
        }

        val service: TextView? =
            requireActivity().findViewById(R.id.booking_review_service_request_text)
        val vehicle: TextView? = requireActivity().findViewById(R.id.booking_review_vehicle_text)
        val name: TextView? = requireActivity().findViewById(R.id.booking_review_your_name_text)
        val phone: TextView? = requireActivity().findViewById(R.id.booking_review_your_phone_text)
        val description: TextView? =
            requireActivity().findViewById(R.id.booking_review_description_text)
        val date: TextView? =
            requireActivity().findViewById(R.id.booking_review_appointment_date_text)
        val time: TextView? =
            requireActivity().findViewById(R.id.booking_review_appointment_time_text)
        val quotation: TextView? =
            requireActivity().findViewById(R.id.booking_review_quoted_price_text)

        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(
                "appointment_details",
                Context.MODE_PRIVATE
            )
        val gson = Gson()
        val json: String? = sharedPreferences.getString("appointment_details", "")
        val appointmentDetails: Appointments = gson.fromJson(json, Appointments::class.java)

        service?.text = appointmentDetails.services
        vehicle?.text = appointmentDetails.vehicle
        name?.text = appointmentDetails.clientName
        phone?.text = appointmentDetails.phoneNo
        if (appointmentDetails.description == "") {
            description?.text = "-"
        } else {
            description?.text = appointmentDetails.description
        }
        date?.text = appointmentDetails.date
        time?.text = appointmentDetails.startTime
        quotation?.text = appointmentDetails.quotedPrice.toString()

        val bookingButton: Button? = requireActivity().findViewById(R.id.booking_review_button)
        bookingButton?.setOnClickListener {
            postAppointment()

        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun postAppointment() {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("appointment_id", Context.MODE_PRIVATE)
        val appointmentId: Int? = preferences?.getInt("appointment_id", 0)

        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(
                "appointment_details",
                Context.MODE_PRIVATE
            )
        val gson = Gson()
        val json: String? = sharedPreferences.getString("appointment_details", "")
        val appointmentDetails: Appointments = gson.fromJson(json, Appointments::class.java)

        val repository = Repository()
        var isTimeOverlapping: Boolean
        var status = "pending"
        val endTime: LocalTime = if (appointmentDetails.endTime != "") {
            LocalTime.parse(
                appointmentDetails.endTime,
                DateTimeFormatter.ofPattern("hh:mm a")
            )
        } else {
            LocalTime.parse(
                appointmentDetails.startTime,
                DateTimeFormatter.ofPattern("hh:mm a")
            ).plusHours(1)
        }
        if (view != null) {
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel =
                ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getAcceptedAppointmentsByWorkshopId(appointmentDetails.workshopId)
            viewModel.appointments.observe(viewLifecycleOwner) { response ->
                try {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val appointments = response.body()!!
                        appointments.forEach { appointment ->
                            if (appointment.date == appointmentDetails.date) {
                                isTimeOverlapping = isTimeOverlapping(
                                    LocalTime.parse(
                                        appointmentDetails.startTime,
                                        DateTimeFormatter.ofPattern("hh:mm a")
                                    ),
                                    endTime,
                                    LocalTime.parse(
                                        appointment.startTime,
                                        DateTimeFormatter.ofPattern("hh:mm a")
                                    ),
                                    LocalTime.parse(
                                        appointment.endTime,
                                        DateTimeFormatter.ofPattern("hh:mm a")
                                    )
                                )
                                status = if (isTimeOverlapping) {
                                    "clashed"
                                } else {
                                    "pending"
                                }
                            }
                        }
                        if (appointmentId != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                            val cal = Calendar.getInstance()
                            val now = sdf.format(cal.time)
                            val appointment = Appointments(
                                appointmentDetails.id,
                                appointmentDetails.services,
                                appointmentDetails.serviceId,
                                appointmentDetails.date,
                                appointmentDetails.startTime,
                                appointmentDetails.duration,
                                appointmentDetails.endTime,
                                appointmentDetails.color,
                                appointmentDetails.vehicle,
                                appointmentDetails.vehicleId,
                                appointmentDetails.phoneNo,
                                appointmentDetails.workshopPhoneNo,
                                appointmentDetails.description,
                                appointmentDetails.workshopId,
                                appointmentDetails.workshopName,
                                appointmentDetails.clientId,
                                appointmentDetails.clientName,
                                status,
                                appointmentDetails.attachment,
                                appointmentDetails.remarks,
                                appointmentDetails.quotedPrice,
                                now
                            )

                            val apiInterface = ApiInterface.create().postAppointments(appointment)
                            apiInterface.enqueue(object : Callback<Appointments> {
                                override fun onResponse(
                                    call: Call<Appointments>,
                                    response: Response<Appointments>,
                                ) {
                                    if (response.isSuccessful) {
                                        setCustomer(appointment.clientId, appointment.workshopId)
                                        Toast.makeText(
                                            requireContext(),
                                            "Appointment requested. Please wait for reply from the selected workshop.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        OneSignalNotificationService().createAppointmentNotification(
                                            appointmentDetails.workshopId,
                                            "${appointmentDetails.clientName} had requested for an appointment",
                                            "Date: ${appointmentDetails.date}\nStart Time: ${appointmentDetails.startTime}"
                                        )
                                        progressDialog.dismissLoading()
                                    }
                                }

                                override fun onFailure(call: Call<Appointments>, t: Throwable) {
                                    progressDialog.dismissLoading()
                                    Toast.makeText(
                                        requireContext(),
                                        "Appointment request failed. Please contact Autopia for resolution.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    } else if (response.isSuccessful && response.body().isNullOrEmpty()) {
                        if (appointmentId != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                            val cal = Calendar.getInstance()
                            val now = sdf.format(cal.time)
                            val appointment = Appointments(
                                appointmentDetails.id,
                                appointmentDetails.services,
                                appointmentDetails.serviceId,
                                appointmentDetails.date,
                                appointmentDetails.startTime,
                                appointmentDetails.duration,
                                appointmentDetails.endTime,
                                appointmentDetails.color,
                                appointmentDetails.vehicle,
                                appointmentDetails.vehicleId,
                                appointmentDetails.phoneNo,
                                appointmentDetails.workshopPhoneNo,
                                appointmentDetails.description,
                                appointmentDetails.workshopId,
                                appointmentDetails.workshopName,
                                appointmentDetails.clientId,
                                appointmentDetails.clientName,
                                status,
                                appointmentDetails.attachment,
                                appointmentDetails.remarks,
                                appointmentDetails.quotedPrice,
                                now
                            )
                            val apiInterface = ApiInterface.create().postAppointments(appointment)
                            apiInterface.enqueue(object : Callback<Appointments> {
                                override fun onResponse(
                                    call: Call<Appointments>,
                                    response: Response<Appointments>,
                                ) {
                                    if (response.isSuccessful) {
                                        setServicesStatistics(
                                            appointment.workshopId,
                                            appointment.services
                                        )
                                        setBookingStatistics(appointment.workshopId)
                                        setCustomer(appointment.clientId, appointment.workshopId)
                                        Toast.makeText(
                                            requireContext(),
                                            "Appointment requested. Please wait for reply from the selected workshop.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        OneSignalNotificationService().createAppointmentNotification(
                                            appointmentDetails.workshopId,
                                            "${appointmentDetails.clientName} had requested for an appointment",
                                            "Date: ${appointmentDetails.date}\nStart Time: ${appointmentDetails.startTime}"
                                        )
                                        progressDialog.dismissLoading()
                                    }
                                }

                                override fun onFailure(call: Call<Appointments>, t: Throwable) {
                                    progressDialog.dismissLoading()
                                    Toast.makeText(
                                        requireContext(),
                                        "Appointment request failed. Please contact Autopia for resolution.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                    }
                } catch (exception: SocketTimeoutException) {
                    Log.d("socket timeout liao", "socket timeout liao")
                }
            }
        }
    }

    //check is customer record existing
    private fun setCustomer(uid: String, workshopId: String) {
        FirestoreClass().fetchUserByID(uid).addOnSuccessListener {
            val customer = Customers(
                null,
                uid,
                workshopId,
                it.data?.get("promotion").toString()
            )
            if (view != null) {
                viewModel.getDuplicatedCustomer(workshopId, uid)
                viewModel.customers.observe(viewLifecycleOwner) { response ->
                    if (response.body().isNullOrEmpty()) {
                        val apiInterface = ApiInterface.create().postCustomers(customer)
                        apiInterface.enqueue(object : Callback<Customers> {
                            override fun onResponse(
                                call: Call<Customers>,
                                response: Response<Customers>,
                            ) {
                                Log.d("customer added", "yeayyyy")

                                val navController =
                                    requireActivity().findNavController(R.id.nav_host_fragment)
                                navController.navigate(R.id.homeFragment)
                            }

                            override fun onFailure(call: Call<Customers>, t: Throwable) {
                                Log.d("customer not added", "whyyyyy")
                            }
                        })
                    } else {
                        Log.d("customer existing", "no add")
                        val navController =
                            requireActivity().findNavController(R.id.nav_host_fragment)
                        navController.navigate(R.id.homeFragment)
                    }
                }
            } else {
                Log.d("View", "is empty")
            }
        }
    }

    private fun setBookingStatistics(workshopId: String) {
        val midnight: LocalTime = LocalTime.MIDNIGHT
        val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur"))
        val todayMidnight = LocalDateTime.of(today, midnight)
        val millis = todayMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        FirebaseFirestore.getInstance().collection(Constants.BookingStatistics)
            .whereEqualTo("workshopId", workshopId).whereEqualTo("millis", millis).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (doc in task.result.documents) {
                        val statistics = BookingStatistics(
                            workshopId,
                            millis,
                            doc.getTimestamp("date")?.toDate(),
                            doc.data?.get("bookingCount").toString().toInt() + 1,
                        )
                        FirebaseFirestore.getInstance().collection(Constants.BookingStatistics)
                            .document(doc.id).set(statistics)
                    }
                } else {
                    val statistics = hashMapOf(
                        "workshopId" to workshopId,
                        "millis" to millis,
                        "date" to cal.time,
                        "bookingCount" to 1
                    )
                    FirebaseFirestore.getInstance().collection(Constants.BookingStatistics)
                        .add(statistics)
                }
            }
    }

    private fun setServicesStatistics(workshopId: String, service: String) {
        FirebaseFirestore.getInstance().collection(Constants.ServicesStatistics)
            .whereEqualTo("workshopId", workshopId).whereEqualTo("service", service).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (doc in task.result.documents) {
                        val services = hashMapOf(
                            "workshopId" to workshopId,
                            "service" to service,
                            "count" to doc.data?.get("count").toString().toInt() + 1,
                        )
                        FirebaseFirestore.getInstance().collection(Constants.ServicesStatistics)
                            .document(doc.id).set(services)
                    }
                } else {
                    val services = hashMapOf(
                        "workshopId" to workshopId,
                        "service" to service,
                        "count" to 1,
                    )
                    FirebaseFirestore.getInstance().collection(Constants.ServicesStatistics)
                        .add(services)
                }
            }
    }

    private fun isTimeOverlapping(
        start1: LocalTime,
        end1: LocalTime,
        start2: LocalTime,
        end2: LocalTime
    ): Boolean {
        if (start1.isAfter(end1)) { // interval 1 crosses midnight
            return if (start2.isAfter(end2)) { // both intervals cross midnight, so they overlap at midnight
                true
            } else isTimeOverlapping(start2, end2, start1, end1)
            // Swap the intervals so interval 1 does not cross midnight
        }

        // Now we know that interval 1 cannot cross midnight
        return if (start2.isAfter(end2)) { // Interval 2 crosses midnight
            start2.isBefore(end1) || end2.isAfter(start1)
        } else { // None of the intervals crosses midnight
            start2.isBefore(end1) && end2.isAfter(start1)
        }
    }
}