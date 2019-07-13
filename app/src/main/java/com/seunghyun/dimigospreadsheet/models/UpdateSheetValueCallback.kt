package com.seunghyun.dimigospreadsheet.models

interface UpdateSheetValueCallback {
    fun onReceive(values: MutableCollection<Any>?)
}