package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Vehicles
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddVehicleFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_add_vehicle, container, false)

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vehicleId = arguments?.getInt("vehicle_id")
        val addVehicleBtn: Button? = requireActivity().findViewById(R.id.add_vehicle_btn)
        val user = FirebaseAuth.getInstance().currentUser

        if (vehicleId != null) {
            loadData(vehicleId)
            "Edit Vehicle".also { addVehicleBtn?.text = it }

            addVehicleBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                val isCorrectFormat: Boolean = validateInputs()
                if (isValid) {
                    if (isCorrectFormat) {
                        user?.uid?.let { it1 -> editVehicle(vehicleId, it1) }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure year or mileage is in numerical format.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireActivity().applicationContext,
                        "Please ensure no fields are empty.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            addVehicleBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                if (user != null && isValid) {
                    val isCorrectFormat: Boolean = validateInputs()
                    if (isCorrectFormat) {
                        addVehicle(user.uid)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure year or mileage is in numerical format.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireActivity().applicationContext,
                        "Please ensure no fields are empty.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val vehicleImage: ImageView? = requireActivity().findViewById(R.id.upload_vehicle_image)
        vehicleImage?.setOnClickListener {
            selectImage()
        }

        val uploadButton: MaterialButton? =
            requireActivity().findViewById(R.id.upload_vehicle_image_button)
        uploadButton?.setOnClickListener {
            selectImage()
        }

        arguments?.remove("vehicle_id")
    }

    private fun validateInputs(): Boolean {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        val purchaseYear: TextView? = requireActivity().findViewById(R.id.add_vehicle_year)
        val purchaseYearText: String = purchaseYear?.text.toString().trim { it <= ' ' }
        val mileage: TextView? = requireActivity().findViewById(R.id.add_vehicle_mileage)
        val mileageText: String = mileage?.text.toString().trim { it <= ' ' }

        if (mileageText != "") {
            return (regex.matches(purchaseYearText) && regex.matches(mileageText))
        } else {
            return (regex.matches(purchaseYearText))
        }
    }

    private fun validateInputFields(): Boolean {
        val plateNo: TextView? = requireActivity().findViewById(R.id.add_vehicle_plate)
        val manufacturer: TextView? = requireActivity().findViewById(R.id.add_vehicle_manufacturer)
        val model: TextView? = requireActivity().findViewById(R.id.add_vehicle_model)
        val purchaseYear: TextView? = requireActivity().findViewById(R.id.add_vehicle_year)
        val plateNoText: String = plateNo?.text.toString().trim { it <= ' ' }
        val manufacturerText: String = manufacturer?.text.toString().trim { it <= ' ' }
        val modelText: String = model?.text.toString().trim { it <= ' ' }
        val purchaseYearText: String = purchaseYear?.text.toString().trim { it <= ' ' }

        return !(plateNoText.isEmpty() || manufacturerText.isEmpty() || modelText.isEmpty() || purchaseYearText.isEmpty())
    }

    private fun loadData(vehicle_id: Int) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getVehicleById(vehicle_id)
            viewModel.vehicle.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val plateNo: TextView? = requireActivity().findViewById(R.id.add_vehicle_plate)
                    plateNo?.text = body?.plateNo
                    val manufacturer: TextView? =
                        requireActivity().findViewById(R.id.add_vehicle_manufacturer)
                    manufacturer?.text = body?.manufacturer
                    val model: TextView? = requireActivity().findViewById(R.id.add_vehicle_model)
                    model?.text = body?.model
                    val purchaseYear: TextView? =
                        requireActivity().findViewById(R.id.add_vehicle_year)
                    purchaseYear?.text = body?.purchaseYear
                    val currentMileage: TextView? =
                        requireActivity().findViewById(R.id.add_vehicle_mileage)
                    currentMileage?.text = body?.currentMileage
                    if (response.body()?.imageLink != null && response.body()!!.imageLink != "") {
                        val uploadButton: MaterialButton? =
                            requireActivity().findViewById(R.id.upload_vehicle_image_button)
                        uploadButton?.isVisible = false
                        val vehicleImage: ImageView? =
                            requireActivity().findViewById(R.id.upload_vehicle_image)
                        if (vehicleImage != null) {
                            Glide.with(requireContext()).load(response.body()!!.imageLink)
                                .into(vehicleImage)
                        }
                    }
                    progressDialog.dismissLoading()
                }
            }
        }
    }

    private fun addVehicle(user_id: String) {
        val plateNo: TextView? = requireActivity().findViewById(R.id.add_vehicle_plate)
        val manufacturer: TextView? = requireActivity().findViewById(R.id.add_vehicle_manufacturer)
        val model: TextView? = requireActivity().findViewById(R.id.add_vehicle_model)
        val purchaseYear: TextView? = requireActivity().findViewById(R.id.add_vehicle_year)
        val currentMileage: TextView? = requireActivity().findViewById(R.id.add_vehicle_mileage)
        val plateNoText: String = plateNo?.text.toString().trim { it <= ' ' }
        val manufacturerText: String = manufacturer?.text.toString().trim { it <= ' ' }
        val modelText: String = model?.text.toString().trim { it <= ' ' }
        val purchaseYearText: String = purchaseYear?.text.toString().trim { it <= ' ' }
        var currentMileageText: String? = currentMileage?.text.toString().trim { it <= ' ' }
        if (currentMileageText?.isEmpty() == true) {
            currentMileageText = ""
        }

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_vehicle_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_vehicle_image", "")
        val vehicle = Vehicles(
            null,
            plateNoText,
            manufacturerText,
            modelText,
            purchaseYearText,
            currentMileageText,
            imageUri,
            user_id
        )
        val apiInterface = ApiInterface.create().postVehicles(vehicle)
        apiInterface.enqueue(object : Callback<Vehicles> {
            override fun onResponse(
                call: Call<Vehicles>,
                response: Response<Vehicles>,
            ) {
                Toast.makeText(
                    context,
                    "Added a new vehicle successfully!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.popBackStack()
            }

            override fun onFailure(call: Call<Vehicles>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error. Vehicle could not be added. " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun editVehicle(vehicle_id: Int, user_id: String) {
        val plateNo: TextView? = requireActivity().findViewById(R.id.add_vehicle_plate)
        val manufacturer: TextView? = requireActivity().findViewById(R.id.add_vehicle_manufacturer)
        val model: TextView? = requireActivity().findViewById(R.id.add_vehicle_model)
        val purchaseYear: TextView? = requireActivity().findViewById(R.id.add_vehicle_year)
        val currentMileage: TextView? = requireActivity().findViewById(R.id.add_vehicle_mileage)
        val plateNoText: String = plateNo?.text.toString().trim { it <= ' ' }
        val manufacturerText: String = manufacturer?.text.toString().trim { it <= ' ' }
        val modelText: String = model?.text.toString().trim { it <= ' ' }
        val purchaseYearText: String = purchaseYear?.text.toString().trim { it <= ' ' }
        var currentMileageText: String? = currentMileage?.text.toString().trim { it <= ' ' }
        if (currentMileageText?.isEmpty() == true) {
            currentMileageText = null
        }

        val pref: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_vehicle_image",
            Context.MODE_PRIVATE
        )
        val image: String? = pref.getString("uploaded_vehicle_image", "")
        val vehicle: Vehicles
        val vehicleImage: ImageView? =
            requireActivity().findViewById(R.id.upload_vehicle_image)
        if (vehicleImage?.drawable != null) {
            vehicle = Vehicles(
                vehicle_id,
                plateNoText,
                manufacturerText,
                modelText,
                purchaseYearText,
                currentMileageText,
                image,
                user_id
            )
        } else {
            vehicle = Vehicles(
                vehicle_id,
                plateNoText,
                manufacturerText,
                modelText,
                purchaseYearText,
                currentMileageText,
                null,
                user_id
            )
        }
        val apiInterface = ApiInterface.create().putVehicles(vehicle, vehicle_id)
        apiInterface.enqueue(object : Callback<Vehicles> {
            override fun onResponse(
                call: Call<Vehicles>,
                response: Response<Vehicles>,
            ) {
                Toast.makeText(
                    context,
                    "Vehicle is updated!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.popBackStack()
            }

            override fun onFailure(call: Call<Vehicles>, t: Throwable) {
                Toast.makeText(context, "Vehicle could not be updated.", Toast.LENGTH_SHORT)
                    .show()
            }
        })
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

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //intent.type = "images/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            imageUrl = data?.data!!
            val vehicleImage: ImageView? =
                requireActivity().findViewById(R.id.upload_vehicle_image)
            val uploadButton: MaterialButton? =
                requireActivity().findViewById(R.id.upload_vehicle_image_button)
            uploadButton?.isVisible = false
            vehicleImage?.setImageURI(imageUrl)
            uploadImageToFirebase(imageUrl)
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "vehicle_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("vehicle_image", imageUrl.toString())
            editor.apply()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("vehicle/$fileName")

        refStorage.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "uploaded_vehicle_image",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()

                    editor.putString("uploaded_vehicle_image", it.toString())
                    editor.apply()
                }
            }
            .addOnFailureListener { e ->
            }
    }
}