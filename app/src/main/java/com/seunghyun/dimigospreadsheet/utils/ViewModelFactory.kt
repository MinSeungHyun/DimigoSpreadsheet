package com.seunghyun.dimigospreadsheet.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.api.services.sheets.v4.Sheets
import com.seunghyun.dimigospreadsheet.models.SheetViewModel

class ViewModelFactory(private val service: Sheets, private val klass: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SheetViewModel(service, klass) as T
    }
}