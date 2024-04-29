package com.shakir.weatherzoomer.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.shakir.weatherzoomer.R

object CustomProgressDialog {

    private var dialog: Dialog? = null

    fun showProgressDialog(context: Context, text: String) {
        if (dialog == null) {
            dialog = Dialog(context)
            dialog?.setContentView(R.layout.custom_progress_dialog)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.setCanceledOnTouchOutside(false)
            dialog?.setCancelable(false)
        }

        // Update the text
        dialog?.findViewById<TextView>(R.id.tv_message)?.text = text

        if (!dialog?.isShowing!!) {
            dialog?.show()
        }
    }

    fun dismissProgressDialog() {
        if (dialog?.isShowing!!) {
            dialog?.dismiss()
        }
    }
}