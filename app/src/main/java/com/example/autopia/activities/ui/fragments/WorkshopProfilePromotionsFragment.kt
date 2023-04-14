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
import com.example.autopia.activities.adapter.PromotionsAdapter
import com.example.autopia.activities.adapter.WorkshopFeedbacksAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Feedbacks
import com.example.autopia.activities.model.Promotions

class WorkshopProfilePromotionsFragment : Fragment() {

    private var promotionList: List<Promotions> = ArrayList()
    private lateinit var promotionListAdapter: PromotionsAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_profile_promotions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences: SharedPreferences? =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val workshopId: String? = preferences?.getString("workshop_id", "")

        promotionListAdapter = PromotionsAdapter(requireActivity(), promotionList)
        loadData(workshopId!!)
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(uid: String) {
        if (view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getPromotionsByWorkshopId(uid)
            viewModel.promotions.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    promotionList = response.body()!!
                    promotionListAdapter = PromotionsAdapter(requireActivity(), promotionList)
                    val recyclerView: RecyclerView? =
                        requireView().findViewById(R.id.workshop_promotions_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(view?.context)
                    recyclerView?.adapter = promotionListAdapter
                    recyclerView?.setHasFixedSize(true)
                    promotionListAdapter.promotionListItems = promotionList
                    promotionListAdapter.notifyDataSetChanged()
                } else {
                    val layout: ConstraintLayout? =
                        requireActivity().findViewById(R.id.noPromotionsLayout)
                    layout?.visibility = View.VISIBLE
                }
            }
        }
    }
}