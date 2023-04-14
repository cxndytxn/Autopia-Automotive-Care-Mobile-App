package com.example.autopia.activities.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.autopia.R
import com.example.autopia.activities.utils.Constants
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_appointments_statistics.*
import java.util.*
import kotlin.collections.ArrayList

class AppointmentsStatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_appointments_statistics, container, false)
    }

    override fun onStart() {
        super.onStart()
        val lineChart = appointments_line_chart
        lineChart?.invalidate()
        lineChart?.animateX(1000)
    }

    override fun onResume() {
        super.onResume()
        val lineChart = appointments_line_chart
        lineChart?.invalidate()
        lineChart?.animateX(1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workshopId = FirebaseAuth.getInstance().currentUser?.uid
        val lineChart = appointments_line_chart
        val bookings: ArrayList<Entry> = arrayListOf()

        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DATE, -7)
        val sevenDaysAgo = cal.time
        FirebaseFirestore.getInstance().collection(Constants.BookingStatistics)
            .whereLessThan("date", today).whereGreaterThan("date", sevenDaysAgo)
            .whereEqualTo("workshopId", workshopId).get().addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    var count = 1f
                    for (doc in task.result.documents) {
                        bookings.add(
                            BarEntry(
                                count,
                                doc.data?.get("bookingCount").toString().toFloat()
                            )
                        )
                        count++
                    }
                    val lineDataSet = LineDataSet(bookings, "")
                    lineDataSet.valueTextColor = Color.BLACK
                    lineDataSet.valueTextSize = 16f
                    lineDataSet.lineWidth = 3f
                    lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                    val colorsArr = intArrayOf(
                        Color.rgb(207, 210, 252),
                        Color.rgb(187, 191, 255),
                        Color.rgb(153, 159, 255),
                        Color.rgb(120, 128, 250),
                        Color.rgb(98, 108, 252),
                    )

                    val colors = ArrayList<Int>()

                    for (c in colorsArr) colors.add(c)

                    lineDataSet.colors = colors

                    val lineData = LineData(lineDataSet)
                    lineChart?.data = lineData
                    lineChart?.description?.text = ""
                    lineChart?.animateX(1000)
                    lineChart?.invalidate()
                } else {
                    val noStatistics: ConstraintLayout? =
                        parentFragment?.requireActivity()?.findViewById(R.id.noAppointmentsStatisticsLayout)
                    noStatistics?.visibility = View.VISIBLE
                }
            }

//        bookings.add(Entry(1f, 5f))
//        bookings.add(Entry(2f, 7f))
//        bookings.add(Entry(3f, 13f))
//        bookings.add(Entry(4f, 9f))
//        bookings.add(Entry(5f, 15f))
    }
}