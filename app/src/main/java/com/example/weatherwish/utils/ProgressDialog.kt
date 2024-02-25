package com.example.weatherwish.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import com.example.weatherwish.R

object ProgressDialog {

    private var dialog: Dialog? = null
    private var isVisible = false

    fun initialize(context: Context) {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.custom_progress_dialog)
        dialog?.setCancelable(false)

        val progressBar = dialog?.findViewById<ProgressBar>(R.id.progress_bar)
        val messageTextView = dialog?.findViewById<TextView>(R.id.tv_message)
    }

    fun setProgressVisibility(visible: Boolean) {
        dialog?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility =
            if (visible) ProgressBar.VISIBLE else ProgressBar.GONE
    }

    fun show(message: String) {
        isVisible = true
        dialog?.findViewById<TextView>(R.id.tv_message)?.text = message
        dialog?.show()
    }

    fun dismiss() {
        if (isVisible) {
            dialog?.dismiss()
        }
    }
}