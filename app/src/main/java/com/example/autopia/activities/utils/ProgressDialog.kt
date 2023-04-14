package com.example.autopia.activities.utils

import android.app.Activity
import android.app.AlertDialog
import android.os.Handler
import android.os.Message
import com.example.autopia.R

class ProgressDialog(private val activity: Activity) {
    private lateinit var progressDialog: AlertDialog
    fun startLoading() {
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.progress_dialog, null)
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        progressDialog = builder.create()
        progressDialog.show()
        mHandler.sendMessageDelayed(Message(), 10000)
    }

    fun dismissLoading() {
        progressDialog.dismiss()
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (progressDialog.isShowing) progressDialog.dismiss()
        }
    }
}