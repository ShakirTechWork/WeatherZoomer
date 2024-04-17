package com.example.weatherwish.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.weatherwish.R

object GifProgressDialog {

    private var dialog: Dialog? = null
    private var isVisible = false

    fun initialize(context: Context) {
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.custom_progress_gif_dialog)
        dialog?.setCancelable(false)
    }

    fun show(message: String) {
        dialog?.findViewById<TextView>(R.id.tv_message)?.text = message
        val imageView = dialog?.findViewById<ImageView>(R.id.progress_gif)
        Glide.with(imageView!!)
            .load(R.drawable.logo_animation_loop) // Replace with the path to your GIF
            .into(imageView)
        dialog?.show()
        isVisible = true
    }

    fun dismiss() {
        if (isVisible && dialog != null) {
            val imageView = dialog?.findViewById<ImageView>(R.id.progress_gif)
            Glide.with(imageView!!)
                .clear(imageView)
            dialog?.dismiss()
            dialog = null
            isVisible = false
        }
    }
}