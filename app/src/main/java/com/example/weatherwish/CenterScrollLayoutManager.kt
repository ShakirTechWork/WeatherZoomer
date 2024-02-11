package com.example.weatherwish

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CenterScrollLayoutManager(private val context: Context, orientation: Int, reverselayout: Boolean): LinearLayoutManager(context, orientation, reverselayout) {


    override fun scrollToPosition(position: Int) {
        super.scrollToPosition(position)
        //this will place the top of the item at the center of the screen
        val width = context.resources.displayMetrics.widthPixels
        val offset = width/2

        //if you know the item height, you can place the center of the item at the center of the screen
        //  by subtracting half the height of that item from the offset:
//        val height = getapplicationcontext().resources.displaymetrics.heightpixels
//        //(say item is 40dp tall)
//        val itemheight = 40f * getapplicationcontext().resources.displaymetrics.scaleddensity
//        val offset = height/2 - itemheight/2

        //depending on if you have a toolbar or other headers above the recyclerview,
        //  you may want to subtract their height as well:
//        val height = getapplicationcontext().resources.displaymetrics.heightpixels
//        //(say item is 40dp tall):
//        val itemheight = 40f * getapplicationcontext().resources.displaymetrics.scaleddensity
//        //(say toolbar is 56dp tall, which is the default action bar height for portrait mode)
//        val toolbarheight = 56f * getapplicationcontext().resources.displaymetrics.scaleddensity
//        val offset = height/2 - itemheight/2 - toolbarheight

        //call scrolltopositionwithoffset with the desired offset
        super.scrollToPositionWithOffset(position, offset)
    }
}