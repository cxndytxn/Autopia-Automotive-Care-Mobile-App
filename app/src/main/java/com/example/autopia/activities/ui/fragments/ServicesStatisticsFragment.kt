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
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_services_statistics.*

class ServicesStatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services_statistics, container, false)
    }

    override fun onStart() {
        super.onStart()
        val pieChart = services_pie_chart
        pieChart?.invalidate()
        pieChart?.animateY(1000)
    }

    override fun onResume() {
        super.onResume()
        val pieChart = services_pie_chart
        pieChart?.invalidate()
        pieChart?.animateY(1000)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workshopId = FirebaseAuth.getInstance().currentUser?.uid
        val pieChart = services_pie_chart
        val services: ArrayList<PieEntry> = arrayListOf()

        FirebaseFirestore.getInstance().collection(Constants.ServicesStatistics)
            .whereEqualTo("workshopId", workshopId).get().addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (doc in task.result.documents) {
                        services.add(
                            PieEntry(
                                doc.data?.get("count").toString().toFloat(),
                                doc.data?.get("service").toString()
                            )
                        )
                    }
                    val pieDataSet = PieDataSet(services, "")
                    pieDataSet.valueTextColor = Color.BLACK
                    pieDataSet.valueTextSize = 16f

                    val colorsArr = intArrayOf(
                        Color.rgb(207, 210, 252),
                        Color.rgb(187, 191, 255),
                        Color.rgb(153, 159, 255),
                        Color.rgb(120, 128, 250),
                        Color.rgb(98, 108, 252),
                    )

                    val colors = ArrayList<Int>()

                    for (c in colorsArr) colors.add(c)

                    pieDataSet.colors = colors

                    val pieData = PieData(pieDataSet)
                    pieChart?.data = pieData
                    pieChart?.description?.text = ""
                    pieChart?.setHoleColor(
                        requireParentFragment().requireActivity().getColor(R.color.theme_grey)
                    )
                    pieChart?.animateY(1000)
                    pieChart?.invalidate()
                } else {
                    val noStatistics: ConstraintLayout? = parentFragment?.requireActivity()
                        ?.findViewById(R.id.noServicesStatisticsLayout)
                    noStatistics?.visibility = View.VISIBLE
                }
            }
//                    services.add(PieEntry(508f, "2015"))
//                    services.add(PieEntry(358f, "2016"))
//                    services.add(PieEntry(828f, "2017"))
//                    services.add(PieEntry(1028f, "2018"))
//                    services.add(PieEntry(937f, "2019"))
    }
}