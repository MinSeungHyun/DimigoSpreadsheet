package com.seunghyun.dimigospreadsheet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SummaryViewModel
import kotlinx.android.synthetic.main.counts_card_prototype.view.*
import kotlinx.android.synthetic.main.fragment_summary.view.*

class SummaryFragment(private val networkErrorCallback: NetworkErrorCallback, private val viewModel: SummaryViewModel, private val viewPager: ViewPager) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_summary, container, false)

        initViews(parent)

        return parent
    }

    @SuppressLint("SetTextI18n")
    private fun initViews(parent: View) {
        val classes = listOf(parent.class1, parent.class2, parent.class3, parent.class4, parent.class5, parent.class6)
        repeat(6) { cnt: Int ->
            classes[cnt].classTV.text = "${cnt + 1}반"
            classes[cnt].setOnClickListener {
                viewPager.setCurrentItem(cnt + 2, true)
            }
        }
    }
}
