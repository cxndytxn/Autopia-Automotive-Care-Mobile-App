package com.example.autopia.activities.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.jsr310.scrollToDateTime
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.example.autopia.R
import com.example.autopia.activities.adapter.WeekViewAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.app_bar_workshop_navigation_drawer.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class WorkshopHomeFragment : Fragment() {

    private lateinit var viewModel: ApiViewModel
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_workshop_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        loadData()
        requireActivity().workshop_toolbar.popupTheme = R.style.WorkshopHomeFragmentMenuStyle
        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.workshop_app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }
    }

    private fun loadData() {
        val weekView: WeekView = requireActivity().findViewById(R.id.weekView)
        weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && view != null) {
            val adapter = WeekViewAdapter(requireView())
            weekView.adapter = adapter
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            viewModel =
                ViewModelProvider(viewModelStore, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getScheduledAppointmentsByWorkshopId(user.uid)

            lifecycleScope.launchWhenStarted {
                viewModel.appointments.observe(viewLifecycleOwner) { response ->
                    if (response.isSuccessful) {
                        adapter.submitList(response.body()!!)
                    } else {
                        Log.d("error", response.message())
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val weekView: WeekView = requireActivity().findViewById(R.id.weekView)
        item.isChecked = !item.isChecked

        when (item.itemId) {
            R.id.oneDayView -> {
                weekView.numberOfVisibleDays = 1
            }
            R.id.threeDayView -> {
                weekView.numberOfVisibleDays = 3
            }
            R.id.fiveDayView -> {
                weekView.numberOfVisibleDays = 5
            }
            R.id.oneWeekView -> {
                weekView.numberOfVisibleDays = 7
            }
            R.id.action_today -> {
                weekView.scrollToDateTime(dateTime = LocalDateTime.now())
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.workshop_home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}