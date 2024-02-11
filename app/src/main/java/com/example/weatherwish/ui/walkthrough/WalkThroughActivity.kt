package com.example.weatherwish.ui.walkthrough

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherwish.R
import com.example.weatherwish.adapter.WalkthroughAdapter
import com.example.weatherwish.utils.Utils

class WalkThroughActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var tvPrevious: TextView
    private lateinit var tvNext: TextView

    private lateinit var walkthroughAdapter: WalkthroughAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_through)

        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)
        tvPrevious = findViewById(R.id.tv_previous)
        tvNext = findViewById(R.id.tv_next)
        walkthroughAdapter = WalkthroughAdapter(this, getWalkthroughPages())
        viewPager.adapter = walkthroughAdapter
        setupIndicator()
        setCurrentIndicator(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        tvPrevious.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        tvNext.setOnClickListener {
            if (viewPager.currentItem < walkthroughAdapter.itemCount - 1) {
                viewPager.currentItem += 1
            }
        }
    }

    private fun setupIndicator() {
        val indicators = arrayOfNulls<TextView>(walkthroughAdapter.itemCount)
        val margin = resources.getDimensionPixelSize(R.dimen.indicator_margin)

        for (i in indicators.indices) {
            indicators[i] = TextView(this)
            indicators[i]?.apply {
                text = "•"
                textSize = 50f
                setTextColor(resources.getColor(R.color.red))

                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(margin, 0, margin, 0)
                this@apply.layoutParams = layoutParams  // Use this@apply to refer to the outer apply
            }
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        Utils.printDebugLog("index:$index")
        if (index==0) {
            tvPrevious.visibility = View.GONE
        } else {
            tvPrevious.visibility = View.VISIBLE
        }
        val childCount = indicatorLayout.childCount
        if (index==childCount-1) {
            tvNext.text = "Start"
        } else {
            tvNext.text = "Next"
        }
        for (i in 0 until childCount) {
            val indicator = indicatorLayout.getChildAt(i) as TextView
            if (i == index) {
                indicator.setTextColor(resources.getColor(R.color.red))
            } else {
                indicator.setTextColor(resources.getColor(R.color.purple_200))
            }
        }
    }

    private fun getWalkthroughPages(): List<Fragment> {
        // Create and return your walkthrough fragments here
        return listOf(
            WalkthroughPageFragment("Page 1", "Description 1"),
            WalkthroughPageFragment("Page 2", "Description 2"),
            WalkthroughPageFragment("Page 3", "Description 3"),
            WalkthroughPageFragment("Page 4", "Description 4")
        )
    }

}