package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.time.Timepoint
import java.text.SimpleDateFormat
import java.util.*


class BookingDateTimeFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var viewModel: ApiViewModel
    private var serviceId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_booking_date_time, container, false)
    }

    override fun onStart() {
        super.onStart()
        val date: EditText? = requireActivity().findViewById(R.id.booking_date)
        date?.setOnClickListener { v -> openDatePickerDialog(v) }

        val time: EditText? = requireActivity().findViewById(R.id.booking_time)
        time?.setOnClickListener { v -> openTimePickerDialog(v) }
    }

    override fun onResume() {
        super.onResume()
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("service_name")
            ?.observe(viewLifecycleOwner) { it ->
                val bookingService: EditText? =
                    requireActivity().findViewById(R.id.booking_services_type)
                bookingService?.setText(it)
                findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("service_name")
                    ?.observe(viewLifecycleOwner) { serviceName ->
                        val serviceTV: TextView? =
                            requireActivity().findViewById(R.id.booking_service_final)
                        (serviceName).also {
                            if (serviceTV != null) {
                                serviceTV.text = it
                            }
                        }
                        val preferences: SharedPreferences =
                            requireContext().getSharedPreferences("booking", Context.MODE_PRIVATE)
                        val editor = preferences.edit()
                        editor.putString("service", serviceName)
                        editor.apply()
                        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Double>(
                            "service_quotation"
                        )
                            ?.observe(viewLifecycleOwner) { quotation ->
                                val quote: TextView? =
                                    requireActivity().findViewById(R.id.booking_service_quotation)
                                ("Quotation: RM$quotation").also { quote?.text = it }
                                editor.putString("quote", quotation.toString())
                                editor.apply()
                            }
                    }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bookingService: EditText? = requireActivity().findViewById(R.id.booking_services_type)
        val workshopId = arguments?.getString("workshop_id")

        bookingService?.setOnClickListener {
            val bundle = bundleOf("workshopId" to workshopId)
            findNavController().navigate(R.id.viewServicesFragment, bundle)
        }

        if (workshopId != null) {
            FirestoreClass().fetchWorkshopInfo(workshopId).addOnCompleteListener {
                val workshopName: TextView? =
                    requireActivity().findViewById(R.id.booking_date_time_sp_name)
                workshopName?.text = it.result.data?.get("workshopName").toString()

                val description: TextView? =
                    requireActivity().findViewById(R.id.booking_date_time_sp_details)
                description?.text = it.result.data?.get("description").toString()
            }
            loadServiceData(workshopId)
        }

        val bookingButton: Button? = requireActivity().findViewById(R.id.booking_time_button)

        bookingButton?.setOnClickListener {
            val isValid: Boolean = validateInputFields()
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("service_id")
                ?.observe(viewLifecycleOwner) { id ->
                    serviceId = id
                }

            if (isValid) {
                val user = FirebaseAuth.getInstance().currentUser

                val preferences: SharedPreferences? =
                    requireContext().getSharedPreferences("date", Context.MODE_PRIVATE)
                val date: String? = preferences?.getString("date", "")

                val pref: SharedPreferences? =
                    requireContext().getSharedPreferences("time", Context.MODE_PRIVATE)
                val time: String? = pref?.getString("time", "")

                val preference: SharedPreferences? =
                    requireContext().getSharedPreferences("booking", Context.MODE_PRIVATE)
                val service: String? = preference?.getString("service", "")
                val quotation: String? = preference?.getString("quote", "")

                if (user != null) {
                    val sharedPreferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "appointment_details",
                            Context.MODE_PRIVATE
                        )

                    val gson = Gson()
                    val json: String? = sharedPreferences.getString("appointment_details", "")
                    val appointmentDetails: Appointments =
                        gson.fromJson(json, Appointments::class.java)

                    val appointment = Appointments(
                        appointmentDetails.id,
                        service.toString(),
                        serviceId,
                        date.toString(),
                        time.toString(),
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
                        appointmentDetails.appointmentStatus,
                        appointmentDetails.attachment,
                        appointmentDetails.remarks,
                        quotation.toString().toDouble(),
                        appointmentDetails.bookDate
                    )

                    val gsonn = Gson()
                    val jsonn = gsonn.toJson(appointment)

                    val sharePreferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "appointment_details",
                            Context.MODE_PRIVATE
                        )
                    val editor = sharePreferences.edit()

                    editor.putString("appointment_details", jsonn)
                    editor.apply()
                    Navigation.findNavController(requireView())
                        .navigate(R.id.bookingReviewFragment)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun loadServiceData(workshopId: String) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val serviceList: ArrayList<String> = ArrayList()
            val mutableList: HashSet<String> = HashSet()
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("booking", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getServicesByWorkshopId(workshopId)
            viewModel.services.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    for (item in response.body()!!) {
                        serviceList.add(item.name)
                        mutableList.add(item.quotation.toString())
                    }
                    editor.putStringSet("quotation", mutableList)
                    editor.apply()
                } else {
                    Log.d("problem", response.message())
                }
            }
        }
    }

    private fun validateInputFields(): Boolean {
        val date: EditText? = requireActivity().findViewById(R.id.booking_date)
        val time: EditText? = requireActivity().findViewById(R.id.booking_time)
        val dateText: String = date?.text.toString().trim { it <= ' ' }
        val timeText: String = time?.text.toString().trim { it <= ' ' }

        return !(dateText.isEmpty() || timeText.isEmpty())
    }

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
            calendar.timeInMillis = datePicked
            val format: SimpleDateFormat
            SimpleDateFormat("yyyy-MM-dd").also { format = it }
            val formatted: String = format.format(calendar.time)

            val date: EditText? = requireActivity().findViewById(R.id.booking_date)
            date?.setText(formatted)

            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("date", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("date", formatted)
            editor.apply()
        }
    }

    private fun openTimePickerDialog(v: View) {
        val workshopId = arguments?.getString("workshop_id")

        if (workshopId != null) {
            FirestoreClass().fetchWorkshopInfo(workshopId).addOnSuccessListener {
                val calendar = Calendar.getInstance()
                val timePicker = TimePickerDialog.newInstance(
                    this, calendar.time.hours, calendar.time.minutes, false
                )
                timePicker.version = TimePickerDialog.Version.VERSION_2
                val openHours = Timepoint(
                    it.data?.get("openHours").toString().dropLast(3).toInt(), it.data?.get("openHours").toString().drop(3).toInt()
                )
                val closeHours = Timepoint(
                    it.data?.get("closeHours").toString().dropLast(3).toInt(), it.data?.get("closeHours").toString().drop(3).toInt()
                )
                closeHours.add(Timepoint.TYPE.MINUTE, -30)
                timePicker.setMinTime(openHours)
                timePicker.setMaxTime(closeHours)
                timePicker.show(requireActivity().supportFragmentManager, "TimePickerDialog")
            }
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
        val time: EditText? = requireActivity().findViewById(R.id.booking_time)

        time?.setText(formattedTime)

        val preferences: SharedPreferences =
            requireContext().getSharedPreferences("time", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("time", formattedTime)
        editor.apply()
    }
}

