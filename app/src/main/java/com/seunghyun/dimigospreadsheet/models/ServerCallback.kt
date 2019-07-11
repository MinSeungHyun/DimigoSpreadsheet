package com.seunghyun.dimigospreadsheet.models

import com.seunghyun.dimigospreadsheet.models.Result

interface ServerCallback {
    fun onReceive(result: Result)
}