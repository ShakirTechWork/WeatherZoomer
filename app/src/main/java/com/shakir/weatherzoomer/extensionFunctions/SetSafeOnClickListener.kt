package com.shakir.weatherzoomer.extensionFunctions

import android.view.View
import com.shakir.weatherzoomer.SafeClickListener

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}