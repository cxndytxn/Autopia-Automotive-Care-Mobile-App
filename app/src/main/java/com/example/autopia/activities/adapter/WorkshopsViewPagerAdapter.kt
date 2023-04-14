package com.example.autopia.activities.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.autopia.activities.ui.fragments.*

class WorkshopsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles = arrayOf("Info", "Services", "Products", "Promotions", "Feedbacks")

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return WorkshopProfileInfoFragment()
            }
            1 -> {
                return WorkshopProfileServicesFragment()
            }
            2 -> {
                return WorkshopProfileProductsFragment()
            }
            3 -> {
                return WorkshopProfilePromotionsFragment()
            }
            4 -> {
                return WorkshopProfileFeedbacksFragment()
            }
        }
        return WorkshopProfileInfoFragment()
    }

}