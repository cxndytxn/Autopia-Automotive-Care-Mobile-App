package com.example.autopia.activities.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class WorkshopInfoActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private lateinit var imageUrl: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workshop_info)

        val workshopName: TextView? = findViewById(R.id.w_info_name)
        val email: TextView? = findViewById(R.id.w_info_email)
        val updateBtn: Button? = findViewById(R.id.w_update_button)
        val image: ImageView? = findViewById(R.id.w_info_profile_image)
        val cover: ImageView? = findViewById(R.id.w_profile_cover)
        val phoneNo: TextView? = findViewById(R.id.w_info_phone)
        val description: TextView? = findViewById(R.id.w_info_description)
        val address: TextView? = findViewById(R.id.w_info_address)
        val switch: SwitchCompat? = findViewById(R.id.vehicle_brands_switch)
        val vehicleBrandsText: TextView? = findViewById(R.id.vehicle_brands)
        val checkboxesGroup: LinearLayout? = findViewById(R.id.checkboxes_group)
        val openHours: TextView? = findViewById(R.id.w_info_open_hours)
        val closeHours: TextView? = findViewById(R.id.w_info_close_hours)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    email?.text = it.result.data?.get("email").toString()
                    email?.isEnabled = false
                    workshopName?.text = it.result.data?.get("workshopName").toString()
                    if (it.result.data?.get("imageLink") != null || it.result.data?.get("imageLink") != "") {
                        if (image != null) {
                            Glide.with(this).load(it.result.data?.get("imageLink")).into(image)
                        }
                    }
                    if (it.result.data?.get("address") != "" || it.result.data?.get("address") != null) {
                        address?.text = it.result.data?.get("address").toString()
                    }
                    if (it.result.data?.get("contactNumber") != "" || it.result.data?.get("contactNumber") != null) {
                        phoneNo?.text = it.result.data?.get("contactNumber").toString()
                    }
                    if (it.result.data?.get("description") != "" || it.result.data?.get("description") != null) {
                        description?.text = it.result.data?.get("description").toString()
                    }
                    if (it.result.data?.get("cover") != null || it.result.data?.get("cover") != "") {
                        if (cover != null) {
                            Glide.with(this).load(it.result.data?.get("cover")).into(cover)
                        }
                    }
                    if (it.result.data?.get("openHours") != null || it.result.data?.get("openHours") != "") {
                        openHours?.text = it.result.data?.get("openHours").toString()
                    }
                    if (it.result.data?.get("closeHours") != null || it.result.data?.get("closeHours") != "") {
                        closeHours?.text = it.result.data?.get("closeHours").toString()
                    }
                    val protonCheckbox: MaterialCheckBox? =
                        findViewById(R.id.proton_checkbox)
                    val peroduaCheckbox: MaterialCheckBox? =
                        findViewById(R.id.perodua_checkbox)
                    val toyotaCheckbox: MaterialCheckBox? =
                        findViewById(R.id.toyota_checkbox)
                    val mercedesCheckbox: MaterialCheckBox? =
                        findViewById(R.id.mercedes_benz_checkbox)
                    val lamborghiniCheckbox: MaterialCheckBox? =
                        findViewById(R.id.lamborghini_checkbox)
                    val checkboxes: LinearLayout? =
                        findViewById(R.id.checkboxes_group)
                    val vehicleBrands: TextView? =
                        findViewById(R.id.vehicle_brands)
                    if (it.result.data?.get("vehicleBrands") != null) {
                        val array: ArrayList<String> =
                            it.result.data?.get("vehicleBrands") as ArrayList<String>
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
                }
            }
        }

        updateBtn?.setOnClickListener {
            val isValid = validateInputFields()
            if (isValid) {
                if (switch?.isChecked == true) {
                    val isCheckboxSelected = validateCheckboxes()
                    if (isCheckboxSelected) {
                        updateData()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Please ensure at least one checkbox is selected!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    updateData()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        image?.setOnClickListener {
            selectImage()
        }

        cover?.setOnClickListener {
            selectCover()
        }

        switch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vehicleBrandsText?.visibility = View.VISIBLE
                checkboxesGroup?.visibility = View.VISIBLE
            } else {
                vehicleBrandsText?.visibility = View.GONE
                checkboxesGroup?.visibility = View.GONE
            }
        }

        openHours?.setOnClickListener { openOpenTimePickerDialog() }

        closeHours?.setOnClickListener { openCloseTimePickerDialog() }
    }

    private fun openOpenTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.newInstance(
            this, calendar.time.hours, calendar.time.minutes, false
        )
        timePicker.version = TimePickerDialog.Version.VERSION_2
        timePicker.show(supportFragmentManager, "OpenTimePickerDialog")
    }

    private fun openCloseTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.newInstance(
            this, calendar.time.hours, calendar.time.minutes, false
        )
        timePicker.version = TimePickerDialog.Version.VERSION_2
        timePicker.show(supportFragmentManager, "CloseTimePickerDialog")
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
        val formatted2 = sdf2.format(formatted)
        if (timePicker?.tag == "OpenTimePickerDialog") {
            val time: EditText? = findViewById(R.id.w_info_open_hours)
            time?.setText(formattedTime)

            val preferences: SharedPreferences =
                getSharedPreferences("open_hours", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("open_hours", formatted2)
            editor.apply()
        } else {
            val time: EditText? = findViewById(R.id.w_info_close_hours)
            time?.setText(formattedTime)

            val preferences: SharedPreferences =
                getSharedPreferences("close_hours", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            editor.putString("close_hours", formatted2)
            editor.apply()
        }
    }

    private fun validateCheckboxes(): Boolean {
        val protonCheckbox: MaterialCheckBox? = findViewById(R.id.proton_checkbox)
        val peroduaCheckbox: MaterialCheckBox? = findViewById(R.id.perodua_checkbox)
        val toyotaCheckbox: MaterialCheckBox? = findViewById(R.id.toyota_checkbox)
        val mercedesBenzCheckbox: MaterialCheckBox? = findViewById(R.id.mercedes_benz_checkbox)
        val lamborghiniCheckbox: MaterialCheckBox? = findViewById(R.id.lamborghini_checkbox)

        return if (protonCheckbox != null && peroduaCheckbox != null && toyotaCheckbox != null && mercedesBenzCheckbox != null && lamborghiniCheckbox != null) {
            (protonCheckbox.isChecked || peroduaCheckbox.isChecked || toyotaCheckbox.isChecked || mercedesBenzCheckbox.isChecked || lamborghiniCheckbox.isChecked)
        } else {
            false
        }
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
            if (address.isNotEmpty()) {
                val location = address[0]
                p1 = LatLng(location.latitude, location.longitude)
            } else {
                Toast.makeText(
                    this,
                    "Please enter a correctly formatted address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    private fun updateData() {
        val user = FirebaseAuth.getInstance().currentUser
        val description: TextView? = findViewById(R.id.w_info_description)
        val phoneNo: TextView? = findViewById(R.id.w_info_phone)
        val address: TextView? = findViewById(R.id.w_info_address)
        val latLng: LatLng? = getLocationFromAddress(applicationContext, address?.text.toString())
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "open_hours",
            Context.MODE_PRIVATE
        )
        val openHours: String? = sharedPreferences.getString("open_hours", "")
        val sp: SharedPreferences = getSharedPreferences(
            "close_hours",
            Context.MODE_PRIVATE
        )
        val closeHours: String? = sp.getString("close_hours", "")
        val switch: SwitchCompat? = findViewById(R.id.vehicle_brands_switch)
        val protonCheckbox: MaterialCheckBox? = findViewById(R.id.proton_checkbox)
        val peroduaCheckbox: MaterialCheckBox? = findViewById(R.id.perodua_checkbox)
        val toyotaCheckbox: MaterialCheckBox? = findViewById(R.id.toyota_checkbox)
        val mercedesBenzCheckbox: MaterialCheckBox? = findViewById(R.id.mercedes_benz_checkbox)
        val lamborghiniCheckbox: MaterialCheckBox? = findViewById(R.id.lamborghini_checkbox)

        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("contactNumber", phoneNo?.text.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("latitude", latLng?.latitude.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("longitude", latLng?.longitude.toString())
            if (latLng != null) {
                FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                    .update("location", GeoPoint(latLng.latitude, latLng.longitude))
            }
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("address", address?.text.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("description", description?.text.toString())
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("openHours", openHours)
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid)
                .update("closeHours", closeHours)
            if (switch?.isChecked == true) {
                val list: ArrayList<String> = arrayListOf()
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
            }
            Toast.makeText(
                applicationContext,
                "Your workshop details had been updated.",
                Toast.LENGTH_SHORT
            ).show()
            val intent =
                Intent(this@WorkshopInfoActivity, WorkshopNavigationDrawerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputFields(): Boolean {
        val description: TextView? = findViewById(R.id.w_info_description)
        val phoneNo: TextView? = findViewById(R.id.w_info_phone)
        val address: TextView? = findViewById(R.id.w_info_address)
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val passwordText: String = phoneNo?.text.toString().trim { it <= ' ' }
        val addressText: String = address?.text.toString().trim { it <= ' ' }
        val openHours: TextView? = findViewById(R.id.w_info_open_hours)
        val closeHours: TextView? = findViewById(R.id.w_info_close_hours)
        val openHoursText: String = openHours?.text.toString().trim { it <= ' ' }
        val closeHoursText: String = closeHours?.text.toString().trim { it <= ' ' }

        return !(descriptionText.isEmpty() || passwordText.isEmpty() || addressText.isEmpty() || openHoursText.isEmpty() || closeHoursText.isEmpty())
    }

    private fun selectCover() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

        if (requestCode == 100 && resultCode != RESULT_CANCELED) {
            imageUrl = data?.data!!
            val profileImage: ImageView? = findViewById(R.id.w_info_profile_image)
            if (profileImage != null) {
                Glide.with(applicationContext).load(imageUrl)
                    .into(profileImage)
            }
            uploadImageToFirebase(imageUrl, "workshopProfile")
        } else if (requestCode == 200 && resultCode != RESULT_CANCELED) {
            imageUrl = data?.data!!
            val cover: ImageView? = findViewById(R.id.w_profile_cover)
            if (cover != null) {
                Glide.with(applicationContext).load(imageUrl)
                    .into(cover)
            }
            uploadImageToFirebase(imageUrl, "workshopCover")
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri, type: String) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val user = FirebaseAuth.getInstance().currentUser
        val refStorage = FirebaseStorage.getInstance().reference.child("$type/$fileName")

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
                                        "latitude" to snapshot.data?.get("latitude"),
                                        "longitude" to snapshot.data?.get("longitude"),
                                        "location" to snapshot.data?.get("location"),
                                        "lowerName" to snapshot.data?.get("lowerName"),
                                        "id" to user.uid,
                                        "imageLink" to imageUrl,
                                        "cover" to snapshot.data?.get("cover"),
                                        "userType" to "workshop",
                                        "description" to snapshot.data?.get("description")
                                            .toString()
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
                                        "latitude" to snapshot.data?.get("latitude"),
                                        "longitude" to snapshot.data?.get("longitude"),
                                        "location" to snapshot.data?.get("location"),
                                        "lowerName" to snapshot.data?.get("lowerName"),
                                        "id" to user.uid,
                                        "imageLink" to snapshot.data?.get("imageLink"),
                                        "cover" to imageUrl,
                                        "userType" to "workshop",
                                        "description" to snapshot.data?.get("description")
                                            .toString()
                                    )
                                    FirebaseFirestore.getInstance().collection(Constants.Users)
                                        .document(user.uid).set(updateUser)
                                }
                                Toast.makeText(
                                    applicationContext,
                                    "Profile image had been uploaded.",
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