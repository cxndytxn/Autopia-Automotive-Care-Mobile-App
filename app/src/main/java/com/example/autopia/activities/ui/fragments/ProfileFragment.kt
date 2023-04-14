package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.utils.Constants
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*

class ProfileFragment : Fragment() {
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View
        val user = FirebaseAuth.getInstance().currentUser

        return if (user != null) {
            view = inflater.inflate(R.layout.fragment_profile, container, false)
            view
        } else {
            view = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = view.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.app_bar_layout)
            appBarLayout?.elevation = 8f
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid).get()
                .addOnSuccessListener {
                    val name: TextView? = requireActivity().findViewById(R.id.profile_name_tv)
                    val email: TextView? = requireActivity().findViewById(R.id.profile_email)
                    val username: TextView? = requireActivity().findViewById(R.id.profile_username)
                    val phoneNo: TextView? = requireActivity().findViewById(R.id.profile_phone)
                    val promotion: SwitchCompat? =
                        requireActivity().findViewById(R.id.profile_switch)
                    email?.isEnabled = false

                    name?.text = it.data?.get("username").toString()
                    email?.text = it.data?.get("email").toString()
                    username?.text = it.data?.get("username").toString()
                    if (it.data?.get("contactNumber") == "" || it.data?.get("contactNumber") == null || it.data?.get(
                            "contactNumber"
                        ) == "null"
                    )
                        phoneNo?.text = ""
                    else
                        phoneNo?.text = it.data?.get("contactNumber").toString()
                    promotion?.isChecked = it.data?.get("promotion").toString() == "true"
                    Glide.with(requireContext()).load(it.data?.get("imageLink"))
                        .into(requireView().user_profile_image)
                    Glide.with(requireContext()).load(it.data?.get("cover"))
                        .into(requireView().profile_cover)
                    promotion?.setOnCheckedChangeListener { compoundButton, b ->
                        val updateUser = hashMapOf(
                            "username" to username?.text.toString(),
                            "email" to it.data?.get("email").toString(),
                            "contactNumber" to phoneNo?.text.toString(),
                            "id" to user.uid,
                            "imageLink" to it.data?.get("imageLink").toString(),
                            "cover" to it.data?.get("cover").toString(),
                            "userType" to "user",
                            "promotion" to promotion.isChecked.toString()
                        )
                        FirebaseFirestore.getInstance().collection(Constants.Users)
                            .document(user.uid).set(updateUser)
                    }
                }
        }

        val cover: ImageView? = requireActivity().findViewById(R.id.profile_cover)
        cover?.setOnClickListener {
            selectCover()
        }

        val profileImage: ImageView? = requireActivity().findViewById(R.id.user_profile_image)
        profileImage?.setOnClickListener {
            selectImage()
        }

        val editBtn: Button? = requireActivity().findViewById(R.id.profile_edit_button)
        editBtn?.setOnClickListener {
            editProfile()
        }
    }

    private fun editProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        val username: TextView? = requireActivity().findViewById(R.id.profile_username)
        val phoneNo: TextView? = requireActivity().findViewById(R.id.profile_phone)
        val promotion: SwitchCompat? = requireActivity().findViewById(R.id.profile_switch)

        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid).get()
                .addOnSuccessListener { snapshot ->
                    val updateUser = hashMapOf(
                        "username" to username?.text.toString(),
                        "email" to snapshot.data?.get("email").toString(),
                        "contactNumber" to phoneNo?.text.toString(),
                        "id" to user.uid,
                        "imageLink" to snapshot.data?.get("imageLink").toString(),
                        "cover" to snapshot.data?.get("cover").toString(),
                        "userType" to "user",
                        "promotion" to promotion?.isChecked.toString()
                    )
                    FirebaseFirestore.getInstance().collection(Constants.Users)
                        .document(user.uid).set(updateUser)
                    Toast.makeText(
                        requireContext(),
                        "Profile had been updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Profile could not be updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
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
            uploadImageToFirebase(imageUrl, "profile")
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "profile_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("profile_image", imageUrl.toString())
            editor.apply()
            Glide.with(requireContext()).load(imageUrl)
                .into(requireView().user_profile_image)
        } else if (requestCode == 200 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            imageUrl = data?.data!!
            uploadImageToFirebase(imageUrl, "cover")
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "profile_cover",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("profile_cover", imageUrl.toString())
            editor.apply()
            Glide.with(requireContext()).load(imageUrl)
                .into(requireView().profile_cover)
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri, type: String) {
        val sp = if (type == "profile") {
            "profile_image"
        } else {
            "profile_cover"
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
                                    if (type == "profile") {
                                        val updateUser = hashMapOf(
                                            "username" to snapshot.data?.get("username").toString(),
                                            "email" to snapshot.data?.get("email").toString(),
                                            "contactNumber" to snapshot.data?.get("contactNumber")
                                                .toString(),
                                            "id" to user.uid,
                                            "imageLink" to imageUrl,
                                            "cover" to snapshot.data?.get("cover").toString(),
                                            "userType" to "user",
                                            "promotion" to snapshot.data?.get("promotion")
                                        )
                                        FirebaseFirestore.getInstance().collection(Constants.Users)
                                            .document(user.uid).set(updateUser)
                                    } else {
                                        val updateUser = hashMapOf(
                                            "username" to snapshot.data?.get("username").toString(),
                                            "email" to snapshot.data?.get("email").toString(),
                                            "contactNumber" to snapshot.data?.get("contactNumber")
                                                .toString(),
                                            "id" to user.uid,
                                            "imageLink" to snapshot.data?.get("imageLink")
                                                .toString(),
                                            "cover" to imageUrl,
                                            "userType" to "user",
                                            "promotion" to snapshot.data?.get("promotion")
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
}