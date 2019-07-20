package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seunghyun.dimigospreadsheet.BuildConfig
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SheetValue
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.enter_name_bottomsheet.*
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

    private val reference = FirebaseDatabase.getInstance().reference
    var isNeedUpdate = true
    val versionCode = BuildConfig.VERSION_CODE
    private val versionListener = object : ValueEventListener { //버전 낮으면 업데이트 화면 띄움
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value.toString().toInt() > versionCode) {
                isNeedUpdate = true
                val intent = Intent(this@SpreadsheetActivity, UpdateActivity::class.java)
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
                val intent = Intent(this@SpreadsheetActivity, ClosingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }

    private val updateCallback = object : UpdateSheetValueCallback {
        override fun onReceive(values: MutableCollection<Any>?) {
            runOnUiThread {
                if (values != null) {
                    networkOk()
                    enterButton.doneLoadingAnimation(Color.GREEN, getDrawable(R.drawable.ic_baseline_check_24px)!!.toBitmap())
                    Handler().postDelayed({
                        enterButton.revertAnimation()
                    }, 1000)
                } else {
                    networkError()
                    enterButton.revertAnimation()
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
        reference.child("app-version").addValueEventListener(versionListener)
        reference.child("isClosing").addValueEventListener(closedListener)
    }

    override fun onPause() {
        super.onPause()
        isShowing = false
        reference.child("app-version").removeEventListener(versionListener)
        reference.child("isClosing").removeEventListener(closedListener)
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
        nameSpinner.adapter = arrayAdapter
        nameSpinner.setSelection(number - 1)
        enterDescriptionTV.text = getString(R.string.enter_description).format(grade, klass)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 3) {
                    reasonInputLayout.visibility = View.VISIBLE
                } else {
                    reasonInputLayout.error = ""
                    reasonInputET.setText("")
                    reasonInputLayout.visibility = View.GONE
                }
            }
        }

        enterButton.setOnClickListener {
            if (typeSpinner.selectedItem.toString() == "기타" && reasonInputET.text.toString().isBlank()) {
                reasonInputLayout.error = getString(R.string.enter_reason)
            } else {
                reasonInputLayout.error = ""
                enterButton.startAnimation {
                    EnterName(service, klass, typeSpinner.selectedItem.toString(), nameSpinner.selectedItem.toString(), reasonInputET.text.toString(), updateCallback).start()
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

    private fun networkError() {
        runOnUiThread {
            checkInternetLayout.visibility = View.VISIBLE
        }
    }

    private fun networkOk() {
        runOnUiThread {
            checkInternetLayout.visibility = View.GONE
        }
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
                    networkOk()
                    runOnUiThread {
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
                    networkError()
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
                    "화장실" -> range = "${klass}반!A10:A30"
                }
                val currentList = SpreadsheetHelper.getValues(service, range)
                val size = currentList?.size ?: 0

                when (type) {
                    "인강실 (1타임)" -> range = "${klass}반!C${2 + size}"
                    "인강실 (2, 3타임)" -> range = "${klass}반!D${2 + size}"
                    "동아리" -> range = "${klass}반!E${2 + size}"
                    "기타" -> range = "${klass}반!F${2 + size}"
                    "화장실" -> range = "${klass}반!A${10 + size}"
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
