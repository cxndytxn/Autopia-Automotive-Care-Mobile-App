package com.example.autopia.activities.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.autopia.R
import com.example.autopia.activities.utils.Constants
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile_views_statistics.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileViewsStatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_views_statistics, container, false)
    }

    override fun onStart() {
        super.onStart()
        val barChart = visitors_bar_chart
        barChart?.invalidate()
        barChart?.animateY(1200)
    }

    override fun onResume() {
        super.onResume()
        val barChart = visitors_bar_chart
        barChart?.invalidate()
        barChart?.animateY(1200)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workshopId = FirebaseAuth.getInstance().currentUser?.uid
        val barChart = visitors_bar_chart
        val visitors: ArrayList<BarEntry> = arrayListOf()

        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DATE, -7)
        val sevenDaysAgo = cal.time
        FirebaseFirestore.getInstance().collection(Constants.ViewStatistics)
            .whereLessThan("date", today).whereGreaterThan("date", sevenDaysAgo)
            .whereEqualTo("workshopId", workshopId).get().addOnCompleteListener { task ->
            if (task.isSuccessful && !task.result.isEmpty) {
                var count = 1f
                for (doc in task.result.documents) {
                    visitors.add(BarEntry(count, doc.data?.get("viewCount").toString().toFloat()))
                    count++
                }
                val barDataSet = BarDataSet(visitors, "")
                barDataSet.valueTextColor = Color.BLACK
                barDataSet.valueTextSize = 16f

                val colorsArr = intArrayOf(
                    Color.rgb(207, 210, 252),
                    Color.rgb(187, 191, 255),
                    Color.rgb(153, 159, 255),
                    Color.rgb(120, 128, 250),
                    Color.rgb(98, 108, 252),
                )

                val colors = ArrayList<Int>()

                for (c in colorsArr) colors.add(c)

                barDataSet.colors = colors

                val barData = BarData(barDataSet)
                barChart?.setFitBars(true)
                barChart?.data = barData
                barChart?.description?.text = ""
                barChart?.animateY(1200)
                barChart?.invalidate()
            } else {
                Log.d("why", task.exception.toString())
                val noStatistics: ConstraintLayout? = parentFragment?.requireActivity()?.findViewById(R.id.noViewsStatisticsLayout)
                noStatistics?.visibility = View.VISIBLE
            }
        }
    }
}