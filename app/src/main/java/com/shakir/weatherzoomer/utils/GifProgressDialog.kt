package com.shakir.weatherzoomer.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.shakir.weatherzoomer.R

object GifProgressDialog {

    private var dialog: Dialog? = null

    fun show(context: Context, message: String) {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.custom_progress_gif_dialog)
        dialog?.setCancelable(false)
        dialog?.findViewById<TextView>(R.id.tv_message)?.text = message
        val imageView = dialog?.findViewById<ImageView>(R.id.progress_gif)
        Glide.with(imageView!!)
            .load(R.drawable.logo_animation_loop) // Replace with the path to your GIF
            .into(imageView)
        dialog?.show()
    }

    fun dismiss() {
        if (dialog?.isShowing == true) {
            val imageView = dialog?.findViewById<ImageView>(R.id.progress_gif)
            Glide.with(imageView!!)
                .clear(imageView)
            dialog?.dismiss()
            dialog = null
        }
    }
}