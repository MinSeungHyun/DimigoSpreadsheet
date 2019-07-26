package com.seunghyun.dimigospreadsheet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import kotlinx.android.synthetic.main.number_card_back.view.*
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import kotlinx.android.synthetic.main.spreadsheet_prototype.view.*

class SpreadsheetFragment(private val networkErrorCallback: NetworkErrorCallback, private val viewModel: SheetViewModel) : Fragment() {
    private lateinit var parent: View
    private var currentIngang1: ArrayList<String>? = null
    private var currentIngang2: ArrayList<String>? = null
    private var currentClub: ArrayList<String>? = null
    private var currentEtc: ArrayList<String>? = null
    private var currentBathroom: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isRunning.value = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parent = inflater.inflate(R.layout.spreadsheet_prototype, container, false)
        initModel()
        return parent
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isRunning.value = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Thread {
            while (!this@SpreadsheetFragment.isResumed);
            viewModel.isShowing.postValue(isVisibleToUser)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (userVisibleHint) viewModel.isShowing.postValue(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.isShowing.postValue(false)
    }

    @SuppressLint("SetTextI18n")
    private fun initModel() {
        viewModel.networkError.observe(this, Observer {
            networkErrorCallback.onError(it)
        })

        viewModel.totalCount.observe(this, Observer {
            parent.totalTV.text = getString(R.string.total) + it
        })
        viewModel.vacancyCount.observe(this, Observer {
            parent.vacancyTV.text = getString(R.string.vacancy) + it
        })
        viewModel.currentCount.observe(this, Observer {
            parent.currentTV.text = getString(R.string.current) + it
        })

        viewModel.ingang1List.observe(this, Observer {
            if (!isSameValues(it, currentIngang1)) {
                enterListToParent(parent.ingang1Layout.namesLayout, it)
                updateNumber(parent.ingang1Back, it.size)
            }
            currentIngang1 = it
        })
        viewModel.ingang2List.observe(this, Observer {
            if (!isSameValues(it, currentIngang2)) {
                enterListToParent(parent.ingang2Layout.namesLayout, it)
                updateNumber(parent.ingang2Back, it.size)
            }
            currentIngang2 = it
        })
        viewModel.clubList.observe(this, Observer {
            if (!isSameValues(it, currentClub)) {
                enterListToParent(parent.clubLayout.namesLayout, it)
                updateNumber(parent.clubBack, it.size)
            }
            currentClub = it
        })
        viewModel.etcList.observe(this, Observer {
            if (!isSameValues(it, currentEtc)) {
                enterListToParent(parent.etcLayout.namesLayout, it)
                updateNumber(parent.etcBack, it.size)
            }
            currentEtc = it
        })
        viewModel.bathroomList.observe(this, Observer {
            if (!isSameValues(it, currentBathroom)) {
                enterListToParent(parent.bathroomLayout.namesLayout, it)
                updateNumber(parent.bathroomBack, it.size)
            }
            currentBathroom = it
        })
    }

    private fun isSameValues(list1: ArrayList<String>, list2: ArrayList<String>?): Boolean {
        if (list2 == null || list1.size != list2.size) return false
        for (i in (0 until list1.size)) {
            if (list1[i] != list2[i]) return false
        }
        return true
    }

    private fun enterListToParent(parent: LinearLayout, names: ArrayList<String>) {
        repeat(parent.childCount - 1) { parent.removeViewAt(0) }
        names.forEach {
            val nameTV = layoutInflater.inflate(R.layout.name_item, parent, false) as TextView
            nameTV.text = it
            parent.addView(nameTV, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNumber(view: View, number: Int) {
        view.countTV.text = "${number}명"
    }
}