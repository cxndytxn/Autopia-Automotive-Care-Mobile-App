package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Services
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddServiceFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_add_service, container, false)

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceId = arguments?.getInt("service_id")
        val addServiceBtn: Button? = requireActivity().findViewById(R.id.add_service_btn)
        val user = FirebaseAuth.getInstance().currentUser

        if (serviceId != null) {
            loadData(serviceId)
            "Edit Service".also { addServiceBtn?.text = it }

            addServiceBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                val isValidFormat: Boolean = validateInputFormat()
                if (isValid) {
                    if (isValidFormat) {
                        user?.uid?.let { it1 -> editService(serviceId, it1) }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure quotation is in numerical format.",
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
            addServiceBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                val isValidFormat: Boolean = validateInputFormat()
                if (user != null && isValid) {
                    if (isValidFormat) {
                        addService(user.uid)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure quotation is in numerical format.",
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

        val serviceImage: ImageView? = requireActivity().findViewById(R.id.upload_service_image)
        serviceImage?.setOnClickListener {
            selectImage()
        }

        val uploadButton: MaterialButton? =
            requireActivity().findViewById(R.id.upload_service_image_button)
        uploadButton?.setOnClickListener {
            selectImage()
        }

        arguments?.remove("service_id")
    }

    private fun validateInputFormat(): Boolean {
        val quotation: TextView? = requireActivity().findViewById(R.id.add_service_quotation)
        val quotationText: String = quotation?.text.toString()
        return (quotationText.matches("-?\\d+(\\.\\d+)?".toRegex()))
    }

    private fun validateInputFields(): Boolean {
        val name: TextView? = requireActivity().findViewById(R.id.add_service_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_service_description)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }

        return !(nameText.isEmpty() || descriptionText.isEmpty())
    }

    private fun loadData(service_id: Int) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getServiceById(service_id)
            viewModel.service.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val name: TextView? = requireActivity().findViewById(R.id.add_service_name)
                    name?.text = body?.name
                    if (body?.name == "Inspection") {
                        name?.isEnabled = false
                    }
                    val description: TextView? =
                        requireActivity().findViewById(R.id.add_service_description)
                    description?.text = body?.description
                    val quotation: TextView? =
                        requireActivity().findViewById(R.id.add_service_quotation)
                    quotation?.text = body?.quotation.toString()
                    if (response.body()?.imageLink != null && response.body()!!.imageLink != "") {
                        val uploadButton: MaterialButton? =
                            requireActivity().findViewById(R.id.upload_service_image_button)
                        uploadButton?.isVisible = false
                        val vehicleImage: ImageView? =
                            requireActivity().findViewById(R.id.upload_service_image)
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

    private fun addService(user_id: String) {
        val name: TextView? = requireActivity().findViewById(R.id.add_service_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_service_description)
        val quotation: TextView? = requireActivity().findViewById(R.id.add_service_quotation)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val quotedPrice: Double = quotation?.text.toString().toDouble()

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_service_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_service_image", "")
        val service = Services(
            null,
            user_id,
            nameText,
            descriptionText,
            quotedPrice.toString().toDouble(),
            imageUri
        )
        val apiInterface = ApiInterface.create().postServices(service)
        apiInterface.enqueue(object : Callback<Services> {
            override fun onResponse(
                call: Call<Services>,
                response: Response<Services>,
            ) {
                Toast.makeText(context, "Successfully added a new service!", Toast.LENGTH_SHORT)
                    .show()
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.popBackStack()
            }

            override fun onFailure(call: Call<Services>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error. Service could not be added. " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun editService(service_id: Int, user_id: String) {
        val name: TextView? = requireActivity().findViewById(R.id.add_service_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_service_description)
        val quotation: TextView? = requireActivity().findViewById(R.id.add_service_quotation)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val quotedPrice: Double = quotation?.text.toString().toDouble()

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_service_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_service_image", "")
        val service: Services
        val serviceImage: ImageView? =
            requireActivity().findViewById(R.id.upload_service_image)
        if (serviceImage?.drawable != null) {
            service = Services(
                service_id,
                user_id,
                nameText,
                descriptionText,
                quotedPrice,
                imageUri
            )
        } else {
            service = Services(
                service_id,
                user_id,
                nameText,
                descriptionText,
                quotedPrice,
                null
            )
        }
        val apiInterface = ApiInterface.create().putServices(service, service_id)
        apiInterface.enqueue(object : Callback<Services> {
            override fun onResponse(
                call: Call<Services>,
                response: Response<Services>,
            ) {
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.popBackStack()
                Toast.makeText(context, "Service is updated!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Services>, t: Throwable) {
                Toast.makeText(context, "Service could not be updated.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onStop() {
        super.onStop()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            imageUrl = data?.data!!
            val serviceImage: ImageView? = requireActivity().findViewById(R.id.upload_service_image)
            val uploadButton: MaterialButton? =
                requireActivity().findViewById(R.id.upload_service_image_button)
            uploadButton?.isVisible = false
            serviceImage?.setImageURI(imageUrl)
            uploadImageToFirebase(imageUrl)
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "service_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("service_image", imageUrl.toString())
            editor.apply()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("service/$fileName")

        refStorage.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "uploaded_service_image",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()

                    editor.putString("uploaded_service_image", it.toString())
                    editor.apply()
                }
            }
            .addOnFailureListener { e ->
            }
    }
}