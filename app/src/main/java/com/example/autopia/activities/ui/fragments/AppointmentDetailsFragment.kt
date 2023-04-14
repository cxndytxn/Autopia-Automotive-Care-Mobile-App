package com.example.autopia.activities.ui.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.model.Vehicles
import com.example.autopia.activities.utils.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_appointment_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AppointmentDetailsFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType")?.equals("workshop") == true) {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
                        bottomNavigationView?.isVisible = true
                    } else {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.bottom_navigation_view)
                        bottomNavigationView?.isVisible = true
                    }
                }
            }
        }
        return inflater.inflate(R.layout.fragment_appointment_details, container, false)
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType") == "workshop" && isAdded) {
                        val appBarLayout: AppBarLayout? =
                            requireActivity().findViewById(R.id.workshop_app_bar_layout)
                        if (appBarLayout != null) {
                            appBarLayout.elevation = 8f
                        }
                    } else {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                        }
                    }
                }
            }
        } else {
            if (isAdded) {
                val appBarLayout: AppBarLayout? =
                    requireActivity().findViewById(R.id.app_bar_layout)
                appBarLayout?.elevation = 8f
            }
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val appointmentId = arguments?.getInt("appointment_id")
        val appointmentStatus = arguments?.getString("appointment_status")

        if (view != null) {
            if (appointmentId != null) {
                val repository = Repository()
                val viewModelFactory = ApiViewModelFactory(repository)
                viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
                viewModel.getAppointmentById(appointmentId)
                val progressDialog = ProgressDialog(requireActivity())
                progressDialog.startLoading()
                viewModel.appointment.observe(viewLifecycleOwner) { response ->
                    if (response.isSuccessful) {
                        val body = response.body()
                        val username: TextView? =
                            requireActivity().findViewById(R.id.appointment_username)
                        val service: TextView? =
                            requireActivity().findViewById(R.id.booking_review_service)
                        val plateNo: TextView? =
                            requireActivity().findViewById(R.id.appointment_vehicle_plate_no)
                        val phone: TextView? =
                            requireActivity().findViewById(R.id.appointment_phone_no)
                        val desc: TextView? =
                            requireActivity().findViewById(R.id.booking_review_description)
                        val date: TextView? =
                            requireActivity().findViewById(R.id.booking_review_date)
                        val startTime: TextView? =
                            requireActivity().findViewById(R.id.booking_review_time)
                        val quotation: TextView? =
                            requireActivity().findViewById(R.id.booking_review_quotation)
                        val duration: TextView? =
                            requireActivity().findViewById(R.id.appointment_duration)
                        val endTime: TextView? =
                            requireActivity().findViewById(R.id.appointment_end_time)
                        val userProfile: ImageView? =
                            requireActivity().findViewById(R.id.appointment_profile)
                        val chatButton: MaterialButton? =
                            requireActivity().findViewById(R.id.appointment_chat_button)
                        val phoneButton: MaterialButton? =
                            requireActivity().findViewById(R.id.appointment_phone_button)
                        if (body != null) {
                            loadVehicle(body.vehicleId)
                            progressDialog.dismissLoading()
                        }

                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            FirestoreClass().fetchUserByID(user.uid).addOnSuccessListener {
                                if (it.data?.get("userType") == "workshop") {
                                    user_details?.text = "Client"
                                    user_info_panel.visibility = View.GONE
                                    if (body != null) {
                                        username?.text = body.clientName
                                        phone?.text = body.phoneNo
                                        val workshopButtons: LinearLayout? =
                                            requireActivity().findViewById(R.id.workshop_buttons)
                                        val buttons: LinearLayout? =
                                            requireActivity().findViewById(R.id.workshopButtons)
                                        workshopButtons?.visibility = View.VISIBLE
                                        val acceptButton: Button? =
                                            requireActivity().findViewById(R.id.acceptButton)
                                        val rejectButton: Button? =
                                            requireActivity().findViewById(R.id.rejectButton)
                                        val rescheduleButton: Button? =
                                            requireActivity().findViewById(R.id.workshopRescheduleButton)
                                        val doneButton: Button? =
                                            requireActivity().findViewById(R.id.doneButton)
                                        if (doneButton != null) {
                                            doneButton.visibility = View.GONE
                                        }
                                        when (appointmentStatus) {
                                            "accepted" -> {
                                                acceptButton?.visibility = View.GONE
                                                buttons?.weightSum = 2F
                                                if (doneButton != null) {
                                                    doneButton.visibility = View.VISIBLE
                                                }
                                            }
                                            "rejected" -> {
                                                buttons?.visibility = View.GONE
                                            }
                                            "no show" -> {
                                                buttons?.visibility = View.GONE
                                            }
                                            "reschedule user" -> {
                                                buttons?.weightSum = 2f
                                                rescheduleButton?.visibility = View.GONE
                                            }
                                            "reschedule workshop" -> {
                                                buttons?.visibility = View.GONE
                                            }
                                            "done" -> {
                                                workshopButtons?.visibility = View.GONE
                                            }
                                        }

                                        phoneButton?.setOnClickListener {
                                            val intent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.fromParts("tel", body.phoneNo, null)
                                            )
                                            startActivity(intent)
                                        }

                                        chatButton?.setOnClickListener {
                                            FirestoreClass().fetchUserByID(body.clientId)
                                                .addOnSuccessListener { snapshot ->
                                                    val bundle = bundleOf(
                                                        "receiver_id" to body.clientId,
                                                        "receiver_name" to snapshot.data?.get("username")
                                                            .toString()
                                                    )
                                                    Navigation.findNavController(requireView())
                                                        .navigate(R.id.chatRoomFragment, bundle)
                                                }
                                        }

                                        acceptButton?.setOnClickListener {
                                            if (body.duration == 0) {
                                                val dialog = ProposeDurationDialog()
                                                dialog.setOnTimeSetOption("Set Time") { hour, minute ->
                                                    acceptAppointment(appointmentId, hour, minute)
                                                }
                                                dialog.setOnCancelOption("Cancel") {
                                                    dialog.dismiss()
                                                }
                                                dialog.show(
                                                    requireActivity().supportFragmentManager,
                                                    "dialog"
                                                )
                                            } else {
                                                acceptAppointment(appointmentId, 0, 0)
                                            }
                                        }

                                        rejectButton?.setOnClickListener {
                                            val bundle = bundleOf(
                                                "appointment_id" to appointmentId,
                                                "user_type" to "workshop",
                                                "uid" to user.uid,
                                                "time" to body.startTime,
                                                "date" to body.date,
                                                "username" to body.clientName,
                                                "workshop_id" to body.workshopId
                                            )
                                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                                            val formattedAppointmentDate = sdf.parse(body.date)
                                            val millionSeconds =
                                                formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
                                            val difference =
                                                TimeUnit.MILLISECONDS.toDays(millionSeconds)
                                            val canReschedule: Boolean = difference <= 2
                                            val dialog =
                                                RejectAppointmentDialog(bundle, canReschedule)
                                            if (appointmentStatus != "reschedule user" && appointmentStatus != "reschedule workshop") {
                                                dialog.setOnPositiveOption("Reschedule") {
                                                    findNavController().navigate(
                                                        R.id.rescheduleAppointmentsFragment,
                                                        bundle
                                                    )
                                                }
                                            } else {
                                                dialog.setOnPositiveOption("") {}
                                            }

                                            dialog.setOnCancelOption("Reject") { isReasonFilled: Boolean, reason: String ->
                                                if (isReasonFilled) {
                                                    rejectAppointment(appointmentId, reason)
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Please provide a reason for appointment rejection.",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            }
                                            dialog.show(
                                                requireActivity().supportFragmentManager,
                                                "reject_dialog"
                                            )
                                        }

                                        rescheduleButton?.setOnClickListener {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                                            val formattedAppointmentDate = sdf.parse(body.date)
                                            val millionSeconds =
                                                formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
                                            val difference =
                                                TimeUnit.MILLISECONDS.toDays(millionSeconds)
                                            if (difference < 2) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Reschedule request is only allowed at least 2 days before the actual appointment date!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val bundle = bundleOf(
                                                    "appointment_id" to appointmentId,
                                                    "user_type" to "workshop",
                                                    "uid" to user.uid,
                                                    "time" to body.startTime,
                                                    "date" to body.date,
                                                    "username" to body.workshopName,
                                                    "workshop_id" to body.workshopId
                                                )
                                                findNavController().navigate(
                                                    R.id.rescheduleAppointmentsFragment,
                                                    bundle
                                                )
                                            }
                                        }

                                        doneButton?.setOnClickListener {
                                            val appointment = Appointments(
                                                body.id,
                                                body.services,
                                                body.serviceId,
                                                body.date,
                                                body.startTime,
                                                body.duration,
                                                body.endTime,
                                                "#70e000",
                                                body.vehicle,
                                                body.vehicleId,
                                                body.phoneNo,
                                                body.workshopPhoneNo,
                                                body.description,
                                                body.workshopId,
                                                body.workshopName,
                                                body.clientId,
                                                body.clientName,
                                                "done",
                                                body.attachment,
                                                body.remarks,
                                                body.quotedPrice,
                                                body.bookDate
                                            )
                                            if (body.id != null) {
                                                val apiInterface =
                                                    ApiInterface.create().putAppointments(
                                                        appointment,
                                                        body.id
                                                    )
                                                apiInterface.enqueue(object :
                                                    Callback<Appointments> {
                                                    override fun onResponse(
                                                        call: Call<Appointments>,
                                                        response: Response<Appointments>,
                                                    ) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Appointment had been marked as done.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        OneSignalNotificationService().createAppointmentNotification(
                                                            appointment.clientId,
                                                            "${appointment.workshopName} marked your appointment as completed",
                                                            "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                                                        )
                                                        Thread.sleep(2000)
                                                        val bundle =
                                                            bundleOf("appointment_id" to appointment.id)
                                                        findNavController()
                                                            .navigate(
                                                                R.id.appointmentInfoFragment,
                                                                bundle
                                                            )
                                                    }

                                                    override fun onFailure(
                                                        call: Call<Appointments>,
                                                        t: Throwable
                                                    ) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Appointment could not be marked as done.",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }

                                                })
                                            }
                                        }
                                    }
                                } else {
                                    if (body != null) {
                                        username?.text = body.workshopName
                                        phone?.text = body.phoneNo
                                        val userButtons: LinearLayout? =
                                            requireActivity().findViewById(R.id.user_buttons)
                                        userButtons?.visibility = View.VISIBLE
                                        val userAcceptButton: Button? =
                                            requireActivity().findViewById(R.id.userAcceptButton)
                                        val rescheduleButton: Button? =
                                            requireActivity().findViewById(R.id.rescheduleButton)
                                        val cancelButton: Button? =
                                            requireActivity().findViewById(R.id.cancelButton)
                                        val userInfoPanel: ConstraintLayout? =
                                            requireActivity().findViewById(R.id.user_info_panel)
                                        val saveButton: Button? =
                                            requireActivity().findViewById(R.id.save_button)
                                        val remarks: TextView? =
                                            requireActivity().findViewById(R.id.remarks)
                                        val attachmentButton: MaterialButton? =
                                            requireActivity().findViewById(R.id.attachment_btn)
                                        //val docButton: MaterialButton = requireActivity().findViewById(R.id.doc_btn)

                                        when (appointmentStatus) {
                                            "rejected" -> {
                                                userButtons?.visibility = View.GONE
                                            }
                                            "no show" -> {
                                                userButtons?.visibility = View.GONE
                                            }
                                            "reschedule workshop" -> {
                                                userButtons?.weightSum = 2f
                                                rescheduleButton?.visibility = View.GONE
                                                "Reject".also { cancelButton?.text = it }
                                            }
                                            "reschedule user" -> {
                                                userButtons?.visibility = View.GONE
                                            }
                                            "pending" -> {
                                                userAcceptButton?.visibility = View.GONE
                                                userButtons?.weightSum = 2f
                                            }
                                            "accepted" -> {
                                                userAcceptButton?.visibility = View.GONE
                                                userButtons?.weightSum = 2f
                                            }
                                            "done" -> {
                                                userButtons?.visibility = View.GONE
                                                userInfoPanel?.visibility = View.VISIBLE

                                                if (body.remarks != "") {
                                                    remarks?.text = body.remarks
                                                }

                                                if (body.attachment != "") {
                                                    attachmentButton?.setIconResource(R.drawable.document)
                                                    attachmentButton?.text =
                                                        FirebaseStorage.getInstance()
                                                            .getReferenceFromUrl(body.attachment!!).name
                                                    attachmentButton?.setOnClickListener {
                                                        val pdfIntent = Intent(Intent.ACTION_VIEW)
                                                        pdfIntent.setDataAndType(
                                                            body.attachment.toUri(),
                                                            "application/pdf"
                                                        )
                                                        pdfIntent.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                        try {
                                                            requireContext().startActivity(pdfIntent)
                                                        } catch (e: ActivityNotFoundException) {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "No Application available to view PDF",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                } else {
                                                    attachmentButton?.setOnClickListener {
                                                        selectDocument()
                                                    }
                                                }

                                                saveButton?.setOnClickListener {
                                                    val text = remarks?.text.toString()
                                                    val sharedPreferences =
                                                        requireActivity().getSharedPreferences(
                                                            "uploaded_attachment",
                                                            Context.MODE_PRIVATE
                                                        )
                                                    val uri = sharedPreferences.getString(
                                                        "uploaded_attachment",
                                                        ""
                                                    )
                                                    val appointment = Appointments(
                                                        appointmentId,
                                                        body.services,
                                                        body.serviceId,
                                                        body.date,
                                                        body.startTime,
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
                                                        body.appointmentStatus,
                                                        uri,
                                                        text,
                                                        body.quotedPrice,
                                                        body.bookDate
                                                    )
                                                    val apiInterface =
                                                        ApiInterface.create()
                                                            .putAppointments(
                                                                appointment,
                                                                appointmentId
                                                            )
                                                    apiInterface.enqueue(object :
                                                        Callback<Appointments> {
                                                        override fun onResponse(
                                                            call: Call<Appointments>,
                                                            response: Response<Appointments>,
                                                        ) {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "The extra information had been saved!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }

                                                        override fun onFailure(
                                                            call: Call<Appointments>,
                                                            t: Throwable
                                                        ) {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "The extra information could not be saved.",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                        }
                                                    })

                                                }
                                            }
                                        }

                                        phoneButton?.setOnClickListener {
                                            val intent = Intent(
                                                Intent.ACTION_DIAL,
                                                Uri.fromParts("tel", body.workshopPhoneNo, null)
                                            )
                                            startActivity(intent)
                                        }

                                        chatButton?.setOnClickListener {
                                            val bundle = bundleOf(
                                                "receiver_id" to body.workshopId,
                                                "receiver_name" to body.workshopName
                                            )
                                            Navigation.findNavController(requireView())
                                                .navigate(R.id.chatRoomFragment, bundle)
                                        }

                                        rescheduleButton?.setOnClickListener {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd")
                                            val formattedAppointmentDate = sdf.parse(body.date)
                                            val millionSeconds =
                                                formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
                                            val difference =
                                                TimeUnit.MILLISECONDS.toDays(millionSeconds)
                                            if (difference < 2) {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Reschedule request is only allowed at least 2 days before the actual appointment date!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val bundle = bundleOf(
                                                    "appointment_id" to appointmentId,
                                                    "user_type" to "user",
                                                    "uid" to user.uid,
                                                    "time" to body.startTime,
                                                    "date" to body.date,
                                                    "username" to body.clientName,
                                                    "workshop_id" to body.workshopId
                                                )
                                                findNavController().navigate(
                                                    R.id.rescheduleAppointmentsFragment,
                                                    bundle
                                                )
                                            }
                                        }

                                        cancelButton?.setOnClickListener {
                                            val bundle = bundleOf(
                                                "appointment_id" to appointmentId,
                                                "user_type" to "user",
                                                "uid" to user.uid,
                                                "time" to body.startTime,
                                                "date" to body.date,
                                                "username" to body.clientName,
                                                "workshop_id" to body.workshopId
                                            )
                                            if (appointmentStatus != "reschedule workshop" && appointmentStatus != "reschedule user") {
                                                val sdf = SimpleDateFormat("yyyy-MM-dd")
                                                val formattedAppointmentDate = sdf.parse(body.date)
                                                val millionSeconds =
                                                    formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
                                                val difference =
                                                    TimeUnit.MILLISECONDS.toDays(millionSeconds)
                                                val canReschedule: Boolean = difference <= 2
                                                val dialog =
                                                    RejectAppointmentDialog(bundle, canReschedule)
                                                dialog.setOnPositiveOption("Reschedule") {
                                                    findNavController().navigate(
                                                        R.id.rescheduleAppointmentsFragment,
                                                        bundle
                                                    )
                                                }
                                                dialog.setOnCancelOption("Cancel Appointment") { isReasonFilled, reason ->
                                                    if (isReasonFilled) {
                                                        cancelAppointment(appointmentId, reason)
                                                    } else {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Please provide a reason for appointment cancellation.",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                }
                                                dialog.show(
                                                    requireActivity().supportFragmentManager,
                                                    "cancel_dialog"
                                                )
                                            } else {
                                                cancelAppointment(appointmentId, "")
                                            }
                                        }

                                        userAcceptButton?.setOnClickListener {
                                            userAcceptAppointment(appointmentId)
                                        }
                                    }
                                }
                                if (body != null) {
                                    service?.text = body.services
                                    plateNo?.text = body.vehicle
                                    if (body.description != "")
                                        desc?.text = body.description
                                    else
                                        desc?.text = "-"
                                    date?.text = body.date
                                    startTime?.text = body.startTime
                                    quotation?.text = body.quotedPrice.toString()
                                    if (body.duration != 0 && body.endTime != "") {
                                        duration?.text = body.duration.toString()
                                        endTime?.text = body.endTime
                                    } else {
                                        duration?.visibility = View.GONE
                                        endTime?.visibility = View.GONE
                                        val durationText =
                                            requireActivity().findViewById<TextView>(R.id.appointment_duration_text)
                                        durationText.visibility = View.GONE
                                        val endTimeText =
                                            requireActivity().findViewById<TextView>(R.id.appointment_end_time_text)
                                        endTimeText.visibility = View.INVISIBLE
                                        val imageView: ImageView? =
                                            requireActivity().findViewById(R.id.imageView17)
                                        imageView?.visibility = View.GONE
                                    }
                                    if (it.data?.get("userType") == "user") {
                                        FirestoreClass().fetchWorkshopInfo(body.workshopId)
                                            .addOnSuccessListener { data ->
                                                if (userProfile != null) {
                                                    Glide.with(requireContext())
                                                        .load(data.get("imageLink").toString())
                                                        .into(userProfile)
                                                }
                                            }
                                    } else {
                                        FirestoreClass().fetchUserByID(body.clientId)
                                            .addOnSuccessListener { data ->
                                                if (userProfile != null) {
                                                    Glide.with(requireContext())
                                                        .load(data.get("imageLink").toString())
                                                        .into(userProfile)
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    } else {
                        //error
                    }
                }
            }
        }
    }

    private fun selectDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            200
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 200 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            requireContext().contentResolver.takePersistableUriPermission(
                data?.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val cursor =
                requireContext().contentResolver.query(data.data!!, null, null, null, null)
            cursor?.moveToFirst()
            val fileName =
                cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    .toString()
            cursor?.close()
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "attachment_document",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()
            editor.putString("attachment_document", data.data!!.toString())
            editor.apply()

            uploadDocumentToFirebase(fileName)
        }
    }

    private fun uploadDocumentToFirebase(filename: String) {
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(
                "attachment_document",
                Context.MODE_PRIVATE
            )
        val imageUri: String? = sharedPreferences.getString("attachment_document", "")
        val refStorage = FirebaseStorage.getInstance().reference.child("chat/$filename")

        if (imageUri != null) {
            refStorage.putFile(imageUri.toUri())
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val url = uri.toString()
                        val preferences: SharedPreferences =
                            requireContext().getSharedPreferences(
                                "uploaded_attachment",
                                Context.MODE_PRIVATE
                            )
                        val editor = preferences.edit()
                        editor.putString("uploaded_attachment", url)
                        editor.apply()
                        val attachmentButton: MaterialButton? =
                            requireActivity().findViewById(R.id.attachment_btn)
                        attachmentButton?.setIconResource(R.drawable.document)
                        attachmentButton?.text = FirebaseStorage.getInstance().getReferenceFromUrl(
                            uri.toString()
                        ).name
                        attachmentButton?.setOnClickListener {
                            val pdfIntent = Intent(Intent.ACTION_VIEW)
                            pdfIntent.setDataAndType(
                                uri,
                                "application/pdf"
                            )
                            pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            try {
                                requireContext().startActivity(pdfIntent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    requireContext(),
                                    "No Application available to view PDF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        Toast.makeText(
                            requireContext(),
                            "Document added. Please press save button.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Document could not be sent.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun loadVehicle(vehicleId: Int) {
        if (view != null) {
            var vehicle = Vehicles()
            viewModel.getVehicleById(vehicleId)
            viewModel.vehicle.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        vehicle = Vehicles(
                            body.id,
                            body.plateNo,
                            body.manufacturer,
                            body.model,
                            body.purchaseYear,
                            body.currentMileage,
                            body.imageLink,
                            body.clientId
                        )
                    }
                    val vehicleImage: ImageView? =
                        requireActivity().findViewById(R.id.appointment_vehicle_profile)
                    val vehicleManufacturer: TextView? =
                        requireActivity().findViewById(R.id.appointment_vehicle_manufacturer_model)
                    if (vehicleImage != null) {
                        Glide.with(requireContext()).load(vehicle.imageLink).into(vehicleImage)
                    }
                    ("${vehicle.manufacturer}, ${vehicle.model}").also {
                        vehicleManufacturer?.text = it
                    }
                }
            }
        }
    }

    private fun loadAppointment(appointmentId: Int, duration: Int?): Appointments {
        var appointment = Appointments()
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getAppointmentById(appointmentId)
            viewModel.appointment.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val df = SimpleDateFormat("hh:mm aa")
                    if (body != null) {
                        val start = df.parse(body.startTime)
                        val cal = Calendar.getInstance()
                        if (start != null && duration != 0 && duration != null) {
                            cal.time = start
                            cal.add(Calendar.MINUTE, duration)
                            var endTime = ""
                            val sdf = SimpleDateFormat("hh:mm a")
                            endTime = sdf.format(cal.time)
                            appointment = Appointments(
                                body.id,
                                body.services,
                                body.serviceId,
                                body.date,
                                body.startTime,
                                duration,
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
                                "accepted",
                                body.attachment,
                                body.remarks,
                                body.quotedPrice,
                                body.bookDate
                            )
                        } else {
                            appointment = Appointments(
                                body.id,
                                body.services,
                                body.serviceId,
                                body.date,
                                body.startTime,
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
                                "accepted",
                                body.attachment,
                                body.remarks,
                                body.quotedPrice,
                                body.bookDate
                            )
                        }
                    }
                }
            }
        }
        return appointment
    }

    private fun userAcceptAppointment(appointmentId: Int) {
        val appointment = loadAppointment(appointmentId, null)
        val apiInterface =
            ApiInterface.create().putAppointments(appointment, appointmentId)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Toast.makeText(
                    requireContext(),
                    "You had accepted the appointment!",
                    Toast.LENGTH_SHORT
                ).show()
                OneSignalNotificationService().createAppointmentNotification(
                    appointment.workshopId,
                    "${appointment.clientName} had accepted your appointment rescheduling request",
                    "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                )
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Appointment could not be accepted.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun acceptAppointment(appointmentId: Int, hour: Int, minute: Int) {
        val duration = hour * 60 + minute
        val appointment = loadAppointment(appointmentId, duration)
        val apiInterface =
            ApiInterface.create().putAppointments(appointment, appointmentId)
        Log.d("why", appointment.toString())
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Toast.makeText(
                    requireContext(),
                    "You had accepted the appointment!",
                    Toast.LENGTH_SHORT
                ).show()
                OneSignalNotificationService().createAppointmentNotification(
                    appointment.clientId,
                    "${appointment.workshopName} had accepted your appointment request",
                    "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                )
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Appointment could not be accepted.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun rejectAppointment(appointmentId: Int, reason: String) {
        val appointment = loadAppointment(appointmentId, null)
        appointment.appointmentStatus = "rejected"
        val apiInterface =
            ApiInterface.create().putAppointments(appointment, appointmentId)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Toast.makeText(
                    requireContext(),
                    "You had rejected the appointment!",
                    Toast.LENGTH_SHORT
                ).show()
                OneSignalNotificationService().createAppointmentNotification(
                    appointment.clientId,
                    "${appointment.workshopName} had rejected your appointment request (appointment ID: $appointmentId)",
                    "Message from ${appointment.workshopName}: $reason"
                )
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Appointment could not be accepted.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }

    private fun cancelAppointment(appointmentId: Int, reason: String) {
        val appointment = loadAppointment(appointmentId, null)
        appointment.appointmentStatus = "cancelled"
        val apiInterface =
            ApiInterface.create().putAppointments(appointment, appointmentId)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Toast.makeText(
                    requireContext(),
                    "You had cancelled the appointment!",
                    Toast.LENGTH_SHORT
                ).show()
                OneSignalNotificationService().createAppointmentNotification(
                    appointment.workshopId,
                    "${appointment.clientName} had cancelled appointment (appointment ID: $appointmentId)",
                    "Message from ${appointment.clientName}: $reason"
                )
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Appointment could not be accepted.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        })
    }
}
