package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seunghyun.dimigospreadsheet.BuildConfig
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val reference = FirebaseDatabase.getInstance().reference
    var isUpdate = true
    val versionCode = BuildConfig.VERSION_CODE
    private val closedListener = object : ValueEventListener { //앱이 점검중이면 점검화면 띄움
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value.toString().toBoolean() && !isUpdate) {
                val intent = Intent(this@MainActivity, ClosingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
    }
    private val versionListener = object : ValueEventListener { //버전 낮으면 업데이트 화면 띄움
        override fun onCancelled(error: DatabaseError) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value.toString().toInt() > versionCode) {
                isUpdate = true
                val intent = Intent(this@MainActivity, UpdateActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            } else {
                isUpdate = false
            }
        }
    }

    private val updateCallback = object : UpdateSheetValueCallback {
        override fun onReceive(values: MutableCollection<Any>?) {
            runOnUiThread {
                if (values != null) {
                    enterButton.doneLoadingAnimation(Color.GREEN, getDrawable(R.drawable.ic_baseline_check_24px)!!.toBitmap())
                    Handler().postDelayed({
                        enterButton.revertAnimation()
                    }, 1000)
                } else {
                    Toast.makeText(applicationContext, R.string.enter_failed, Toast.LENGTH_LONG).show()
                    enterButton.revertAnimation()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this, getString(R.string.admob_app_id))
        adView.loadAd(AdRequest.Builder().build())

        reference.child("app-version").addValueEventListener(versionListener)
        reference.child("isClosing").addValueEventListener(closedListener)

        val name = intent.getStringExtra("name")
        val studentId = intent.getStringExtra("studentId")
        val grade = intent.getIntExtra("grade", 0)
        val klass = intent.getIntExtra("class", 0)
        val number = studentId.substring(2, 4).toInt()
        val names = intent.getStringArrayExtra("names")

        val arrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, names)
        nameSpinner.adapter = arrayAdapter
        nameSpinner.setSelection(number - 1)
        nameTV.text = "$studentId $name"
        enterDescriptionTV.text = getString(R.string.enter_description).format(grade, klass)
        val service = SpreadsheetHelper.getService(this@MainActivity)

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

        goToSheetButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SpreadsheetActivity::class.java).apply {
                putExtra("grade", grade)
                putExtra("class", klass)
            }
            startActivity(intent)
            Animatoo.animateSlideLeft(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        reference.child("app-version").removeEventListener(versionListener)
        reference.child("isClosing").removeEventListener(closedListener)
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
