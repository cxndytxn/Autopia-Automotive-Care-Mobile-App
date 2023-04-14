package com.example.autopia.activities.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.autopia.activities.ui.fragments.*

class StatisticsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles =
        arrayOf("Profile Views", "Bookings", "Popular Services")

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return ProfileViewsStatisticsFragment()
            }
            1 -> {
                return AppointmentsStatisticsFragment()
            }
            2 -> {
                return ServicesStatisticsFragment()
            }
        }
        return ProfileViewsStatisticsFragment()
    }
}