package com.example.autopia.activities.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import com.example.autopia.R

class ProposeDurationDialog : DialogFragment() {
    private lateinit var timePickerLayout: View
    private lateinit var hourPicker: com.shawnlin.numberpicker.NumberPicker
    private lateinit var minPicker: com.shawnlin.numberpicker.NumberPicker

    private var onTimeSetOption:
                (hour: Int, minute: Int) -> Unit = { _, _ -> }
    private var timeSetText: String = "Ok"

    private var onCancelOption: () -> Unit = {}
    private var cancelText: String = "Cancel"

    var initialHour: Int = 0
    var initialMinute: Int = 0
    var maxValueHour: Int = 23
    var maxValueMinute: Int = 59
    var minValueHour: Int = 0
    var minValueMinute: Int = 0
    var includeHours: Boolean = true
    private var title: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            timePickerLayout = requireActivity()
                .layoutInflater.inflate(R.layout.propose_duration_dialog, null)

            setupTimePickerLayout()

            builder.setView(timePickerLayout)
            title?.let { title ->
                builder.setTitle(title)
            }
            builder.setPositiveButton(timeSetText) { _, _ ->
                var hour = hourPicker.value
                if (!includeHours) hour = 0
                onTimeSetOption(hour, minPicker.value)
            }
                .setNegativeButton(cancelText) { _, _ ->
                    onCancelOption
                }
            val buildObject = builder.create()
            buildObject.show()
            // Create the AlertDialog object and return it
            val positiveButton = buildObject.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.typeface = resources.getFont(R.font.open_sans)
            positiveButton.isAllCaps = false
            positiveButton.setTextColor(resources.getColor(R.color.indigo_300))
            val negativeButton = buildObject.getButton(DialogInterface.BUTTON_NEGATIVE)
            negativeButton.typeface = resources.getFont(R.font.open_sans)
            negativeButton.isAllCaps = false

            buildObject
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setOnTimeSetOption(text: String, onTimeSet: (hour: Int, minute: Int) -> Unit) {
        onTimeSetOption = onTimeSet
        timeSetText = text
    }

    fun setOnCancelOption(text: String, onCancelOption: () -> Unit) {
        this.onCancelOption = onCancelOption
        cancelText = text
    }

    private fun setupTimePickerLayout() {
        bindViews()

        setupMaxValues()
        setupMinValues()
        setupInitialValues()

        if (!includeHours) {
            timePickerLayout.findViewById<LinearLayout>(R.id.hours_container)
                .visibility = View.GONE
        }
    }

    private fun bindViews() {
        hourPicker = timePickerLayout.findViewById(R.id.hours)
        minPicker = timePickerLayout.findViewById(R.id.minutes)
    }

    private fun setupMaxValues() {
        hourPicker.maxValue = maxValueHour
        minPicker.maxValue = maxValueMinute
    }

    private fun setupMinValues() {
        hourPicker.minValue = minValueHour
        minPicker.minValue = minValueMinute
    }

    private fun setupInitialValues() {
        hourPicker.value = initialHour
        minPicker.value = initialMinute
    }
}