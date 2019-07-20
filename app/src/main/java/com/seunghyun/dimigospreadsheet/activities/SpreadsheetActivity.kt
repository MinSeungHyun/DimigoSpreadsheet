package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SheetValue
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import java.lang.Thread.sleep

class SpreadsheetActivity : AppCompatActivity() {
    private var isRunning = false
    private var isShowing = false
    private val name by lazy { intent.getStringExtra("name") }
    private val studentId by lazy { intent.getStringExtra("studentId") }
    private val grade by lazy { intent.getIntExtra("grade", 0) }
    private val klass by lazy { intent.getIntExtra("class", 0) }
    private val service by lazy { MainActivity.getService(this@SpreadsheetActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        isRunning = true
        title = "${grade}학년 ${klass}반"

        ingang1Layout.typeTV.setText(R.string.ingang1)
        ingang2Layout.typeTV.setText(R.string.ingang2)
        clubLayout.typeTV.setText(R.string.club)
        etcLayout.typeTV.setText(R.string.etc)
        bathroomLayout.typeTV.setText(R.string.bathroom)

        object : Thread() {
            override fun run() {
                updateSheetValues()
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

    private fun enterListToParent(parent: LinearLayout, names: ArrayList<String>) {
        parent.removeAllViews()
        names.forEach {
            val nameTV = layoutInflater.inflate(R.layout.name_item, parent, false) as TextView
            nameTV.text = it
            parent.addView(nameTV)
        }
    }

    private fun isSameValues(list1: ArrayList<String>, list2: ArrayList<String>): Boolean {
        return list1.size == list2.size && list1.containsAll(list2)
    }

    @SuppressLint("SetTextI18n")
    private fun updateSheetValues() {
        var oldIngang1: ArrayList<String>? = null
        var oldIngang2: ArrayList<String>? = null
        var oldClub: ArrayList<String>? = null
        var oldEtc: ArrayList<String>? = null
        var oldBathroom: ArrayList<String>? = null
        while (isRunning) {
            if (isShowing) {
                try {
                    val sheetValue = SheetValue(MainActivity.getValues(service, "${klass}반!1:30"))
                    runOnUiThread {
                        checkInternetLayout.visibility = View.GONE
                        totalTV.text = getString(R.string.total) + sheetValue.totalCount
                        vacancyTV.text = getString(R.string.vacancy) + sheetValue.vacancyCount
                        currentTV.text = getString(R.string.current) + sheetValue.currentCount
                        if (oldIngang1 == null || !isSameValues(oldIngang1!!, sheetValue.ingang1)) enterListToParent(ingang1Layout.namesLayout, sheetValue.ingang1)
                        if (oldIngang2 == null || !isSameValues(oldIngang2!!, sheetValue.ingang2)) enterListToParent(ingang2Layout.namesLayout, sheetValue.ingang2)
                        if (oldClub == null || !isSameValues(oldClub!!, sheetValue.club)) enterListToParent(clubLayout.namesLayout, sheetValue.club)
                        if (oldEtc == null || !isSameValues(oldEtc!!, sheetValue.etc)) enterListToParent(etcLayout.namesLayout, sheetValue.etc)
                        if (oldBathroom == null || !isSameValues(oldBathroom!!, sheetValue.bathroom)) enterListToParent(bathroomLayout.namesLayout, sheetValue.bathroom)

                        oldIngang1 = sheetValue.ingang1
                        oldIngang2 = sheetValue.ingang2
                        oldClub = sheetValue.club
                        oldEtc = sheetValue.etc
                        oldBathroom = sheetValue.bathroom
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        checkInternetLayout.visibility = View.VISIBLE
                    }
                }
            }
            sleep(1000)
        }
    }
}
