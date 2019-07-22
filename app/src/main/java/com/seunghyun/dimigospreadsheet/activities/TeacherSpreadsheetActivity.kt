package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_teacher_spreadsheet.*

class TeacherSpreadsheetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_spreadsheet)

        viewPager.adapter = ViewPagerAdapter(this)
        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                titleTV.text = "1학년 ${position + 1}반"
                when (position) {
                    0 -> backButton.visibility = View.INVISIBLE
                    5 -> forwardButton.visibility = View.INVISIBLE
                    else -> {
                        backButton.visibility = View.VISIBLE
                        forwardButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewPager.addOnPageChangeListener(pageChangeListener)
        pageChangeListener.onPageSelected(0)

        backButton.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem - 1, true) }
        forwardButton.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
    }
}
