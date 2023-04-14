package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class PromotionDetailsFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_promotion_details, container, false)
    }

    override fun onStart() {
        super.onStart()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirestoreClass().fetchUserByID(uid).addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    if (snapshot.result.data?.get("userType").toString() == "workshop") {
                        val workshopBottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
                        workshopBottomNavigationView?.isVisible = false
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val promotionId = arguments?.getInt("promotion_id")
        val image: ImageView? = requireActivity().findViewById(R.id.promotion_details_image)
        val title: TextView? = requireActivity().findViewById(R.id.promotion_details_title)
        val desc: TextView? = requireActivity().findViewById(R.id.promotion_details_desc)
        val start: TextView? = requireActivity().findViewById(R.id.details_start)
        val end: TextView? = requireActivity().findViewById(R.id.details_end)
        val workshop: TextView? = requireActivity().findViewById(R.id.details_workshop_name)
        val date: TextView? = requireActivity().findViewById(R.id.posted_date)

        val repository = Repository()
        val viewModelFactory = ApiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
        if (promotionId != null) {
            viewModel.getPromotionById(promotionId)
        }
        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.startLoading()
        viewModel.promotion.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                FirestoreClass().fetchWorkshopInfo(response.body()!!.workshopId).addOnSuccessListener {
                    workshop?.text = it.data?.get("workshopName").toString()
                }
                if (image != null) {
                    Glide.with(requireContext()).load(response.body()!!.imageLink)
                        .into(image)
                }
                title?.text = response.body()!!.title
                desc?.text = response.body()!!.description
                start?.text = response.body()!!.startDate
                end?.text = response.body()!!.endDate
                date?.text = response.body()!!.dateTime
                progressDialog.dismissLoading()
            } else {
                progressDialog.dismissLoading()
            }
        }
    }
}