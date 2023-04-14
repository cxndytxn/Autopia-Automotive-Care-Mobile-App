package com.example.autopia.activities.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.autopia.activities.ui.fragments.*

class AppointmentsViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val titles =
        arrayOf("Upcoming", "Pending", "Reschedule", "Rejected/Cancelled", "No Show")

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return UpcomingAppointmentsFragment()
            }
            1 -> {
                return PendingAppointmentsFragment()
            }
            2 -> {
                return ReschedulingAppointmentsFragment()
            }
            3 -> {
                return RejectedAppointmentsFragment()
            }
            4 -> {
                return NoShowAppointmentsFragment()
            }
        }
        return UpcomingAppointmentsFragment()
    }
}