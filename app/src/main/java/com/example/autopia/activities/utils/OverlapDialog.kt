package com.example.autopia.activities.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.autopia.R

class OverlapDialog : DialogFragment() {
    private lateinit var layout: View
    private var positiveButtonText: String = "Ok"
    private var negativeButtonText: String = "Cancel"
    private var onCancelOption: () -> Unit = {}
    private var onPositiveOption: () -> Unit = {}
    private var title: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            layout = requireActivity()
                .layoutInflater.inflate(R.layout.overlap_dialog, null)
            builder.setView(layout)
            title?.let { title ->
                builder.setTitle(title)
            }
            builder.setPositiveButton(positiveButtonText) { _, _ -> onPositiveOption() }
                .setNegativeButton(negativeButtonText) { _, _ -> onCancelOption() }
            // Create the AlertDialog object and return it
            val buildObject = builder.create()
            buildObject.show()

            val positiveButton = buildObject.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.typeface = resources.getFont(R.font.open_sans)
            positiveButton.isAllCaps = false
            positiveButton.setTextColor(resources.getColor(R.color.indigo_300))
            val negativeButton = buildObject.getButton(DialogInterface.BUTTON_NEGATIVE)
            negativeButton.typeface = resources.getFont(R.font.open_sans)
            negativeButton.setTextColor(resources.getColor(R.color.black))
            negativeButton.isAllCaps = false

            buildObject
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setOnPositiveOption(text: String, onPositiveOption: () -> Unit) {
        this.onPositiveOption = onPositiveOption
        positiveButtonText = text
    }

    fun setOnCancelOption(
        text: String,
        onCancelOption: () -> Unit
    ) {
        this.onCancelOption = onCancelOption
        negativeButtonText = text
    }
}
