package com.seunghyun.dimigospreadsheet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.seunghyun.dimigospreadsheet.fragments.SpreadsheetFragment
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback

class ViewPagerAdapter(fm: FragmentManager, private val networkErrorCallback: NetworkErrorCallback) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return SpreadsheetFragment(position + 1, networkErrorCallback)
    }

    override fun getCount(): Int {
        return 6
    }
}