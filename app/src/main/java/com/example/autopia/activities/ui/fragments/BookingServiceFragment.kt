package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookingServiceFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    private var vehicleId: Int = 0
    private var vehicleImageLink: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_booking_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val workshopId = arguments?.getString("workshop_id")

        FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnCompleteListener {
            val workshopName: TextView? = requireActivity().findViewById(R.id.booking_sp_name)
            workshopName?.text = it.result.data?.get("workshopName").toString()

            val description: TextView? = requireActivity().findViewById(R.id.booking_sp_details)
            description?.text = it.result.data?.get("description").toString()
        }

        val user = FirebaseAuth.getInstance().currentUser

        val bookingVehicle: EditText? = requireActivity().findViewById(R.id.booking_vehicle)
        bookingVehicle?.setOnClickListener {
            val bundle = bundleOf("userId" to user!!.uid)
            findNavController().navigate(R.id.viewVehiclesFragment, bundle)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        val bookingButton: Button? = requireActivity().findViewById(R.id.booking_service_button)

        bookingButton?.setOnClickListener {
            saveAppointment()
        }
    }

    override fun onResume() {
        super.onResume()
        loadVehicleData()
        val bookingButton: Button? = requireActivity().findViewById(R.id.booking_service_button)

        bookingButton?.setOnClickListener {
            saveAppointment()
        }

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("vehicle_plate")
            ?.observe(viewLifecycleOwner) { it ->
                val bookingVehicle: EditText? = requireActivity().findViewById(R.id.booking_vehicle)
                bookingVehicle?.setText(it)
            }
    }

    private fun saveAppointment() {
        val isValid: Boolean = validateInputFields()
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("vehicle_id")
            ?.observe(viewLifecycleOwner) { it ->
                vehicleId = it
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("vehicle_image_link")
            ?.observe(viewLifecycleOwner) { it ->
                vehicleImageLink = it
                Log.d("vehicle image", vehicleImageLink)
            }
        if (isValid) {
            val workshopId = arguments?.getString("workshop_id")
            val user = FirebaseAuth.getInstance().currentUser

            val workshopName: TextView? = requireActivity().findViewById(R.id.booking_sp_name)
            val clientName: TextView? = requireActivity().findViewById(R.id.booking_username)
            val phoneNo: TextView? = requireActivity().findViewById(R.id.booking_phone_no)
            val description: TextView? = requireActivity().findViewById(R.id.booking_description)
            val vehicle: EditText? = requireActivity().findViewById(R.id.booking_vehicle)
            FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnSuccessListener {
                val appointment = Appointments(
                    null,
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    "#999FFF",
                    vehicle?.text.toString(),
                    vehicleId,
                    phoneNo?.text.toString(),
                    it.get("contactNumber").toString(),
                    description?.text.toString(),
                    workshopId,
                    workshopName?.text.toString(),
                    user!!.uid,
                    clientName?.text.toString(),
                    "active",
                    "",
                    "",
                    null,
                    ""
                )

                val gson = Gson()
                val json = gson.toJson(appointment)

                val preferences: SharedPreferences =
                    requireContext().getSharedPreferences(
                        "appointment_details",
                        Context.MODE_PRIVATE
                    )
                val editor = preferences.edit()

                editor.putString("appointment_details", json)
                editor.apply()

                val bundle = bundleOf("workshop_id" to workshopId)
                Navigation.findNavController(requireView())
                    .navigate(R.id.bookingDateTimeFragment, bundle)
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please ensure no fields are empty.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun loadVehicleData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val vehicleList: ArrayList<String> = ArrayList()
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            if (uid != null) {
                viewModel.getVehiclesByClientId(uid)
            }
            viewModel.vehicles.observe(viewLifecycleOwner) { response ->
                //pass list here
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    //val services: Spinner = requireActivity().findViewById(R.id.booking_vehicle)
                    for (item in response.body()!!) {
                        vehicleList.add(item.plateNo)
                    }
//                val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(),
//                    R.layout.support_simple_spinner_dropdown_item,
//                    vehicleList);
//                services.adapter = adapter; // this will set list of values to spinner
                }
                progressDialog.dismissLoading()
                if (vehicleList.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Please add vehicle info before booking appointment.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val bookingButton: Button? =
                        requireActivity().findViewById(R.id.booking_service_button)
                    bookingButton?.isEnabled = false
                    bookingButton?.background?.setTint(Color.GRAY)
                    val clientName: EditText? =
                        requireActivity().findViewById(R.id.booking_username)
                    val phoneNo: EditText? = requireActivity().findViewById(R.id.booking_phone_no)
                    val description: EditText? =
                        requireActivity().findViewById(R.id.booking_description)
                    val vehicle: EditText? = requireActivity().findViewById(R.id.booking_vehicle)
                    clientName?.isEnabled = false
                    phoneNo?.isEnabled = false
                    description?.isEnabled = false
                    vehicle?.isEnabled = false
                } else {
                    val bookingButton: Button? =
                        requireActivity().findViewById(R.id.booking_service_button)
                    bookingButton?.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.indigo_300)
                }
            }
        }
    }

    private fun validateInputFields(): Boolean {

        val username: TextView? = requireActivity().findViewById(R.id.booking_username)
        val phoneNo: TextView? = requireActivity().findViewById(R.id.booking_phone_no)
        val usernameText: String = username?.text.toString().trim { it <= ' ' }
        val phoneNoText: String = phoneNo?.text.toString().trim { it <= ' ' }

        return !(usernameText.isEmpty() || phoneNoText.isEmpty())
    }
}