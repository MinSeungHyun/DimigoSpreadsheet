package com.seunghyun.dimigospreadsheet.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper

class SpreadsheetState : ViewModel() {
    val isShowing by lazy { MutableLiveData<Boolean>() }
    val isRunning by lazy { MutableLiveData<Boolean>() }

    val networkError by lazy { MutableLiveData<Exception?>() }

    val totalCount by lazy { MutableLiveData<String>() }
    val vacancyCount by lazy { MutableLiveData<String>() }
    val currentCount by lazy { MutableLiveData<String>() }

    val ingang1List by lazy { MutableLiveData<ArrayList<String>>() }
    val ingang2List by lazy { MutableLiveData<ArrayList<String>>() }
    val clubList by lazy { MutableLiveData<ArrayList<String>>() }
    val etcList by lazy { MutableLiveData<ArrayList<String>>() }
    val bathroomList by lazy { MutableLiveData<ArrayList<String>>() }

    private var mIsRunning = false
    private var mIsShowing = false

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
        startUpdate()
    }

    private fun startUpdate() {
        object : Thread() {
            override fun run() {
                while (mIsRunning) {
                    while (mIsShowing) {
                        try {
                            val sheetValue = SheetValue(SpreadsheetHelper.getValues(service!!, "${klass!!}반!1:30"))
                            networkError.postValue(null)
                            if (totalCount.value == null || totalCount.value != sheetValue.totalCount) totalCount.postValue(sheetValue.totalCount)
                            if (vacancyCount.value == null || vacancyCount.value != sheetValue.vacancyCount) vacancyCount.postValue(sheetValue.vacancyCount)
                            if (currentCount.value == null || currentCount.value != sheetValue.currentCount) currentCount.postValue(sheetValue.currentCount)
                            ingang1List.postValue(sheetValue.ingang1)
                            ingang2List.postValue(sheetValue.ingang2)
                            clubList.postValue(sheetValue.club)
                            etcList.postValue(sheetValue.etc)
                            bathroomList.postValue(sheetValue.bathroom)
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

    companion object {
        var service: Sheets? = null
        var klass: Int? = null
    }
}
