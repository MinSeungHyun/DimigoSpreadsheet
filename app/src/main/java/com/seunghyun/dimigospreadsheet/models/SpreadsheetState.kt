package com.seunghyun.dimigospreadsheet.models

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpreadsheetState : ViewModel() {
    val isShowing by lazy { MutableLiveData<Boolean>() }
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val totalCount by lazy { MutableLiveData<Int>() }
    val vacancyCount by lazy { MutableLiveData<Int>() }
    val currentCount by lazy { MutableLiveData<Int>() }

    val Ingang1 by lazy { MutableLiveData<ArrayList<String>>() }
    val Ingang2 by lazy { MutableLiveData<ArrayList<String>>() }
    val Club by lazy { MutableLiveData<ArrayList<String>>() }
    val Etc by lazy { MutableLiveData<ArrayList<String>>() }
    val Bathroom by lazy { MutableLiveData<ArrayList<String>>() }

    private var mIsRunning = false
    private var mIsShowing = false

    init {
        isRunning.observeForever { isRunning ->
            Log.d("testing", "isRunning: $isRunning")
            mIsRunning = isRunning
            if (isRunning) {
                startUpdate()
            }
        }
        isShowing.observeForever { isShowing ->
            Log.d("testing", "isShowing: $isShowing")
            mIsShowing = isShowing
        }
        startUpdate()
    }

    private fun startUpdate() {
        object : Thread() {
            override fun run() {
                while (mIsRunning) {
                    while (mIsShowing) {
                    }
                }
            }
        }.start()
    }
}
