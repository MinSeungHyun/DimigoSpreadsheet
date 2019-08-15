package com.seunghyun.dimigospreadsheet.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper

class SummaryViewModel : ViewModel() {
    lateinit var context: Context

    val isRunning by lazy { MutableLiveData<Boolean>() }
    val isShowing by lazy { MutableLiveData<Boolean>() }
    val networkError by lazy { MutableLiveData<Exception?>() }

    private var mIsRunning = false
    private var mIsShowing = false

    val countsList by lazy { MutableLiveData<ArrayList<ArrayList<String>>>() }

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
                            val values = SpreadsheetHelper.getValues(SpreadsheetHelper.getService(context), "사용 안내!B1:F12")
                            networkError.postValue(null)
                            val counts = ArrayList<ArrayList<String>>()

                            counts.add(ArrayList(listOf(
                                    values?.get(1)?.get(1).toString(),
                                    values?.get(2)?.get(1).toString(),
                                    values?.get(3)?.get(1).toString())))
                            counts.add(ArrayList(listOf(
                                    values?.get(1)?.get(3).toString(),
                                    values?.get(2)?.get(3).toString(),
                                    values?.get(3)?.get(3).toString())))
                            counts.add(ArrayList(listOf(
                                    values?.get(5)?.get(1).toString(),
                                    values?.get(6)?.get(1).toString(),
                                    values?.get(7)?.get(1).toString())))
                            counts.add(ArrayList(listOf(
                                    values?.get(5)?.get(3).toString(),
                                    values?.get(6)?.get(3).toString(),
                                    values?.get(7)?.get(3).toString())))
                            counts.add(ArrayList(listOf(
                                    values?.get(9)?.get(1).toString(),
                                    values?.get(10)?.get(1).toString(),
                                    values?.get(11)?.get(1).toString())))
                            counts.add(ArrayList(listOf(
                                    values?.get(9)?.get(3).toString(),
                                    values?.get(10)?.get(3).toString(),
                                    values?.get(11)?.get(3).toString())))

                            countsList.postValue(counts)
                        } catch (e: GoogleJsonResponseException) {
                            networkError.postValue(e)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            networkError.postValue(e)
                        }
                        sleep(2000)
                    }
                }
            }
        }.start()
    }

}