package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.autopia.R
import com.example.autopia.activities.adapter.StatisticsViewPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class WorkshopInsightsFragment : Fragment() {

    private val titles = arrayOf("Profile Views", "Bookings", "Popular Services")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = false

        return inflater.inflate(R.layout.fragment_workshop_insights, container, false)
    }

    override fun onPause() {
        super.onPause()
        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }

        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager: ViewPager2? = requireActivity().findViewById(R.id.statistics_view_pager)
        val tabLayout: TabLayout? = requireActivity().findViewById(R.id.statistics_tab_layout)
        val adapter: FragmentStateAdapter = StatisticsViewPagerAdapter(this)

        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 3
        if (tabLayout != null && viewPager != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }

        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 0f
            }
        }
    }
}