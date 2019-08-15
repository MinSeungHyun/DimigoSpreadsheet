package com.seunghyun.dimigospreadsheet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.seunghyun.dimigospreadsheet.fragments.SpreadsheetFragment
import com.seunghyun.dimigospreadsheet.fragments.SummaryFragment
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel

class ViewPagerAdapter(fm: FragmentManager, private val networkErrorCallback: NetworkErrorCallback, private val viewModels: ArrayList<SheetViewModel>,
                       private val viewPager: ViewPager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return if (position == 0) SummaryFragment(viewPager)
        else SpreadsheetFragment(networkErrorCallback, viewModels[position - 1])
    }

    override fun getCount(): Int {
        return 7
    }
}