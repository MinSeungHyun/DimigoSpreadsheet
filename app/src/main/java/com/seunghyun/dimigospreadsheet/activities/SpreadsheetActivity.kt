package com.seunghyun.dimigospreadsheet.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.seunghyun.dimigospreadsheet.R

class SpreadsheetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        val grade = intent.getIntExtra("grade", 0)
        val klass = intent.getIntExtra("class", 0)
        title = "${grade}학년 ${klass}반"
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
