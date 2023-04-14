package com.example.autopia.activities.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.autopia.activities.ui.fragments.WorkshopsFragment
import com.example.autopia.activities.ui.fragments.NearbyWorkshopsFragment

class WorkshopsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles = arrayOf("Nearby", "All")

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return WorkshopsFragment()
            }
            1 -> {
                return NearbyWorkshopsFragment()
            }
        }
        return WorkshopsFragment()
    }


}