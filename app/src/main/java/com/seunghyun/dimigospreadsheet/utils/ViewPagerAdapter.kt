package com.seunghyun.dimigospreadsheet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.ViewPager
import com.seunghyun.dimigospreadsheet.fragments.SpreadsheetFragment
import com.seunghyun.dimigospreadsheet.fragments.SummaryFragment
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import com.seunghyun.dimigospreadsheet.models.SummaryViewModel

class ViewPagerAdapter(fm: FragmentManager, private val networkErrorCallback: NetworkErrorCallback, private val viewModels: ArrayList<ViewModel>,
                       private val viewPager: ViewPager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return if (position == 0) SummaryFragment(networkErrorCallback, viewModels[position] as SummaryViewModel, viewPager)
        else SpreadsheetFragment(networkErrorCallback, viewModels[position] as SheetViewModel)
    }

    override fun getCount(): Int {
        return 7
    }
}