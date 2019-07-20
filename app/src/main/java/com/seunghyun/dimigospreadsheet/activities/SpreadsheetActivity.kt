package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SheetValue
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.enter_name_bottomsheet.view.*
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import java.lang.Thread.sleep

class SpreadsheetActivity : AppCompatActivity() {
    private var isRunning = false
    private var isShowing = false
    private val name by lazy { intent.getStringExtra("name") }
    private val studentId by lazy { intent.getStringExtra("studentId") }
    private val grade by lazy { intent.getIntExtra("grade", 0) }
    private val klass by lazy { intent.getIntExtra("class", 0) }
    private val number by lazy { studentId.substring(2, 4).toInt() }
    private val names by lazy { intent.getStringArrayExtra("names") }
    private val service by lazy { SpreadsheetHelper.getService(this@SpreadsheetActivity) }

    private val updateCallback = object : UpdateSheetValueCallback {
        override fun onReceive(values: MutableCollection<Any>?) {
            runOnUiThread {
                if (values != null) {
                    bottomSheet.enterButton.doneLoadingAnimation(Color.GREEN, getDrawable(R.drawable.ic_baseline_check_24px)!!.toBitmap())
                    Handler().postDelayed({
                        bottomSheet.enterButton.revertAnimation()
                    }, 1000)
                } else {
                    Toast.makeText(applicationContext, R.string.enter_failed, Toast.LENGTH_LONG).show()
                    bottomSheet.enterButton.revertAnimation()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        isRunning = true
        title = "${grade}학년 ${klass}반"

        initSheet()
        initBottomSheet()

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

    private fun initSheet() {
        ingang1Layout.typeTV.setText(R.string.ingang1)
        ingang2Layout.typeTV.setText(R.string.ingang2)
        clubLayout.typeTV.setText(R.string.club)
        etcLayout.typeTV.setText(R.string.etc)
        bathroomLayout.typeTV.setText(R.string.bathroom)
    }

    private fun initBottomSheet() {
        val arrayAdapter = ArrayAdapter(this@SpreadsheetActivity, android.R.layout.simple_spinner_dropdown_item, names)
        bottomSheet.nameSpinner.adapter = arrayAdapter
        bottomSheet.nameSpinner.setSelection(number - 1)
        bottomSheet.enterDescriptionTV.text = getString(R.string.enter_description).format(grade, klass)

        bottomSheet.typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 3) {
                    bottomSheet.reasonInputLayout.visibility = View.VISIBLE
                } else {
                    bottomSheet.reasonInputLayout.error = ""
                    bottomSheet.reasonInputET.setText("")
                    bottomSheet.reasonInputLayout.visibility = View.GONE
                }
            }
        }

        bottomSheet.enterButton.setOnClickListener {
            if (bottomSheet.typeSpinner.selectedItem.toString() == "기타" && bottomSheet.reasonInputET.text.toString().isBlank()) {
                bottomSheet.reasonInputLayout.error = getString(R.string.enter_reason)
            } else {
                bottomSheet.reasonInputLayout.error = ""
                bottomSheet.enterButton.startAnimation {
                    EnterName(service, klass, bottomSheet.typeSpinner.selectedItem.toString(), bottomSheet.nameSpinner.selectedItem.toString(), bottomSheet.reasonInputET.text.toString(), updateCallback).start()
                }
            }
        }
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
                    val sheetValue = SheetValue(SpreadsheetHelper.getValues(service, "${klass}반!1:30"))
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

    private class EnterName(val service: Sheets, val klass: Int, val type: String, val name_: String, val reason: String, val callback: UpdateSheetValueCallback) : Thread() {
        override fun run() {
            try {
                var range = ""
                when (type) {
                    "인강실 (1타임)" -> range = "${klass}반!C2:C30"
                    "인강실 (2, 3타임)" -> range = "${klass}반!D2:D30"
                    "동아리" -> range = "${klass}반!E2:E30"
                    "기타" -> range = "${klass}반!F2:F30"
                }
                val currentList = SpreadsheetHelper.getValues(service, range)
                val size = currentList?.size ?: 0

                when (type) {
                    "인강실 (1타임)" -> range = "${klass}반!C${2 + size}"
                    "인강실 (2, 3타임)" -> range = "${klass}반!D${2 + size}"
                    "동아리" -> range = "${klass}반!E${2 + size}"
                    "기타" -> range = "${klass}반!F${2 + size}"
                }

                val values = if (reason.isBlank()) {
                    ValueRange().setValues(listOf(listOf(name_)))
                } else {
                    ValueRange().setValues(listOf(listOf("$reason - $name_")))
                }
                val result = SpreadsheetHelper.updateValues(service, range, values)
                callback.onReceive(result)
            } catch (e: Exception) {
                callback.onReceive(null)
            }
        }
    }
}
