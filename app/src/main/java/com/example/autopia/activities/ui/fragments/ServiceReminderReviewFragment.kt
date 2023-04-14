package com.example.autopia.activities.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.model.ServiceReminders
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProgressDialog
import com.example.autopia.activities.utils.ReminderReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class ServiceReminderReviewFragment : Fragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        return inflater.inflate(R.layout.fragment_service_reminder_review, container, false)
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onStop() {
        super.onStop()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceReminderId = arguments?.getInt("service_reminder_id")
        val serviceId = arguments?.getInt("service_id")
        val service = arguments?.getString("service")
        val appointmentId = arguments?.getInt("appointment_id")
        val date = arguments?.getString("date")
        val duration = arguments?.getInt("duration")
        val clientId = arguments?.getString("client_id")
        val workshopId = arguments?.getString("workshop_id")
        val mileage = arguments?.getInt("mileage")

        val workshopName: TextView? = requireActivity().findViewById(R.id.workshop_name)
        val workshopDetails: TextView? = requireActivity().findViewById(R.id.workshop_details)
        val serviceText: TextView? = requireActivity().findViewById(R.id.review_service)
        val vehicleText: TextView? = requireActivity().findViewById(R.id.review_vehicle)
        val nameText: TextView? = requireActivity().findViewById(R.id.review_name)
        val phoneText: TextView? = requireActivity().findViewById(R.id.review_phone)
        val descriptionText: TextView? = requireActivity().findViewById(R.id.review_description)
        val appointmentDate: TextView? =
            requireActivity().findViewById(R.id.review_appointment_date)
        val appointmentTime: TextView? =
            requireActivity().findViewById(R.id.review_appointment_time)
        val quotation: TextView? = requireActivity().findViewById(R.id.review_quoted_price)
        val btn: Button? = requireActivity().findViewById(R.id.review_button)

        val sp = requireActivity().getSharedPreferences("details", Context.MODE_PRIVATE)
        val editor = sp.edit()

        if (workshopId != null) {
            FirestoreClass().fetchWorkshopInfo(workshopId).addOnSuccessListener { snapshot ->
                workshopName?.text = snapshot.data?.get("workshopName").toString()
                workshopDetails?.text = snapshot.data?.get("description").toString()
            }
        }

        if (appointmentId != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getAppointmentById(appointmentId)
            viewModel.appointment.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    vehicleText?.text = body?.vehicle
                    editor.putInt("vehicle_id", body?.vehicleId!!)
                    editor.apply()
                }
            }
            serviceText?.text = service
            appointmentDate?.text = date
        }

        if (serviceId != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getServiceById(serviceId)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel.service.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    quotation?.text = body?.quotation.toString()
                }
            }
            serviceText?.text = service
            progressDialog.dismissLoading()
        }

        appointmentTime?.setOnClickListener { openTimePickerDialog(workshopId!!) }

        btn?.setOnClickListener {
            val isValid = validateInputFields()
            if (isValid) {
                val sharedPreferences =
                    requireActivity().getSharedPreferences("appointment_time", Context.MODE_PRIVATE)
                val time = sharedPreferences.getString("appointment_time", "")
                val sdf = SimpleDateFormat("hh:mm a")
                val formatted = time?.let { it1 -> sdf.parse(it1) }
                val cal = Calendar.getInstance()
                if (formatted != null) {
                    cal.time = formatted
                }
                cal.add(Calendar.MINUTE, duration!!)
                var endTime = ""
                endTime = sdf.format(cal.time)
                val sharedPref =
                    requireActivity().getSharedPreferences("details", Context.MODE_PRIVATE)
                val vehicleId = sharedPref.getInt("vehicle_id", 0)
                if (workshopId != null && clientId != null) {
                    FirestoreClass().fetchWorkshopInfo(workshopId)
                        .addOnSuccessListener { snapshot ->
                            val repository = Repository()
                            var isOverlapped = false
                            var status = ""
                            val viewModelFactory = ApiViewModelFactory(repository)
                            val progressDialog = ProgressDialog(requireActivity())
                            progressDialog.startLoading()
                            viewModel =
                                ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                            viewModel.getAcceptedAppointmentsByWorkshopId(workshopId)
                            viewModel.appointments.observe(viewLifecycleOwner) { response ->
                                try {
                                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                                        val appointments = response.body()!!
                                        appointments.forEach { appointment ->
                                            val isTimeOverlapping = isTimeOverlapping(
                                                LocalTime.parse(
                                                    time,
                                                    DateTimeFormatter.ofPattern("hh:mm a")
                                                ),
                                                LocalTime.parse(
                                                    endTime,
                                                    DateTimeFormatter.ofPattern("hh:mm a")
                                                ),
                                                LocalTime.parse(
                                                    appointment.startTime,
                                                    DateTimeFormatter.ofPattern("hh:mm a")
                                                ),
                                                LocalTime.parse(
                                                    appointment.endTime,
                                                    DateTimeFormatter.ofPattern("hh:mm a")
                                                )
                                            )
                                            if (isTimeOverlapping) {
                                                isOverlapped = true
                                                Log.d("overlapped", "o")
                                            }
                                        }
                                        progressDialog.dismissLoading()
                                    }
                                } catch (exception: SocketTimeoutException) {
                                    Log.d("socket timeout liao", "socket timeout liao")
                                }
                            }
                            status = if (isOverlapped) {
                                "clashed"
                            } else {
                                "accepted"
                            }
                            val sdff = SimpleDateFormat("yyyy-MM-dd")
                            val call = Calendar.getInstance()
                            val now = sdff.format(call.time)
                            val appointment = Appointments(
                                null,
                                service!!,
                                serviceId!!,
                                date!!,
                                appointmentTime?.text.toString(),
                                duration,
                                endTime,
                                "#999FFF",
                                vehicleText?.text.toString(),
                                vehicleId,
                                phoneText?.text.toString(),
                                snapshot.data?.get("contactNumber").toString(),
                                descriptionText?.text.toString(),
                                workshopId,
                                snapshot.data?.get("workshopName").toString(),
                                clientId,
                                nameText?.text.toString(),
                                status, //accepted
                                "",
                                "",
                                quotation?.text.toString().toDouble(),
                                now
                            )

                            val apiInterface =
                                ApiInterface.create().postAppointments(appointment)
                            apiInterface.enqueue(object : Callback<Appointments> {
                                override fun onResponse(
                                    call: Call<Appointments>,
                                    response: Response<Appointments>,
                                ) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Appointment booked successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        FirestoreClass().fetchUserByID(clientId)
                                            .addOnSuccessListener {
                                                val serviceReminders = ServiceReminders(
                                                    serviceReminderId,
                                                    service,
                                                    serviceId,
                                                    date,
                                                    duration,
                                                    appointmentId!!,
                                                    workshopId,
                                                    clientId,
                                                    "",
                                                    "",
                                                    mileage!!,
                                                    "accepted"
                                                )
                                                updateServiceReminderStatus(
                                                    serviceReminders,
                                                    serviceReminderId!!
                                                )
                                                OneSignalNotificationService().createAppointmentNotification(
                                                    workshopId,
                                                    it.data?.get("username")
                                                        .toString() + " had accepted your service reminder",
                                                    "Date: ${date}\nStart Time: ${appointmentTime?.text}\nEstimated Duration: $duration minutes\nEstimated End Time: $endTime"
                                                )
                                                val intent = Intent(
                                                    requireContext(),
                                                    ReminderReceiver::class.java
                                                ).putExtra("appointment_id", appointmentId)

                                                val pendingIntent =
                                                    PendingIntent.getBroadcast(
                                                        requireContext(),
                                                        0,
                                                        intent,
                                                        0
                                                    )

                                                val alarmManager: AlarmManager =
                                                    requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                                val sdf = SimpleDateFormat("hh:mm a")
                                                sdf.timeZone = TimeZone.getTimeZone("GMT+8")
                                                val sdf2 = SimpleDateFormat("HH:mm")
                                                sdf2.timeZone = TimeZone.getTimeZone("GMT+8")
                                                val formattedTime = sdf.parse(appointment.startTime)
                                                val format = sdf2.format(formattedTime!!)
                                                val time = sdf2.parse(format)
                                                val reminderDate = Calendar.getInstance()
                                                if (time != null) {
                                                    reminderDate.set(
                                                        appointment.date.dropLast(6).toInt(),
                                                        appointment.date.drop(5).dropLast(3)
                                                            .toInt() - 1,
                                                        appointment.date.drop(8).toInt(),
                                                        time.hours - 1,
                                                        time.minutes
                                                    )
                                                }
                                                alarmManager.set(
                                                    AlarmManager.RTC_WAKEUP,
                                                    reminderDate.timeInMillis,
                                                    pendingIntent
                                                )
                                            }
                                        progressDialog.dismissLoading()
                                        val navController =
                                            requireActivity().findNavController(R.id.nav_host_fragment)
                                        navController.navigate(R.id.homeFragment)
                                    }
                                }

                                override fun onFailure(call: Call<Appointments>, t: Throwable) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Appointment booking failed. Please contact Autopia for resolution.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressDialog.dismissLoading()
                                }
                            })
                        }
                }
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
        val serviceText: TextView? = requireActivity().findViewById(R.id.review_service)
        val vehicleText: TextView? = requireActivity().findViewById(R.id.review_vehicle)
        val nameText: TextView? = requireActivity().findViewById(R.id.review_name)
        val phoneText: TextView? = requireActivity().findViewById(R.id.review_phone)
        val descriptionText: TextView? = requireActivity().findViewById(R.id.review_description)
        val appointmentDate: TextView? =
            requireActivity().findViewById(R.id.review_appointment_date)
        val appointmentTime: TextView? =
            requireActivity().findViewById(R.id.review_appointment_time)
        val quotation: TextView? = requireActivity().findViewById(R.id.review_quoted_price)

        return !(serviceText?.text?.isEmpty() == true || vehicleText?.text?.isEmpty() == true || nameText?.text?.isEmpty() == true || phoneText?.text?.isEmpty() == true || descriptionText?.text?.isEmpty() == true || appointmentDate?.text?.isEmpty() == true || appointmentTime?.text?.isEmpty() == true || quotation?.text?.isEmpty() == true)
    }

    private fun openTimePickerDialog(workshopId: String) {
        FirestoreClass().fetchWorkshopInfo(workshopId).addOnSuccessListener {
            val calendar = Calendar.getInstance()
            val timePicker = TimePickerDialog.newInstance(
                this, calendar.time.hours, calendar.time.minutes, false
            )
            timePicker.version = TimePickerDialog.Version.VERSION_2
            val openHours = Timepoint(
                it.data?.get("openHours").toString().dropLast(3).toInt(),
                it.data?.get("openHours").toString().drop(3).toInt()
            )
            val closeHours = Timepoint(
                it.data?.get("closeHours").toString().dropLast(3).toInt(),
                it.data?.get("closeHours").toString().drop(3).toInt()
            )
            timePicker.setMinTime(openHours)
            timePicker.setMaxTime(closeHours)
            timePicker.show(requireActivity().supportFragmentManager, "TimePickerDialog")
        }
    }

    override fun onTimeSet(
        timePicker: TimePickerDialog?,
        hourOfDay: Int,
        minute: Int,
        second: Int
    ) {
        val formatted = "$hourOfDay:$minute"
        val sdf = SimpleDateFormat("HH:mm")
        val sdf2 = SimpleDateFormat("hh:mm a")
        val processed = sdf.parse(formatted)
        val formattedTime = sdf2.format(processed!!)
        val time: EditText? = requireActivity().findViewById(R.id.review_appointment_time)
        time?.setText(formattedTime)

        val preferences: SharedPreferences =
            requireActivity().getSharedPreferences("appointment_time", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("appointment_time", formattedTime)
        editor.apply()
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

    private fun updateServiceReminderStatus(serviceReminders: ServiceReminders, id: Int) {
        val apiInterface =
            ApiInterface.create().putServiceReminders(serviceReminders, id)
        apiInterface.enqueue(object : Callback<ServiceReminders> {
            override fun onResponse(
                call: Call<ServiceReminders>,
                response: Response<ServiceReminders>,
            ) {
            }

            override fun onFailure(call: Call<ServiceReminders>, t: Throwable) {
            }
        })
    }
}