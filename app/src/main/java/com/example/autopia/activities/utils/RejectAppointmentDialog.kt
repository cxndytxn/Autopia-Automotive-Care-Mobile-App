package com.example.autopia.activities.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.autopia.R

class RejectAppointmentDialog(private val bundle: Bundle, private val cantReschedule: Boolean?) :
    DialogFragment() {
    private lateinit var layout: View
    private var positiveButtonText: String = "Ok"
    private var negativeButtonText: String = "Cancel"
    private var onCancelOption: (isReasonFilled: Boolean, reason: String) -> Unit = { _, _ -> }
    private var onPositiveOption: () -> Unit = {}
    private var title: String? = null
    private lateinit var editText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            layout = requireActivity()
                .layoutInflater.inflate(R.layout.reject_appointment_dialog, null)
            setupLayout()
            builder.setView(layout)
            title?.let { title ->
                builder.setTitle(title)
            }
            builder.setPositiveButton(positiveButtonText) { _, _ ->
                if (cantReschedule == true)
                    Toast.makeText(
                        context,
                        "Reschedule request is only allowed at least 2 days before the actual appointment date!",
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    findNavController().navigate(R.id.rescheduleAppointmentsFragment, bundle)
            }
                .setNegativeButton(negativeButtonText) { _, _ ->
                    if (dialog?.isShowing == true) {
                        val reason = editText.text.toString()
                        val bool: Boolean = reason != ""
                        onCancelOption(bool, reason)
                    }
                }
            // Create the AlertDialog object and return it
            val buildObject = builder.create()
            buildObject.show()

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

    private fun setupLayout() {
        bindViews()
    }

    private fun bindViews() {
        editText = layout.findViewById(R.id.reject_reason)
    }

    fun setOnPositiveOption(text: String, onPositiveOption: () -> Unit) {
        this.onPositiveOption = onPositiveOption
        positiveButtonText = text
    }

    fun setOnCancelOption(
        text: String,
        onCancelOption: (isReasonFilled: Boolean, reason: String) -> Unit
    ) {
        this.onCancelOption = onCancelOption
        negativeButtonText = text
    }
}
