package com.example.autopia.activities.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.example.autopia.R
import com.example.autopia.activities.model.Appointments
import java.text.SimpleDateFormat
import java.util.*

class WeekViewAdapter(var view: View) : WeekView.SimpleAdapter<Appointments>() {

    override fun onCreateEntity(item: Appointments): WeekViewEntity {
        val startTime = Calendar.getInstance()
        val formattedStartTime = timeFormatter(item.date, item.startTime)
        startTime.set(
            formattedStartTime[0],
            formattedStartTime[1],
            formattedStartTime[2],
            formattedStartTime[3],
            formattedStartTime[4]
        )
        val endTime = Calendar.getInstance()
        val formattedEndTime = timeFormatter(item.date, item.endTime!!)
        endTime.set(
            formattedEndTime[0],
            formattedEndTime[1],
            formattedEndTime[2],
            formattedEndTime[3],
            formattedEndTime[4]
        )
        val title = item.services
        val styledTitle = SpannableStringBuilder(title).apply {
            val titleSpan = TypefaceSpan("sans-serif-medium")
            setSpan(titleSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val style = WeekViewEntity.Style.Builder()
            .setBackgroundColor(Color.parseColor(item.color.toString()))
            .build()
        return WeekViewEntity.Event.Builder(item)
            .setId(item.id!!.toLong())
            .setTitle(styledTitle)
            .setSubtitle(item.clientName)
            .setStyle(style)
            .setStartTime(startTime)
            .setEndTime(endTime)
            .build()
    }

    override fun onEventClick(data: Appointments) {
        super.onEventClick(data)
        val bundle = bundleOf("appointment_id" to data.id, "appointment_status" to data.appointmentStatus)
        Navigation.findNavController(view).navigate(R.id.appointmentDetailsFragment, bundle)
    }

    @SuppressLint("SimpleDateFormat")
    private fun timeFormatter(date: String, time: String): ArrayList<Int> {
        val year: Int = date.substring(0, 4).toInt()
        val month: Int = date.substring(5, 7).toInt()
        val day: Int = date.substring(8, 10).toInt()
        val hMMA = SimpleDateFormat("h:mm a")
        val hhMMSS = SimpleDateFormat("HH:mm")
        val simpleDate = hMMA.parse(time)
        val formattedTime = hhMMSS.format(simpleDate!!)
        val hour: Int = formattedTime.toString().substring(0, 2).toInt()
        val minute: Int = formattedTime.toString().substring(3, 5).toInt()
        var alphabeticMonth = 0
        when (month) {
            1 -> alphabeticMonth = Calendar.JANUARY
            2 -> alphabeticMonth = Calendar.FEBRUARY
            3 -> alphabeticMonth = Calendar.MARCH
            4 -> alphabeticMonth = Calendar.APRIL
            5 -> alphabeticMonth = Calendar.MAY
            6 -> alphabeticMonth = Calendar.JUNE
            7 -> alphabeticMonth = Calendar.JULY
            8 -> alphabeticMonth = Calendar.AUGUST
            9 -> alphabeticMonth = Calendar.SEPTEMBER
            10 -> alphabeticMonth = Calendar.OCTOBER
            11 -> alphabeticMonth = Calendar.NOVEMBER
            12 -> alphabeticMonth = Calendar.DECEMBER
        }
        return arrayListOf(year, alphabeticMonth, day, hour, minute)
    }
}