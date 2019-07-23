package com.seunghyun.dimigospreadsheet.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import com.seunghyun.dimigospreadsheet.utils.ViewModelFactory

class SpreadsheetFragment(private val klass: Int) : Fragment() {
    private val viewModel by lazy {
        ViewModelProviders.of(this, ViewModelFactory(SpreadsheetHelper.getService(requireContext()), klass))[klass.toString(), SheetViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isRunning.value = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.spreadsheet_prototype, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.isShowing.value = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.isShowing.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isRunning.value = false
    }
}