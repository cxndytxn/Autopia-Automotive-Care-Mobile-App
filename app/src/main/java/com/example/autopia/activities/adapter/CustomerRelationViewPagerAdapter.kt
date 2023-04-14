package com.example.autopia.activities.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.autopia.activities.ui.fragments.CustomersFragment
import com.example.autopia.activities.ui.fragments.PromotionsFragment

class CustomerRelationViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles =
        arrayOf("Promotions", "Customers")

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return PromotionsFragment()
            }
            1 -> {
                return CustomersFragment()
            }
        }
        return PromotionsFragment()
    }
}