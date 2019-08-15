package com.seunghyun.dimigospreadsheet.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SummaryViewModel
import kotlinx.android.synthetic.main.counts_card_prototype.view.*
import kotlinx.android.synthetic.main.fragment_summary.view.*

class SummaryFragment(private val networkErrorCallback: NetworkErrorCallback, private val viewModel: SummaryViewModel, private val viewPager: ViewPager) : Fragment() {
    lateinit var classes: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isRunning.value = true
        viewModel.context = requireContext()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_summary, container, false)

        initViews(parent)
        initModel()

        return parent
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isRunning.value = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        viewModel.isShowing.postValue(isVisibleToUser)
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
    private fun initViews(parent: View) {
        classes = listOf(parent.class1, parent.class2, parent.class3, parent.class4, parent.class5, parent.class6)
        repeat(6) { cnt: Int ->
            classes[cnt].classTV.text = "${cnt + 1}반"
            classes[cnt].setOnClickListener {
                viewPager.setCurrentItem(cnt + 1, true)
            }
        }
    }

    private fun initModel() {
        viewModel.networkError.observe(this, Observer {
            networkErrorCallback.onError(it)
        })

        viewModel.countsList.observe(this, Observer {
            repeat(6) { i ->
                classes[i].totalCountTV.text = it[i][0]
                classes[i].vacancyCountTV.text = it[i][1]
                classes[i].currentCountTV.text = it[i][2]
            }
        })
    }
}
