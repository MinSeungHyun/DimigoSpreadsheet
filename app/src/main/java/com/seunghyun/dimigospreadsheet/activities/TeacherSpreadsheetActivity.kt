package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_teacher_spreadsheet.*
import kotlinx.android.synthetic.main.network_error_screen.view.*

class TeacherSpreadsheetActivity : AppCompatActivity() {
    private val networkErrorCallback = object : NetworkErrorCallback {
        override fun onError(e: Exception?) {
            when (e) {
                null -> networkOk()
                is GoogleJsonResponseException -> networkError(SERVER_ERROR)
                else -> networkError(NETWORK_ERROR)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_spreadsheet)

        init()
    }

    private fun init() {
        val classList = ArrayList<String>()
        repeat(6) { classList.add("1학년 ${it + 1}반") }
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, classList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        titleSpinner.adapter = adapter

        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, networkErrorCallback)
        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                titleSpinner.setSelection(position)
                when (position) {
                    0 -> {
                        backButton.visibility = View.INVISIBLE
                        forwardButton.visibility = View.VISIBLE
                    }
                    5 -> {
                        forwardButton.visibility = View.INVISIBLE
                        backButton.visibility = View.VISIBLE
                    }
                    else -> {
                        backButton.visibility = View.VISIBLE
                        forwardButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewPager.addOnPageChangeListener(pageChangeListener)
        pageChangeListener.onPageSelected(0)

        titleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewPager.setCurrentItem(position, true)
            }
        }
        backButton.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem - 1, true) }
        forwardButton.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
    }

    private fun networkError(error: Int) {
        runOnUiThread {
            if (error == NETWORK_ERROR) checkInternetLayout.errorTV.setText(R.string.check_internet)
            else checkInternetLayout.errorTV.setText(R.string.server_error)
            checkInternetLayout.visibility = View.VISIBLE
        }
    }

    private fun networkOk() {
        runOnUiThread {
            checkInternetLayout.visibility = View.GONE
        }
    }

    companion object {
        const val NETWORK_ERROR = 0
        const val SERVER_ERROR = 1
    }
}
