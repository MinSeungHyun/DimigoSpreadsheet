package com.seunghyun.dimigospreadsheet.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.seunghyun.dimigospreadsheet.models.SheetViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SheetViewModel() as T
    }
}