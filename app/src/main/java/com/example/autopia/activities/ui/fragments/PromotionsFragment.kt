package com.example.autopia.activities.ui.fragments

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
import com.example.autopia.activities.adapter.PromotionsAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Promotions
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_vehicles.*

class PromotionsFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel
    private var promotionList: List<Promotions> = ArrayList()
    private lateinit var promotionAdapter: PromotionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_promotions, container, false)

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            root.findViewById<FloatingActionButton>(R.id.add_promotion_button).setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.workshop_nav_host_fragment)
                navController.navigate(R.id.addPromotionFragment)
            }
        } else {
            "Hello there, you can't manage promotions when you're not logged in!".also {
                not_logged_in_vehicles.text = it
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseAuth.getInstance().currentUser?.uid!!
        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        viewModel.getPromotionsByWorkshopId(uid)
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        viewModel.promotions.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                promotionList = response.body()!!
                promotionAdapter = PromotionsAdapter(
                    requireActivity(),
                    promotionList
                )
                val recyclerView: RecyclerView? = requireView().findViewById(R.id.promotions_rv)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = promotionAdapter
                recyclerView?.setHasFixedSize(true)
                promotionAdapter.promotionListItems = promotionList
                progressDialog.dismissLoading()
            } else {
                progressDialog.dismissLoading()
            }
        }
    }
}