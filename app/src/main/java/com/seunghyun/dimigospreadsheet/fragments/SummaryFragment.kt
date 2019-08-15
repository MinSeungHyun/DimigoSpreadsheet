package com.seunghyun.dimigospreadsheet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.counts_card_prototype.view.*
import kotlinx.android.synthetic.main.fragment_summary.view.*

class SummaryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_summary, container, false)

        initViews(parent)

        return parent
    }

    @SuppressLint("SetTextI18n")
    private fun initViews(parent: View) {
        val classes = listOf(parent.class1, parent.class2, parent.class3, parent.class4, parent.class5, parent.class6)
        repeat(6) { classes[it].classTV.text = "${it + 1}반" }
    }
}
