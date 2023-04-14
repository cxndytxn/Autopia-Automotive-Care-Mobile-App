package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.ProductsAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Products
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class ProductsFragment : Fragment() {
    private var productList: List<Products> = ArrayList()
    private lateinit var productListAdapter: ProductsAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_products, container, false)

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        root?.findViewById<FloatingActionButton>(R.id.add_product_button)?.setOnClickListener {
            val navController = requireActivity().findNavController(R.id.workshop_nav_host_fragment)
            navController.navigate(R.id.addProductFragment)
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            loadData(user.uid)

            root?.findViewById<FloatingActionButton>(R.id.add_product_button)?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.navigate(R.id.addProductFragment)
            }
        }

        productListAdapter = ProductsAdapter(requireActivity(), productList)

        return root
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            loadData(user.uid)
        }
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

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(uid: String) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getProductsByWorkshopId(uid)
            viewModel.products.observe(viewLifecycleOwner) { response ->
                //pass list here
                if (response.isSuccessful) {
                    productList = response.body()!!
                    productListAdapter = ProductsAdapter(requireActivity(), productList)
                    val recyclerView: RecyclerView? = requireView().findViewById(R.id.product_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = productListAdapter
                    recyclerView?.setHasFixedSize(true)
                    productListAdapter.productListItems = productList
                    productListAdapter.notifyDataSetChanged()
                    progressDialog.dismissLoading()
                } else {
                    //error
                }
            }
        }
    }
}