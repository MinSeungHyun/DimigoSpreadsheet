package com.seunghyun.dimigospreadsheet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.seunghyun.dimigospreadsheet.fragments.SpreadsheetFragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return SpreadsheetFragment(position + 1)
    }

    override fun getCount(): Int {
        return 6
    }
}