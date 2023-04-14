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
import com.example.autopia.activities.adapter.CustomerRelationViewPagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CustomerRelationshipManagementFragment : Fragment() {

    private val titles = arrayOf("Promotions", "Customers")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_customer_relationship_management,
            container,
            false
        )
    }

    override fun onStart() {
        super.onStart()
        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = false
    }

    override fun onStop() {
        super.onStop()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        bottomNavigationView?.isVisible = true
        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager: ViewPager2? = requireActivity().findViewById(R.id.crm_view_pager)
        val tabLayout: TabLayout? = requireActivity().findViewById(R.id.crm_tab_layout)
        val adapter: FragmentStateAdapter = CustomerRelationViewPagerAdapter(this)

        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 5
        if (tabLayout != null && viewPager != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }
    }
}