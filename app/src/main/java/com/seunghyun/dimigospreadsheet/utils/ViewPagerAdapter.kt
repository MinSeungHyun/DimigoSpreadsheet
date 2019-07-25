package com.seunghyun.dimigospreadsheet.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.seunghyun.dimigospreadsheet.fragments.SpreadsheetFragment
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel

class ViewPagerAdapter(fm: FragmentManager, private val networkErrorCallback: NetworkErrorCallback, private val viewModels: ArrayList<SheetViewModel>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return SpreadsheetFragment(networkErrorCallback, viewModels[position])
    }

    override fun getCount(): Int {
        return 6
    }
}