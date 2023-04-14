package com.example.autopia.activities.ui.fragments

import android.content.*
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.ServiceReminders
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProgressDialog
import com.example.autopia.activities.utils.ProposeDurationDialog
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AppointmentInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (isAdded) {
            val bottomNavigationView: BottomNavigationView? =
                requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
            bottomNavigationView?.isVisible = false

            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }

        return inflater.inflate(R.layout.fragment_appointment_info, container, false)
    }

    override fun onResume() {
        super.onResume()
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("service_name")
            ?.observe(viewLifecycleOwner) { it ->
                val nextService: EditText? = requireActivity().findViewById(R.id.next_service)
                nextService?.setText(it)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appointmentId = arguments?.getInt("appointment_id", 0)
        val clientId = arguments?.getString("client_id", "")

        val nextService: EditText? = requireActivity().findViewById(R.id.next_service)
        val nextServiceDate: EditText? = requireActivity().findViewById(R.id.next_service_date)
        val nextServiceMileage: EditText? = requireActivity().findViewById(R.id.next_service_mileage)
        val doneButton: Button? = requireActivity().findViewById(R.id.next_done_button)
        val duration: TextView? = requireActivity().findViewById(R.id.next_service_duration)

        nextServiceDate?.setOnClickListener { openDatePickerDialog() }
        duration?.setOnClickListener {
            val dialog = ProposeDurationDialog()
            dialog.setOnTimeSetOption("Set Time") { hour, minute ->
                val selectedDuration = hour * 60 + minute
                (selectedDuration.toString()).also { duration.text = it }
            }
            dialog.setOnCancelOption("Cancel") {
                dialog.dismiss()
            }
            dialog.show(
                (context as AppCompatActivity).supportFragmentManager,
                "dialog"
            )
        }

        val user = FirebaseAuth.getInstance().currentUser
        nextService?.setOnClickListener {
            val bundle = bundleOf("workshopId" to user?.uid)
            findNavController().navigate(R.id.viewServicesFragment, bundle)
        }

        doneButton?.setOnClickListener {
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            val isValid = validateInputFields()
            var serviceId = 0
            val navController = findNavController()
            navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("service_id")
                ?.observe(viewLifecycleOwner) { id ->
                    serviceId = id
                }
            if (isValid) {
                Log.d("Why", clientId!!)
                val serviceReminder = ServiceReminders(
                    null,
                    nextService?.text.toString(),
                    serviceId,
                    nextServiceDate?.text.toString(),
                    duration?.text.toString().toInt(),
                    appointmentId!!,
                    user?.uid!!,
                    clientId,
                    "",
                    "",
                    nextServiceMileage?.text.toString().toInt()
                )
                val apiInterface = ApiInterface.create().postServiceReminders(serviceReminder)
                apiInterface.enqueue(object : Callback<ServiceReminders> {
                    override fun onResponse(
                        call: Call<ServiceReminders>,
                        response: Response<ServiceReminders>,
                    ) {
                        OneSignalNotificationService().createAppointmentNotification(
                            clientId,
                            "Your workshop set a service reminder!",
                            "Next Service Date: ${nextServiceDate?.text}\nService needed: ${nextService?.text}\nMileage: ${nextServiceMileage?.text}"
                        )
                        progressDialog.dismissLoading()
                        Toast.makeText(
                            requireContext(),
                            "Service reminder had been saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Thread.sleep(2000)
                        navController.popBackStack()
                    }

                    override fun onFailure(call: Call<ServiceReminders>, t: Throwable) {
                        Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                progressDialog.dismissLoading()
                Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputFields(): Boolean {
        val nextService: EditText? = requireActivity().findViewById(R.id.next_service)
        val nextServiceDate: EditText? = requireActivity().findViewById(R.id.next_service_date)
        val nextServiceMileage: EditText? = requireActivity().findViewById(R.id.next_service_mileage)
        val nextServiceDuration: EditText? =
            requireActivity().findViewById(R.id.next_service_duration)
        val nextServiceText: String = nextService?.text.toString().trim { it <= ' ' }
        val nextServiceDateText: String = nextServiceDate?.text.toString().trim { it <= ' ' }
        val nextServiceMileageText: String = nextServiceMileage?.text.toString().trim { it <= ' ' }
        val nextServiceDurationText: String = nextServiceDuration?.text.toString().trim { it <= ' ' }

        return !(nextServiceText.isEmpty() || nextServiceMileageText.isEmpty() || nextServiceDateText.isEmpty() || nextServiceDurationText.isEmpty())
    }

//    private fun selectDocument() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        intent.type = "application/pdf"
//        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//
//        startActivityForResult(
//            Intent.createChooser(
//                intent,
//                "Please select..."
//            ),
//            200
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 200 && resultCode != AppCompatActivity.RESULT_CANCELED) {
//            val cursor =
//                requireContext().contentResolver.query(data?.data!!, null, null, null, null)
//            cursor?.moveToFirst()
//            val fileName =
//                cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
//                    .toString()
//            cursor?.close()
//            uploadDocumentToFirebase(fileName)
//            val preferences: SharedPreferences =
//                requireContext().getSharedPreferences(
//                    "document",
//                    Context.MODE_PRIVATE
//                )
//            val editor = preferences.edit()
//            editor.putString("document", data.data!!.toString())
//            editor.apply()
//        }
//    }
//
//    private fun uploadDocumentToFirebase(filename: String) {
//        val sharedPreferences: SharedPreferences =
//            requireContext().getSharedPreferences(
//                "document",
//                Context.MODE_PRIVATE
//            )
//        val imageUri: String? = sharedPreferences.getString("document", "")
//        val refStorage = FirebaseStorage.getInstance().reference.child("serviceReminder/$filename")
//
//        if (imageUri != null) {
//            refStorage.putFile(imageUri.toUri())
//                .addOnSuccessListener { taskSnapshot ->
//                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
//                        val url = uri.toString()
//                        val preferences: SharedPreferences =
//                            requireContext().getSharedPreferences(
//                                "uploaded_doc",
//                                Context.MODE_PRIVATE
//                            )
//                        val editor = preferences.edit()
//                        editor.putString("uploaded_doc", url)
//                        editor.apply()
//                        val docBtn: MaterialButton = requireActivity().findViewById(R.id.doc_button)
//                        docBtn.visibility = View.VISIBLE
//                        docBtn.text = filename
//                        docBtn.setOnClickListener {
//                            val pdfIntent = Intent(Intent.ACTION_VIEW)
//                            pdfIntent.setDataAndType(
//                                uri,
//                                "application/pdf"
//                            )
//                            pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            try {
//                                requireContext().startActivity(pdfIntent)
//                            } catch (e: ActivityNotFoundException) {
//                                Toast.makeText(
//                                    context,
//                                    "No Application available to view PDF",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                        val attachmentBtn: MaterialButton =
//                            requireActivity().findViewById(R.id.next_attachment_button)
//                        attachmentBtn.visibility = View.GONE
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(
//                        context,
//                        "Document could not be sent",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                }
//        }
//    }

    private fun openDatePickerDialog() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
        val builder =
            MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraintsBuilder.build())
                .setTitleText("Select Next Appointment Date")
        val picker = builder.build()
        picker.show(requireActivity().supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { datePicked ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = datePicked
            val format: SimpleDateFormat
            SimpleDateFormat("yyyy-MM-dd").also { format = it }
            val formatted: String = format.format(calendar.time)

            val date: EditText? = requireActivity().findViewById(R.id.next_service_date)
            date?.setText(formatted)

            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("date", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("date", formatted)
            editor.apply()
        }
    }
}