package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.CustomersAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Customers
import com.example.autopia.activities.utils.ProgressDialog
import com.google.firebase.auth.FirebaseAuth

class CustomersFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel
    private var customerList: List<Customers> = ArrayList()
    private lateinit var customerAdapter: CustomersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        viewModel.getCustomersByWorkshopId(uid)
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        viewModel.customers.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                customerList = response.body()!!
                customerAdapter = CustomersAdapter(
                    requireContext(),
                    customerList
                )

                val recyclerView: RecyclerView? = requireView().findViewById(R.id.customers_rv)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = customerAdapter
                recyclerView?.setHasFixedSize(true)
                customerAdapter.customerListItems = customerList
                progressDialog.dismissLoading()
            }
        }
    }
}