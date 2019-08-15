package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seunghyun.dimigospreadsheet.BuildConfig
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import com.seunghyun.dimigospreadsheet.utils.ViewModelFactory
import com.seunghyun.dimigospreadsheet.utils.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_teacher_spreadsheet.*
import kotlinx.android.synthetic.main.network_error_screen.view.*

class TeacherSpreadsheetActivity : AppCompatActivity() {
    private val service by lazy { SpreadsheetHelper.getService(this) }
    private val viewModels by lazy {
        ArrayList(listOf(
                ViewModelProviders.of(this, ViewModelFactory(service, 1))["1", SheetViewModel::class.java],
                ViewModelProviders.of(this, ViewModelFactory(service, 2))["2", SheetViewModel::class.java],
                ViewModelProviders.of(this, ViewModelFactory(service, 3))["3", SheetViewModel::class.java],
                ViewModelProviders.of(this, ViewModelFactory(service, 4))["4", SheetViewModel::class.java],
                ViewModelProviders.of(this, ViewModelFactory(service, 5))["5", SheetViewModel::class.java],
                ViewModelProviders.of(this, ViewModelFactory(service, 6))["6", SheetViewModel::class.java]
        ))
    }
    private val networkErrorCallback = object : NetworkErrorCallback {
        override fun onError(e: Exception?) {
            when (e) {
                null -> networkOk()
                is GoogleJsonResponseException -> networkError(SERVER_ERROR)
                else -> networkError(NETWORK_ERROR)
            }
        }
    }

    private val reference = FirebaseDatabase.getInstance().reference
    var isNeedUpdate = true
    val versionCode = BuildConfig.VERSION_CODE
    private val versionListener = object : ValueEventListener { //버전 낮으면 업데이트 화면 띄움
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value.toString().toInt() > versionCode) {
                isNeedUpdate = true
                val intent = Intent(this@TeacherSpreadsheetActivity, UpdateActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                isNeedUpdate = false
            }
        }
    }
    private val closedListener = object : ValueEventListener { //앱이 점검중이면 점검화면 띄움
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value.toString().toBoolean() && !isNeedUpdate) {
                val intent = Intent(this@TeacherSpreadsheetActivity, ClosingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_spreadsheet)

        init()
    }

    override fun onResume() {
        super.onResume()
        reference.child("app-version").addValueEventListener(versionListener)
        reference.child("isClosing").addValueEventListener(closedListener)
    }

    override fun onPause() {
        super.onPause()
        reference.child("app-version").removeEventListener(versionListener)
        reference.child("isClosing").removeEventListener(closedListener)
    }

    private fun init() {
        val classList = ArrayList<String>()
        classList.add(getString(R.string.summary))
        repeat(6) { classList.add("1학년 ${it + 1}반") }
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, classList)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        titleSpinner.adapter = adapter

        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, networkErrorCallback, viewModels, viewPager)
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
                    6 -> {
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
