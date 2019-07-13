package com.seunghyun.dimigospreadsheet.models

interface GetSheetValueCallback {
    fun onReceive(values: List<List<Any>>?)
}