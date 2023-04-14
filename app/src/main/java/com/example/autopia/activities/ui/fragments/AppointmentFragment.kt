package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.autopia.R
import com.example.autopia.activities.adapter.AppointmentsViewPagerAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AppointmentFragment : Fragment() {

    private val titles =
        arrayOf("Upcoming", "Pending", "Reschedule", "Rejected/Cancelled", "No Show")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val view: View
        return if (user != null) {
            view = inflater.inflate(R.layout.fragment_appointment, container, false)
            view
        } else {
            view = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = view.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
            view
        }
    }

    override fun onPause() {
        super.onPause()

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType") == "workshop") {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.workshop_app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                        }
                    } else {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                        }
                    }
                }
            }
        } else {
            if (isAdded) {
                val appBarLayout: AppBarLayout? =
                    requireActivity().findViewById(R.id.app_bar_layout)
                appBarLayout?.elevation = 8f
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType") == "workshop") {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.workshop_app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f

                            }
                        }
                    } else {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.app_bar_layout)
                            if (appBarLayout != null) {
                                appBarLayout.elevation = 8f
                            }
                        }
                    }
                }
            }
        } else {
            if (isAdded) {
                val appBarLayout: AppBarLayout? =
                    requireActivity().findViewById(R.id.app_bar_layout)
                appBarLayout?.elevation = 8f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadPager()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType") == "workshop") {
                        if (isAdded) {
                            val bottomNavigationView: BottomNavigationView? =
                                requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
                            bottomNavigationView?.isVisible = true
                        }
                    } else {
                        if (isAdded) {
                            val bottomNavigationView: BottomNavigationView? =
                                requireActivity().findViewById(R.id.bottom_navigation_view)
                            bottomNavigationView?.isVisible = true
                        }
                    }
                }
            }
        } else {
            if (isAdded) {
                val appBarLayout: AppBarLayout? =
                    requireActivity().findViewById(R.id.app_bar_layout)
                appBarLayout?.elevation = 8f
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loadPager()
    }

    private fun loadPager() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            FirestoreClass().fetchWorkshopInfo(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType") == "workshop") {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.workshop_app_bar_layout)
                            appBarLayout?.elevation = 0f
                        }
                    } else {
                        if (isAdded) {
                            val appBarLayout: AppBarLayout? =
                                requireActivity().findViewById(R.id.app_bar_layout)
                            appBarLayout?.elevation = 0f
                        }
                    }
                }
            }

            val viewPager: ViewPager2? = requireActivity().findViewById(R.id.appointment_view_pager)
            val tabLayout: TabLayout? = requireActivity().findViewById(R.id.appointment_tab_layout)
            val adapter: FragmentStateAdapter = AppointmentsViewPagerAdapter(this)

            viewPager?.adapter = adapter
            viewPager?.offscreenPageLimit = 5
            if (tabLayout != null && viewPager != null) {
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    tab.text = titles[position]
                }.attach()
            }
        }
    }
}