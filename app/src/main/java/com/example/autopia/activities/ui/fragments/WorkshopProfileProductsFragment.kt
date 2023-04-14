package com.example.autopia.activities.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopProductsAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Products

class WorkshopProfileProductsFragment : Fragment() {
    private var productList: List<Products> = ArrayList()
    private lateinit var productListAdapter: WorkshopProductsAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_profile_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        productListAdapter = WorkshopProductsAdapter(requireActivity(), productList)
        loadData(workshopId!!)
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(uid: String) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getProductsByWorkshopId(uid)
            viewModel.products.observe(viewLifecycleOwner) { response ->
                //pass list here
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    productList = response.body()!!
                    productListAdapter = WorkshopProductsAdapter(requireActivity(), productList)
                    val recyclerView: RecyclerView? =
                        requireView().findViewById(R.id.workshop_products_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = productListAdapter
                    recyclerView?.setHasFixedSize(true)
                    productListAdapter.productListItems = productList
                    productListAdapter.notifyDataSetChanged()
                } else {
                    val layout: ConstraintLayout? =
                        requireActivity().findViewById(R.id.noProductsLayout)
                    layout?.visibility = View.VISIBLE
                }
            }
        }
    }
}