package com.shakir.weatherzoomer.ui.walkthrough

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.shakir.weatherzoomer.R
class WalkthroughPageFragment(
    private val image: Int,
    private val title: String,
    private val description: String
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_walk_through_page, container, false)

        // Customize the UI elements based on the title and description

        val imgItem: ImageView = view.findViewById(R.id.img_item)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvSubTitle: TextView = view.findViewById(R.id.tv_sub_title)

        imgItem.load(image)
        tvTitle.text = title
        tvSubTitle.text = description

        return view
    }
}