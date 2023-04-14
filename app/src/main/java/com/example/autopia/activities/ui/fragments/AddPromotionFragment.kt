package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Customers
import com.example.autopia.activities.model.Promotions
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class AddPromotionFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_add_promotion, container, false)

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val promotionId = arguments?.getInt("promotion_id")
        val addPromotionBtn: Button? = requireActivity().findViewById(R.id.add_promotion_btn)
        val user = FirebaseAuth.getInstance().currentUser
        val startDate: EditText? = requireActivity().findViewById(R.id.start_date)
        val endDate: EditText? = requireActivity().findViewById(R.id.end_date)

        startDate?.setOnClickListener { openDatePickerDialog("start") }
        endDate?.setOnClickListener { openDatePickerDialog("end") }

        addPromotionBtn?.setOnClickListener {
            val isValid: Boolean = validateInputFields()
            if (user != null && isValid) {
                addPromotion(user.uid)
            } else {
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val promotionImage: ImageView? = requireActivity().findViewById(R.id.upload_promotion_image)
        promotionImage?.setOnClickListener {
            selectImage()
        }

        val uploadButton: MaterialButton? =
            requireActivity().findViewById(R.id.upload_promotion_image_button)
        uploadButton?.setOnClickListener {
            selectImage()
        }

        arguments?.remove("promotion_id")
    }

    @SuppressLint("SimpleDateFormat")
    private fun openDatePickerDialog(type: String) {
        val startDate: EditText? = requireActivity().findViewById(R.id.start_date)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val point: Long = if (startDate?.text.toString() != "") {
            formatter.parse(startDate?.text.toString())!!.toInstant().toEpochMilli()
        } else {
            0
        }
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(
                DateValidatorPointForward.from(
                    point + 24 * 60 * 60 * 1000
                )
            )
        val builder = if (type == "end") {
            MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraintsBuilder.build())
                .setTitleText("Select Promotion Date")
        } else {
            MaterialDatePicker.Builder.datePicker().setTitleText("Select Promotion Date")
        }
        val picker = builder.build()
        picker.show(requireActivity().supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { datePicked ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = datePicked
            val format: SimpleDateFormat
            SimpleDateFormat("yyyy-MM-dd").also { format = it }
            val formatted: String = format.format(calendar.time)

            val date: EditText? = if (type == "start")
                requireActivity().findViewById(R.id.start_date)
            else
                requireActivity().findViewById(R.id.end_date)
            date?.setText(formatted)

            val preferences: SharedPreferences =
                requireContext().getSharedPreferences("date", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("date", formatted)
            editor.apply()
        }
    }

    private fun validateInputFields(): Boolean {
        val name: TextView? = requireActivity().findViewById(R.id.add_promotion_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_promotion_description)
        val start: TextView? = requireActivity().findViewById(R.id.start_date)
        val end: TextView? = requireActivity().findViewById(R.id.end_date)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val startText: String = start?.text.toString().trim { it <= ' ' }
        val endText: String = end?.text.toString().trim { it <= ' ' }

        return !(nameText.isEmpty() || descriptionText.isEmpty() || startText.isEmpty() || endText.isEmpty())
    }

//    private fun loadData(promotion_id: Int) {
//        val repository = Repository()
//        val viewModelFactory = ApiViewModelFactory(repository)
//        val progressDialog = ProgressDialog(requireActivity())
//        progressDialog.startLoading()
//        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
//        viewModel.getPromotionById(promotion_id)
//        viewModel.promotion.observe(viewLifecycleOwner) { response ->
//            if (response.isSuccessful) {
//                val body = response.body()
//                val name: TextView = requireActivity().findViewById(R.id.add_promotion_name)
//                name.text = body?.title
//                val description: TextView =
//                    requireActivity().findViewById(R.id.add_promotion_description)
//                description.text = body?.description
//                if (response.body()?.imageLink != null && response.body()!!.imageLink != "") {
//                    val uploadButton: MaterialButton =
//                        requireActivity().findViewById(R.id.upload_promotion_image_button)
//                    uploadButton.isVisible = false
//                    val image: ImageView =
//                        requireActivity().findViewById(R.id.upload_promotion_image)
//                    Glide.with(requireContext()).load(response.body()!!.imageLink)
//                        .into(image)
//                }
//                progressDialog.dismissLoading()
//            }
//        }
//    }

    private fun addPromotion(user_id: String) {
        val name: TextView? = requireActivity().findViewById(R.id.add_promotion_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_promotion_description)
        val start: TextView? = requireActivity().findViewById(R.id.start_date)
        val end: TextView? = requireActivity().findViewById(R.id.end_date)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val startText: String = start?.text.toString().trim { it <= ' ' }
        val endText: String = end?.text.toString().trim { it <= ' ' }

        val dateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        val formattedDateTime = dateTime.format(formatter)

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_promotion_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_promotion_image", "")
        val promotion = Promotions(
            null,
            user_id,
            nameText,
            descriptionText,
            formattedDateTime,
            startText,
            endText,
            imageUri
        )

        var body: List<Customers>
        val users: MutableList<String> = mutableListOf()
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getActiveCustomersByWorkshopId(user_id)
            viewModel.customers.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    body = response.body()!!
                    body.forEach { customer ->
                        users.add(customer.clientId)
                    }
                    val apiInterface = ApiInterface.create().postPromotions(promotion)
                    apiInterface.enqueue(object : Callback<Promotions> {
                        override fun onResponse(
                            call: Call<Promotions>,
                            response: Response<Promotions>,
                        ) {
                            OneSignalNotificationService().createPromotionNotification(
                                users.distinct(),
                                nameText,
                                descriptionText,
                                "$imageUri.jpg"
                            )
                            Toast.makeText(
                                context,
                                "A new promotion had been broadcast successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            val navController =
                                requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                            navController.popBackStack()
                        }

                        override fun onFailure(call: Call<Promotions>, t: Throwable) {
                            Toast.makeText(
                                context,
                                "Error. Promotion could not be added. " + t.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    val apiInterface = ApiInterface.create().postPromotions(promotion)
                    apiInterface.enqueue(object : Callback<Promotions> {
                        override fun onResponse(
                            call: Call<Promotions>,
                            response: Response<Promotions>,
                        ) {
                            Toast.makeText(
                                context,
                                "A new promotion had been added successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            val navController =
                                requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                            navController.popBackStack()
                        }

                        override fun onFailure(call: Call<Promotions>, t: Throwable) {
                            Toast.makeText(
                                context,
                                "Error. Promotion could not be added. " + t.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }
        }
    }

//    private fun editPromotion(promotion_id: Int, user_id: String) {
//        val name: TextView = requireActivity().findViewById(R.id.add_promotion_name)
//        val description: TextView = requireActivity().findViewById(R.id.add_promotion_description)
//        val nameText: String = name.text.toString().trim { it <= ' ' }
//        val descriptionText: String = description.text.toString().trim { it <= ' ' }
//
//        val dateTime = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
//        val formattedDateTime = dateTime.format(formatter)
//
//        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
//            "uploaded_promotion_image",
//            Context.MODE_PRIVATE
//        )
//        val imageUri: String? = sharedPreferences.getString("uploaded_promotion_image", "")
//        val promotion: Promotions
//        val promotionImage: ImageView =
//            requireActivity().findViewById(R.id.upload_promotion_image)
//        if (promotionImage.drawable != null) {
//            promotion = Promotions(
//                promotion_id,
//                user_id,
//                nameText,
//                descriptionText,
//                formattedDateTime,
//                imageUri
//            )
//        } else {
//            promotion = Promotions(
//                promotion_id,
//                user_id,
//                nameText,
//                descriptionText,
//                formattedDateTime,
//                null
//            )
//        }
//        val apiInterface = ApiInterface.create().putPromotions(promotion, promotion_id)
//        apiInterface.enqueue(object : Callback<Promotions> {
//            override fun onResponse(
//                call: Call<Promotions>,
//                response: Response<Promotions>,
//            ) {
//                val navController =
//                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
//                navController.popBackStack()
//                Toast.makeText(context, "Promotion is updated!", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onFailure(call: Call<Promotions>, t: Throwable) {
//                Toast.makeText(context, "Promotion could not be updated.", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

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
            val promotionImage: ImageView? =
                requireActivity().findViewById(R.id.upload_promotion_image)
            val uploadButton: MaterialButton? =
                requireActivity().findViewById(R.id.upload_promotion_image_button)
            uploadButton?.isVisible = false
            promotionImage?.setImageURI(imageUrl)
            uploadImageToFirebase(imageUrl)
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "promotion_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("promotion_image", imageUrl.toString())
            editor.apply()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("promotion/$fileName")

        refStorage.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "uploaded_promotion_image",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()

                    editor.putString("uploaded_promotion_image", it.toString())
                    editor.apply()
                }
            }
            .addOnFailureListener { e ->
            }
    }
}