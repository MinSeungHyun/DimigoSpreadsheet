package com.seunghyun.dimigospreadsheet.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_spreadsheet.*

class SpreadsheetActivity : AppCompatActivity() {
    private var isRunning = false
    private var isShowing = false
    private val grade by lazy { intent.getIntExtra("grade", 0) }
    private val klass by lazy { intent.getIntExtra("class", 0) }
    private val service by lazy { MainActivity.getService(this@SpreadsheetActivity) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        isRunning = true
        title = "${grade}학년 ${klass}반"

        object : Thread() {
            override fun run() {
                while (isRunning) {
                    if (isShowing) {
                        try {
                            val counts = MainActivity.getValues(service, "${klass}반!B2:B4")
                            runOnUiThread {
                                totalTV.text = getString(R.string.total).format(counts?.get(0)?.get(0).toString().toInt())
                                vacancyTV.text = getString(R.string.vacancy).format(counts?.get(1)?.get(0).toString().toInt())
                                currentTV.text = getString(R.string.current).format(counts?.get(2)?.get(0).toString().toInt())
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
//                                testTV.setText(R.string.check_internet)
                            }
                        }
                    }
                    sleep(1000)
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
    }

    override fun onPause() {
        super.onPause()
        isShowing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Animatoo.animateSlideRight(this@SpreadsheetActivity)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                Animatoo.animateSlideRight(this@SpreadsheetActivity)
                return true
            }
        }
        return false
    }
}
