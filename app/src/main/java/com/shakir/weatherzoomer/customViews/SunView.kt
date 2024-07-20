package com.shakir.weatherzoomer.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SunView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var sunColor: Int = Color.YELLOW // Default color is yellow
    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width.coerceAtMost(height) / 2f

        paint.color = sunColor
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    fun setSunColor(color: Int) {
        sunColor = color
        invalidate() // Redraw the view with the new color
    }
}
