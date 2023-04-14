package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.String.valueOf
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class RescheduleAppointmentsFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var viewModel: ApiViewModel
    private lateinit var hourPicker: com.shawnlin.numberpicker.NumberPicker
    private lateinit var minPicker: com.shawnlin.numberpicker.NumberPicker

    private lateinit var progressDialog: ProgressDialog
    var initialHour: Int = 0
    var initialMinute: Int = 0
    var maxValueHour: Int = 23
    var maxValueMinute: Int = 59
    var minValueHour: Int = 0
    var minValueMinute: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reschedule_appointments, container, false)
    }

    override fun onStart() {
        super.onStart()

        progressDialog = ProgressDialog(requireActivity())
        val uid = arguments?.getString("uid")
        val userType = arguments?.getString("user_type")
        val appointmentId = arguments?.getInt("appointment_id")
        val time = arguments?.getString("time")
        val date = arguments?.getString("date")
        val username = arguments?.getString("username")
        val workshopId = arguments?.getString("workshop_id")

        val dateField: EditText? = requireActivity().findViewById(R.id.reschedule_date)
        dateField?.setText(date)
        dateField?.setOnClickListener { v -> openDatePickerDialog(v) }

        val timeField: EditText? = requireActivity().findViewById(R.id.reschedule_time)
        timeField?.setText(time)
        timeField?.setOnClickListener { openTimePickerDialog(workshopId!!) }

        val button: Button? = requireActivity().findViewById(R.id.rescheduleConfirmButton)

        if (uid != null) {
            if (userType == "workshop") {
                FirestoreClass().fetchUserByID(uid).addOnSuccessListener {
                    hourPicker = requireActivity().findViewById(R.id.reschedule_hours)
                    minPicker = requireActivity().findViewById(R.id.reschedule_minutes)
                    setupTimePickerLayout()
                    if (view != null) {
                        val repository = Repository()
                        val viewModelFactory = ApiViewModelFactory(repository)
                        viewModel =
                            ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                        if (appointmentId != null && view != null) {
                            viewModel.getAppointmentById(appointmentId.toInt())
                            viewModel.appointment.observe(viewLifecycleOwner) { response ->
                                if (response.isSuccessful) {
                                    val body = response.body()!!
                                    val duration = body.duration
                                    if (duration != null) {
                                        if (duration > 60) {
                                            val division = duration / 60.0
                                            val integer = division.toInt()
                                            val bigDecimal = BigDecimal(valueOf(division))
                                            val decimal =
                                                bigDecimal.subtract(BigDecimal(division)).toDouble()
                                            val min = decimal * 60.0
                                            if (integer != 0)
                                                hourPicker.value = integer
                                            minPicker.value = min.toInt()
                                            button?.setOnClickListener {
                                                if (username != null) {
                                                    loadAppointment(
                                                        appointmentId.toInt(),
                                                        body,
                                                        userType,
                                                        username,
                                                        uid
                                                    )
                                                }
                                            }
                                        } else {
                                            minPicker.value = duration
                                            button?.setOnClickListener {
                                                if (username != null) {
                                                    loadAppointment(
                                                        appointmentId.toInt(),
                                                        body,
                                                        userType,
                                                        username,
                                                        uid
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val textView: TextView? = requireActivity().findViewById(R.id.textView0012)
                val layout: ConstraintLayout? = requireActivity().findViewById(R.id.cl1)
                textView?.visibility = View.GONE
                layout?.visibility = View.GONE
                button?.setOnClickListener {
                    if (appointmentId != null && userType != null && username != null) {
                        loadAppointment(appointmentId.toInt(), null, userType, username, uid)
                    }
                }
            }
        }
    }

    private fun rescheduleAppointment(
        appointment: Appointments,
        appointmentId: Int,
        username: String,
        userType: String
    ) {
        Log.d("rescheduleAppointment", "why")
        val apiInterface = ApiInterface.create().putAppointments(appointment, appointmentId)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Toast.makeText(
                    requireContext(),
                    "Request for appointment rescheduling had been sent. Please wait for response.",
                    Toast.LENGTH_SHORT
                ).show()
                Thread.sleep(2000)
                Log.d("ghost", userType)
                if (userType == "workshop") {
                    progressDialog.dismissLoading()
                    OneSignalNotificationService().createAppointmentNotification(
                        appointment.clientId,
                        "$username had requested for appointment rescheduling with new date time proposed",
                        "New Date: ${appointment.date}\nNew Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                    )
                    val navController =
                        requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                    navController.popBackStack()
                } else {
                    progressDialog.dismissLoading()
                    OneSignalNotificationService().createAppointmentNotification(
                        appointment.workshopId,
                        "$username had requested for appointment rescheduling with new date time proposed",
                        "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\n"
                    )
                    val navController =
                        requireActivity().findNavController(R.id.nav_host_fragment)
                    navController.popBackStack()
                }
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
            }
        })
    }

    private fun loadAppointment(
        appointmentId: Int,
        res: Appointments?,
        userType: String,
        username: String,
        userId: String
    ) {
        var appointment: Appointments
        progressDialog.startLoading()
        val date =
            requireActivity().findViewById<EditText>(R.id.reschedule_date)?.text.toString()
        val time =
            requireActivity().findViewById<EditText>(R.id.reschedule_time)?.text.toString()
        if (res == null && userType == "user") {
            if (view != null) {
                val repository = Repository()
                val viewModelFactory = ApiViewModelFactory(repository)
                viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                viewModel.getAppointmentById(appointmentId)
                viewModel.appointment.observe(viewLifecycleOwner) { response ->
                    if (response.isSuccessful) {
                        val body = response.body()!!
                        if (body.duration != 0) {
                            val cal = Calendar.getInstance()
                            val df = SimpleDateFormat("hh:mm aa")
                            val start = df.parse(time)
                            if (start != null) {
                                cal.time = start
                            }
                            cal.add(Calendar.MINUTE, body.duration!!)
                            var endTime = ""
                            val sdf = SimpleDateFormat("hh:mm a")
                            endTime = sdf.format(cal.time)

                            FirestoreClass().fetchUserByID(userId).addOnSuccessListener {
                                appointment = Appointments(
                                    body.id,
                                    body.services,
                                    body.serviceId,
                                    date,
                                    time,
                                    body.duration,
                                    endTime,
                                    body.color,
                                    body.vehicle,
                                    body.vehicleId,
                                    body.phoneNo,
                                    body.workshopPhoneNo,
                                    body.description,
                                    body.workshopId,
                                    body.workshopName,
                                    body.clientId,
                                    body.clientName,
                                    "reschedule user",
                                    body.attachment,
                                    body.remarks,
                                    body.quotedPrice,
                                    body.bookDate
                                )
                                rescheduleAppointment(
                                    appointment,
                                    appointmentId,
                                    username,
                                    it.get("userType").toString()
                                )
                            }
                        } else {
                            appointment = Appointments(
                                body.id,
                                body.services,
                                body.serviceId,
                                date,
                                time,
                                body.duration,
                                body.endTime,
                                body.color,
                                body.vehicle,
                                body.vehicleId,
                                body.phoneNo,
                                body.workshopPhoneNo,
                                body.description,
                                body.workshopId,
                                body.workshopName,
                                body.clientId,
                                body.clientName,
                                "reschedule user",
                                body.attachment,
                                body.remarks,
                                body.quotedPrice,
                                body.bookDate
                            )
                            rescheduleAppointment(
                                appointment,
                                appointmentId,
                                username,
                                "user"
                            )
                        }
                    }
                }
            }
        } else if (res != null && userType == "workshop") {
            val duration: Int
            val hour = hourPicker.value
            val min = minPicker.value
            duration = hour * 60 + min
            if (duration > 0) {
                val cal = Calendar.getInstance()
                val df = SimpleDateFormat("hh:mm aa")
                val start = df.parse(time)
                if (start != null) {
                    cal.time = start
                }
                cal.add(Calendar.MINUTE, duration)
                val endTime: String
                val sdf = SimpleDateFormat("hh:mm a")
                endTime = sdf.format(cal.time)
                FirestoreClass().fetchUserByID(userId).addOnSuccessListener {
                    appointment = Appointments(
                        res.id,
                        res.services,
                        res.serviceId,
                        date,
                        time,
                        duration,
                        endTime,
                        res.color,
                        res.vehicle,
                        res.vehicleId,
                        res.phoneNo,
                        res.workshopPhoneNo,
                        res.description,
                        res.workshopId,
                        res.workshopName,
                        res.clientId,
                        res.clientName,
                        "reschedule workshop",
                        res.attachment,
                        res.remarks,
                        res.quotedPrice,
                        res.bookDate
                    )
                    Log.d("loadAppointment", "why")
                    rescheduleAppointment(
                        appointment,
                        appointmentId,
                        username,
                        "workshop"
                    )
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please propose an appointment duration.",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismissLoading()
            }
        }
    }

//                        FirestoreClass().fetchUserByID(userId).addOnSuccessListener {
//                            if (it.get("userType") == "workshop") {
//                                appointment = Appointments(
//                                    body.id,
//                                    body.services,
//                                    body.serviceId,
//                                    date,
//                                    time,
//                                    body.duration,
//                                    body.endTime,
//                                    body.color,
//                                    body.vehicle,
//                                    body.vehicleId,
//                                    body.phoneNo,
//                                    body.workshopPhoneNo,
//                                    body.description,
//                                    body.workshopId,
//                                    body.workshopName,
//                                    body.clientId,
//                                    body.clientName,
//                                    "reschedule workshop",
//                                    body.attachment,
//                                    body.remarks,
//                                    body.quotedPrice
//                                )
//                                rescheduleAppointment(
//                                    appointment,
//                                    appointmentId,
//                                    username,
//                                    it.get("userType").toString()
//                                )

    @SuppressLint("SimpleDateFormat")
    private fun openDatePickerDialog(v: View) {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
        val builder =
            MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraintsBuilder.build())
                .setTitleText("Select Appointment Date")
        val picker = builder.build()
        picker.show(requireActivity().supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { datePicked ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = datePicked;
            val format: SimpleDateFormat
            SimpleDateFormat("yyyy-MM-dd").also { format = it }
            val formatted: String = format.format(calendar.time)

            val date: EditText? = requireActivity().findViewById(R.id.reschedule_date)
            date?.setText(formatted)

            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("date", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("date", formatted)
            editor.apply()
        }
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
        val time: EditText? = requireActivity().findViewById(R.id.reschedule_time)

        time?.setText(formattedTime)

        val preferences: SharedPreferences =
            requireContext().getSharedPreferences("time", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("time", formattedTime)
        editor.apply()
    }

    private fun setupTimePickerLayout() {
        setupMaxValues()
        setupMinValues()
        setupInitialValues()
    }

    private fun setupMaxValues() {
        hourPicker.maxValue = maxValueHour
        minPicker.maxValue = maxValueMinute

    }

    private fun setupMinValues() {
        hourPicker.minValue = minValueHour
        minPicker.minValue = minValueMinute
    }

    private fun setupInitialValues() {
        hourPicker.value = initialHour
        minPicker.value = initialMinute
    }
}