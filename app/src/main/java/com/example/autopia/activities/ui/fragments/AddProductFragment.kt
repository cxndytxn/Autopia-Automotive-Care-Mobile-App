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
import com.example.autopia.activities.model.Products
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddProductFragment : Fragment() {
    private lateinit var viewModel: ApiViewModel
    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_add_product, container, false)

        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView.isVisible = false

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = arguments?.getInt("product_id")
        val addProductBtn: Button? = requireActivity().findViewById(R.id.add_product_btn)
        val user = FirebaseAuth.getInstance().currentUser

        if (productId != null) {
            loadData(productId)
            "Edit Product".also { addProductBtn?.text = it }

            addProductBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                val isValidFormat: Boolean = validateInputFormat()
                if (isValid) {
                    if (isValidFormat) {
                        user?.uid?.let { it1 -> editProduct(productId, it1) }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure price is in numerical format.",
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
            addProductBtn?.setOnClickListener {
                val isValid: Boolean = validateInputFields()
                val isValidFormat: Boolean = validateInputFormat()
                if (user != null && isValid) {
                    if (isValidFormat) {
                        addProduct(user.uid)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please ensure price is in numerical format.",
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

        val productImage: ImageView? = requireActivity().findViewById(R.id.upload_product_image)
        productImage?.setOnClickListener {
            selectImage()
        }

        val uploadButton: MaterialButton? =
            requireActivity().findViewById(R.id.upload_product_image_button)
        uploadButton?.setOnClickListener {
            selectImage()
        }

        arguments?.remove("product_id")
    }

    private fun validateInputFormat(): Boolean {
        val price: TextView? = requireActivity().findViewById(R.id.add_product_price)
        val priceText: String = price?.text.toString()
        return (priceText.matches("-?\\d+(\\.\\d+)?".toRegex()))
    }

    private fun validateInputFields(): Boolean {
        val name: TextView? = requireActivity().findViewById(R.id.add_product_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_product_description)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }

        return !(nameText.isEmpty() || descriptionText.isEmpty())
    }

    private fun loadData(product_id: Int) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getProductById(product_id)
            viewModel.product.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val name: TextView? = requireActivity().findViewById(R.id.add_product_name)
                    name?.text = body?.name
                    val description: TextView? =
                        requireActivity().findViewById(R.id.add_product_description)
                    description?.text = body?.description
                    val price: TextView? = requireActivity().findViewById(R.id.add_product_price)
                    price?.text = body?.price.toString()
                    if (response.body()?.imageLink != null && response.body()!!.imageLink != "") {
                        val uploadButton: MaterialButton? =
                            requireActivity().findViewById(R.id.upload_product_image_button)
                        uploadButton?.isVisible = false
                        val productImage: ImageView? =
                            requireActivity().findViewById(R.id.upload_product_image)
                        if (productImage != null) {
                            Glide.with(requireContext()).load(response.body()!!.imageLink)
                                .into(productImage)
                        }
                    }
                    progressDialog.dismissLoading()
                }
            }
        }
    }

    private fun addProduct(user_id: String) {
        val name: TextView? = requireActivity().findViewById(R.id.add_product_name)
        val description: TextView? = requireActivity().findViewById(R.id.add_product_description)
        val price: TextView? = requireActivity().findViewById(R.id.add_product_price)
        val nameText: String = name?.text.toString().trim { it <= ' ' }
        val descriptionText: String = description?.text.toString().trim { it <= ' ' }
        val priceText: Double = price?.text.toString().toDouble()

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_product_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_product_image", "")
        val product = Products(
            null,
            user_id,
            nameText,
            descriptionText,
            priceText.toString().toDouble(),
            imageUri
        )
        val apiInterface = ApiInterface.create().postProducts(product)
        apiInterface.enqueue(object : Callback<Products> {
            override fun onResponse(
                call: Call<Products>,
                response: Response<Products>,
            ) {
                Toast.makeText(context, "Successfully added a new product!", Toast.LENGTH_SHORT)
                    .show()
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.popBackStack()
            }

            override fun onFailure(call: Call<Products>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Error. Product could not be added. " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun editProduct(product_id: Int, user_id: String) {
        val name: TextView = requireActivity().findViewById(R.id.add_product_name)
        val description: TextView = requireActivity().findViewById(R.id.add_product_description)
        val price: TextView = requireActivity().findViewById(R.id.add_product_price)
        val nameText: String = name.text.toString().trim { it <= ' ' }
        val descriptionText: String = description.text.toString().trim { it <= ' ' }
        val priceText: Double = price.text.toString().toDouble()

        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences(
            "uploaded_product_image",
            Context.MODE_PRIVATE
        )
        val imageUri: String? = sharedPreferences.getString("uploaded_product_image", "")
        val product: Products
        val productImage: ImageView? =
            requireActivity().findViewById(R.id.upload_product_image)
        if (productImage?.drawable != null) {
            product = Products(
                product_id,
                user_id,
                nameText,
                descriptionText,
                priceText,
                imageUri
            )
        } else {
            product = Products(
                product_id,
                user_id,
                nameText,
                descriptionText,
                priceText,
                null
            )
        }
        val apiInterface = ApiInterface.create().putProducts(product, product_id)
        apiInterface.enqueue(object : Callback<Products> {
            override fun onResponse(
                call: Call<Products>,
                response: Response<Products>,
            ) {
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.popBackStack()
                Toast.makeText(context, "Product is updated!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Products>, t: Throwable) {
                Toast.makeText(context, "Product could not be updated.", Toast.LENGTH_SHORT).show()
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
            val productImage: ImageView? = requireActivity().findViewById(R.id.upload_product_image)
            val uploadButton: MaterialButton? =
                requireActivity().findViewById(R.id.upload_product_image_button)
            uploadButton?.isVisible = false
            productImage?.setImageURI(imageUrl)
            uploadImageToFirebase(imageUrl)
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "product_image",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()

            editor.putString("product_image", imageUrl.toString())
            editor.apply()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("product/$fileName")

        refStorage.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "uploaded_product_image",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()

                    editor.putString("uploaded_product_image", it.toString())
                    editor.apply()
                }
            }
            .addOnFailureListener { e ->
            }
    }
}