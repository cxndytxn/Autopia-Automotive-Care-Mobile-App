package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.fragment_workshop_info.view.*
import kotlinx.android.synthetic.main.fragment_workshop_profile.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WorkshopProfileFragment : Fragment(), TimePickerDialog.OnTimeSetListener {
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid).get()
                .addOnSuccessListener {
                    val name: TextView? = requireActivity().findViewById(R.id.workshop_profile_name)
                    val email: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_email)
                    val username: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_username)
                    val phoneNo: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_phone)
                    val description: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_description)
                    val address: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_address)
                    val openHours: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_open_hours)
                    val closeHours: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_close_hours)
                    val switch: SwitchCompat? =
                        requireActivity().findViewById(R.id.workshop_profile_switch)
                    val protonCheckbox: CheckBox? =
                        requireActivity().findViewById(R.id.workshop_proton_checkbox)
                    val peroduaCheckbox: CheckBox? =
                        requireActivity().findViewById(R.id.workshop_perodua_checkbox)
                    val toyotaCheckbox: CheckBox? =
                        requireActivity().findViewById(R.id.workshop_toyota_checkbox)
                    val mercedesCheckbox: CheckBox? =
                        requireActivity().findViewById(R.id.workshop_mercedes_benz_checkbox)
                    val lamborghiniCheckbox: CheckBox? =
                        requireActivity().findViewById(R.id.workshop_lamborghini_checkbox)
                    val checkboxes: LinearLayout? =
                        requireActivity().findViewById(R.id.checkboxes_grp)
                    val vehicleBrands: TextView? =
                        requireActivity().findViewById(R.id.workshop_profile_vehicle_brands)

                    email?.isEnabled = false

                    name?.text = it.data?.get("workshopName").toString()
                    email?.text = it.data?.get("email").toString()
                    username?.text = it.data?.get("workshopName").toString()
                    if (it.data?.get("contactNumber") == "" || it.data?.get("contactNumber") == null || it.data?.get(
                            "contactNumber"
                        ) == "null"
                    )
                        phoneNo?.text = ""
                    else
                        phoneNo?.text = it.data?.get("contactNumber").toString()
                    if (it.data?.get("description") == null || it.data?.get("description") == "" || it.data?.get(
                            "description"
                        ) == "null"
                    )
                        description?.text = ""
                    else
                        description?.text = it.data?.get("description").toString()
                    if (it.data?.get("address") == null || it.data?.get("address") == "null" || it.data?.get(
                            "address"
                        ) == ""
                    )
                        address?.text = ""
                    else
                        address?.text = it.data?.get("address").toString()
                    if (it.data?.get("openHours") == null)
                        openHours?.text = ""
                    else
                        openHours?.text = it.data?.get("openHours").toString()
                    if (it.data?.get("closeHours") == null)
                        closeHours?.text = ""
                    else
                        closeHours?.text = it.data?.get("closeHours").toString()
                    if (it.data?.get("vehicleBrands") != null) {
                        val array: ArrayList<String> =
                            it.data?.get("vehicleBrands") as ArrayList<String>
                        switch?.isChecked = array.isNotEmpty()
                        if (switch?.isChecked == true) {
                            vehicleBrands?.visibility = View.VISIBLE
                            checkboxes?.visibility = View.VISIBLE
                            for (brand in array) {
                                if (brand == "Proton")
                                    protonCheckbox?.isChecked = true
                                if (brand == "Perodua")
                                    peroduaCheckbox?.isChecked = true
                                if (brand == "Toyota")
                                    toyotaCheckbox?.isChecked = true
                                if (brand == "Mercedes Benz")
                                    mercedesCheckbox?.isChecked = true
                                if (brand == "Lamborghini")
                                    lamborghiniCheckbox?.isChecked = true
                            }
                        } else {
                            vehicleBrands?.visibility = View.GONE
                            checkboxes?.visibility = View.GONE
                        }
                    } else {
                        vehicleBrands?.visibility = View.GONE
                        checkboxes?.visibility = View.GONE
                    }
                    Glide.with(requireContext()).load(it.data?.get("imageLink"))
                        .into(requireView().workshop_profile_image)
                    Glide.with(requireContext()).load(it.data?.get("cover"))
                        .into(requireView().workshop_profile_cover)
                }
        }

        return inflater.inflate(R.layout.fragment_workshop_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val checkboxes: LinearLayout? =
            requireActivity().findViewById(R.id.checkboxes_grp)
        val switch: SwitchCompat? =
            requireActivity().findViewById(R.id.workshop_profile_switch)
        val vehicleBrands: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_vehicle_brands)

        switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vehicleBrands?.visibility = View.VISIBLE
                checkboxes?.visibility = View.VISIBLE
            } else {
                vehicleBrands?.visibility = View.GONE
                checkboxes?.visibility = View.GONE
            }
        }

        val profileImage: ImageView? = requireActivity().findViewById(R.id.workshop_profile_image)
        profileImage?.setOnClickListener {
            selectImage()
        }

        val coverImage: ImageView? = requireActivity().findViewById(R.id.workshop_profile_cover)
        coverImage?.setOnClickListener {
            selectCover()
        }

        val editBtn: Button? = requireActivity().findViewById(R.id.workshop_profile_edit_button)
        editBtn?.setOnClickListener {
            editProfile()
        }

        val openHours: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_open_hours)
        openHours?.setOnClickListener { openOpenTimePickerDialog() }

        val closeHours: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_close_hours)
        closeHours?.setOnClickListener { openCloseTimePickerDialog() }

        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            appBarLayout?.elevation = 8f
        }
    }

    private fun openOpenTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.newInstance(
            this, calendar.time.hours, calendar.time.minutes, false
        )
        timePicker.version = TimePickerDialog.Version.VERSION_2
        timePicker.show(requireActivity().supportFragmentManager, "OpenTimePickerDialog")
    }

    private fun openCloseTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.newInstance(
            this, calendar.time.hours, calendar.time.minutes, false
        )
        timePicker.version = TimePickerDialog.Version.VERSION_2
        timePicker.show(requireActivity().supportFragmentManager, "CloseTimePickerDialog")
    }

    override fun onTimeSet(
        timePicker: TimePickerDialog?,
        hourOfDay: Int,
        minute: Int,
        second: Int
    ) {
        val formattedTime: String = when {
            hourOfDay > 12 -> {
                if (minute < 10) {
                    "${hourOfDay - 12}:0${minute} PM"
                } else {
                    "${hourOfDay - 12}:${minute} PM"
                }
            }
            hourOfDay == 12 -> {
                if (minute < 10) {
                    "${hourOfDay}:0${minute} PM"
                } else {
                    "${hourOfDay}:${minute} PM"
                }
            }
            hourOfDay == 0 -> {
                if (minute < 10) {
                    "${hourOfDay + 12}:0${minute} AM"
                } else {
                    "${hourOfDay + 12}:${minute} AM"
                }
            }
            else -> {
                if (minute < 10) {
                    "${hourOfDay}:0${minute} AM"
                } else {
                    "${hourOfDay}:${minute} AM"
                }
            }
        }
        val sdf = SimpleDateFormat("h:mm a")
        val sdf2 = SimpleDateFormat("HH:mm")
        val formatted = sdf.parse(formattedTime)
        val formatted2 = sdf2.format(formatted!!)
        if (timePicker?.tag == "OpenTimePickerDialog") {
            val time: EditText? = requireActivity().findViewById(R.id.workshop_profile_open_hours)
            time?.setText(formattedTime)

            val preferences: SharedPreferences =
                requireActivity().getSharedPreferences("open_hours", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("open_hours", formatted2)
            editor.apply()
        } else {
            val time: EditText? = requireActivity().findViewById(R.id.workshop_profile_close_hours)
            time?.setText(formattedTime)

            val preferences: SharedPreferences =
                requireActivity().getSharedPreferences("close_hours", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("close_hours", formatted2)
            editor.apply()
        }
    }

    private fun editProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val username: TextView? = requireActivity().findViewById(R.id.workshop_profile_username)
        val phoneNo: TextView? = requireActivity().findViewById(R.id.workshop_profile_phone)
        val description: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_description)
        val address: TextView? = requireActivity().findViewById(R.id.workshop_profile_address)
        val switch: SwitchCompat? =
            requireActivity().findViewById(R.id.workshop_profile_switch)
        val latLng: LatLng? = getLocationFromAddress(requireContext(), address?.text.toString())
        val protonCheckbox: CheckBox? =
            requireActivity().findViewById(R.id.workshop_proton_checkbox)
        val peroduaCheckbox: CheckBox? =
            requireActivity().findViewById(R.id.workshop_perodua_checkbox)
        val toyotaCheckbox: CheckBox? =
            requireActivity().findViewById(R.id.workshop_toyota_checkbox)
        val mercedesBenzCheckbox: CheckBox? =
            requireActivity().findViewById(R.id.workshop_mercedes_benz_checkbox)
        val lamborghiniCheckbox: CheckBox? =
            requireActivity().findViewById(R.id.workshop_lamborghini_checkbox)
        val open: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_open_hours)
        val close: TextView? =
            requireActivity().findViewById(R.id.workshop_profile_close_hours)

        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("workshopName", username?.text.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("contactNumber", phoneNo?.text.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("latitude", latLng?.latitude.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("longitude", latLng?.longitude.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("address", address?.text.toString())
            if (latLng != null) {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("location", GeoPoint(latLng.latitude, latLng.longitude))
            }
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("description", description?.text.toString())
            val sharedPreferences =
                requireActivity().getSharedPreferences("open_hours", Context.MODE_PRIVATE)
            val openHours = sharedPreferences.getString("open_hours", "")
            if (openHours != "") {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("openHours", openHours)
            } else {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("openHours", open?.text)
            }
            val sp = requireActivity().getSharedPreferences("close_hours", Context.MODE_PRIVATE)
            val closeHours = sp.getString("close_hours", "")
            if (closeHours != "") {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("closeHours", closeHours)
            } else {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("closeHours", close?.text)
            }
            if (switch?.isChecked == true) {
                val list: java.util.ArrayList<String> = arrayListOf()
                if (protonCheckbox?.isChecked == true) {
                    list.add("Proton")
                }
                if (peroduaCheckbox?.isChecked == true) {
                    list.add("Perodua")
                }
                if (mercedesBenzCheckbox?.isChecked == true) {
                    list.add("Mercedes Benz")
                }
                if (lamborghiniCheckbox?.isChecked == true) {
                    list.add("Lamborghini")
                }
                if (toyotaCheckbox?.isChecked == true) {
                    list.add("Toyota")
                }
                if (list.size > 0) {
                    FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                        .update("vehicleBrands", list)
                }
            } else {
                val list: java.util.ArrayList<String> = arrayListOf()
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("vehicleBrands", list)
            }
        }

        Toast.makeText(requireContext(), "Profile had been updated.", Toast.LENGTH_SHORT).show()
    }

    private fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location = address[0]
            p1 = LatLng(location.latitude, location.longitude)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    private fun selectCover() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //intent.type = "images/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            200
        )
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
            uploadImageToFirebase(imageUrl, "workshopProfile")
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "workshop_profile_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("workshop_profile_image", imageUrl.toString())
            editor.apply()
            Glide.with(requireContext()).load(imageUrl)
                .into(requireView().workshop_profile_image)
        } else if (requestCode == 200 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            imageUrl = data?.data!!
            uploadImageToFirebase(imageUrl, "workshopCover")
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "workshop_profile_cover",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("workshop_profile_cover", imageUrl.toString())
            editor.apply()
            Glide.with(requireContext()).load(imageUrl)
                .into(requireView().workshop_profile_cover)
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri, type: String) {
        val sp = if (type == "workshopProfile") {
            "workshop_profile_image"
        } else {
            "workshop_profile_cover"
        }
        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(
                sp,
                Context.MODE_PRIVATE
            )
        val imageUri: String? = sharedPreferences.getString(sp, "")
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val user = FirebaseAuth.getInstance().currentUser
        val refStorage = FirebaseStorage.getInstance().reference.child("$type/$fileName")

        if (imageUri != null) {
            refStorage.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { it ->
                        val imageUrl = it.toString()
                        if (user != null) {
                            FirebaseFirestore.getInstance().collection(Constants.Users)
                                .document(user.uid).get().addOnSuccessListener { snapshot ->
                                    if (type == "workshopProfile") {
                                        val updateUser = hashMapOf(
                                            "workshopName" to snapshot.data?.get("workshopName")
                                                .toString(),
                                            "email" to snapshot.data?.get("email").toString(),
                                            "address" to snapshot.data?.get("address").toString(),
                                            "contactNumber" to snapshot.data?.get("contactNumber")
                                                .toString(),
                                            "id" to user.uid,
                                            "lowerName" to snapshot.data?.get("lowerName"),
                                            "latitude" to snapshot.data?.get("latitude"),
                                            "longitude" to snapshot.data?.get("longitude"),
                                            "location" to snapshot.data?.get("location"),
                                            "imageLink" to imageUrl,
                                            "cover" to snapshot.data?.get("cover"),
                                            "userType" to "workshop",
                                            "description" to snapshot.data?.get("description")
                                                .toString(),
                                        )
                                        FirebaseFirestore.getInstance().collection(Constants.Users)
                                            .document(user.uid).set(updateUser)
                                    } else {
                                        val updateUser = hashMapOf(
                                            "workshopName" to snapshot.data?.get("workshopName")
                                                .toString(),
                                            "email" to snapshot.data?.get("email").toString(),
                                            "address" to snapshot.data?.get("address").toString(),
                                            "contactNumber" to snapshot.data?.get("contactNumber")
                                                .toString(),
                                            "id" to user.uid,
                                            "lowerName" to snapshot.data?.get("lowerName"),
                                            "latitude" to snapshot.data?.get("latitude"),
                                            "longitude" to snapshot.data?.get("longitude"),
                                            "location" to snapshot.data?.get("location"),
                                            "imageLink" to snapshot.data?.get("imageLink"),
                                            "cover" to imageUrl,
                                            "userType" to "workshop",
                                            "description" to snapshot.data?.get("description")
                                                .toString(),
                                        )
                                        FirebaseFirestore.getInstance().collection(Constants.Users)
                                            .document(user.uid).set(updateUser)
                                    }
                                    Toast.makeText(
                                        requireContext(),
                                        "Image had been uploaded.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("error", e.toString())
                }
        }
    }

//    override fun onMapReady(googleMap: GoogleMap) {
//        val latitude = 37.422160
//        val longitude = -122.084270
//        val homeLatLng = LatLng(latitude, longitude)
//        map = googleMap
//        map.moveCamera(CameraUpdateFactory.newLatLng(homeLatLng))
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
//                enableMyLocation()
//            }
//        }
//    }
//
//    private fun isPermissionGranted() : Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun enableMyLocation() {
//        if (isPermissionGranted()) {
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            map.isMyLocationEnabled = true
//        }
//        else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        }
//    }
}