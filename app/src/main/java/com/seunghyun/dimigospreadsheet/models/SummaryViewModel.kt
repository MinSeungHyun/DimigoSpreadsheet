package com.seunghyun.dimigospreadsheet.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.googleapis.json.GoogleJsonResponseException

class SummaryViewModel : ViewModel() {
    val isRunning by lazy { MutableLiveData<Boolean>() }
    val isShowing by lazy { MutableLiveData<Boolean>() }
    val networkError by lazy { MutableLiveData<Exception?>() }

    private var mIsRunning = false
    private var mIsShowing = false

    val countsList by lazy { MutableLiveData<List<List<Int>>>() }

    init {
        isRunning.observeForever { isRunning ->
            mIsRunning = isRunning
            if (isRunning) {
                startUpdate()
            }
        }
        isShowing.observeForever { isShowing ->
            mIsShowing = isShowing
        }
    }

    private fun startUpdate() {
        object : Thread() {
            override fun run() {
                while (mIsRunning) {
                    while (mIsShowing) {
                        try {
                            networkError.postValue(null)
                        } catch (e: GoogleJsonResponseException) {
                            networkError.postValue(e)
                        } catch (e: Exception) {
                            networkError.postValue(e)
                        }
                        sleep(2000)
                    }
                }
            }
        }.start()
    }

}