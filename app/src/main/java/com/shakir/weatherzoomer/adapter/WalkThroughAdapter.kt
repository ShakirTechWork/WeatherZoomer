package com.shakir.weatherzoomer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class WalkthroughAdapter(
    fragmentActivity: FragmentActivity,
    private val walkthroughPages: List<Fragment>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return walkthroughPages.size
    }

    override fun createFragment(position: Int): Fragment {
        return walkthroughPages[position]
    }
}