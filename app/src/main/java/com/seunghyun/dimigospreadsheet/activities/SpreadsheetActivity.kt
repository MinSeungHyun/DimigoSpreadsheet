package com.seunghyun.dimigospreadsheet.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seunghyun.dimigospreadsheet.BuildConfig
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SpreadsheetState
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.enter_name_bottomsheet.*
import kotlinx.android.synthetic.main.network_error_screen.view.*
import kotlinx.android.synthetic.main.number_card_back.view.*
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import kotlinx.android.synthetic.main.number_card_prototype.view.typeTV

class SpreadsheetActivity : AppCompatActivity() {
    private val spreadsheetModel by lazy {
        SpreadsheetState.service = service
        SpreadsheetState.klass = klass
        ViewModelProviders.of(this@SpreadsheetActivity)[SpreadsheetState::class.java]
    }
    private var currentIngang1 = ArrayList<String>()
    private var currentIngang2 = ArrayList<String>()
    private var currentClub = ArrayList<String>()
    private var currentEtc = ArrayList<String>()
    private var currentBathroom = ArrayList<String>()
    private val isBackShowing = HashMap<String, Boolean>()

    private val name by lazy { intent.getStringExtra("name") }
    private val grade by lazy { intent.getIntExtra("grade", 0) }
    private val klass by lazy { intent.getIntExtra("class", 0) }
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
                    networkError(NETWORK_ERROR)
                    enterButton.revertAnimation()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        setSupportActionBar(customToolbar)
        title = ""
        toolbarTitle.text = "${grade}학년 ${klass}반"
        spreadsheetModel.isRunning.value = true

        initSheet()
        initModel()
        initBottomSheet()
    }

    override fun onResume() {
        super.onResume()
        spreadsheetModel.isShowing.value = true
        reference.child("app-version").addValueEventListener(versionListener)
        reference.child("isClosing").addValueEventListener(closedListener)
    }

    override fun onPause() {
        super.onPause()
        spreadsheetModel.isShowing.value = false
        reference.child("app-version").removeEventListener(versionListener)
        reference.child("isClosing").removeEventListener(closedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        spreadsheetModel.isRunning.value = false
    }

    @SuppressLint("SetTextI18n")
    private fun initModel() {
        spreadsheetModel.networkError.observe(this, Observer {
            when (it) {
                null -> networkOk()
                is GoogleJsonResponseException -> networkError(SERVER_ERROR)
                else -> networkError(NETWORK_ERROR)
            }
        })

        spreadsheetModel.totalCount.observe(this, Observer {
            totalTV.text = getString(R.string.total) + it
        })
        spreadsheetModel.vacancyCount.observe(this, Observer {
            vacancyTV.text = getString(R.string.vacancy) + it
        })
        spreadsheetModel.currentCount.observe(this, Observer {
            currentTV.text = getString(R.string.current) + it
        })

        spreadsheetModel.ingang1List.observe(this, Observer {
            if (!isSameValues(it, currentIngang1)) {
                enterListToParent(ingang1Layout.namesLayout, it)
                updateNumber(ingang1Back, it.size)
            }
            currentIngang1 = it
        })
        spreadsheetModel.ingang2List.observe(this, Observer {
            if (!isSameValues(it, currentIngang2)) {
                enterListToParent(ingang2Layout.namesLayout, it)
                updateNumber(ingang2Back, it.size)
            }
            currentIngang2 = it
        })
        spreadsheetModel.clubList.observe(this, Observer {
            if (!isSameValues(it, currentClub)) {
                enterListToParent(clubLayout.namesLayout, it)
                updateNumber(clubBack, it.size)
            }
            currentClub = it
        })
        spreadsheetModel.etcList.observe(this, Observer {
            if (!isSameValues(it, currentEtc)) {
                enterListToParent(etcLayout.namesLayout, it)
                updateNumber(etcBack, it.size)
            }
            currentEtc = it
        })
        spreadsheetModel.bathroomList.observe(this, Observer {
            if (!isSameValues(it, currentBathroom)) {
                enterListToParent(bathroomLayout.namesLayout, it)
                updateNumber(bathroomBack, it.size)
            }
            currentBathroom = it
        })
    }

    private fun initSheet() {
        ingang1Layout.typeTV.setText(R.string.ingang1)
        ingang1Back.typeTV.setText(R.string.ingang1)
        ingang2Layout.typeTV.setText(R.string.ingang2)
        ingang2Back.typeTV.setText(R.string.ingang2)
        clubLayout.typeTV.setText(R.string.club)
        clubBack.typeTV.setText(R.string.club)
        etcLayout.typeTV.setText(R.string.etc)
        etcBack.typeTV.setText(R.string.etc)
        bathroomLayout.typeTV.setText(R.string.bathroom)
        bathroomBack.typeTV.setText(R.string.bathroom)

        updateNumber(ingang1Back, 0)
        updateNumber(ingang2Back, 0)
        updateNumber(clubBack, 0)
        updateNumber(etcBack, 0)
        updateNumber(bathroomBack, 0)

        setFlipAnimation()
    }

    private fun setFlipAnimation() {
        //CameraDistance setting
        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        ingang1Layout.cameraDistance = scale
        ingang1Back.cameraDistance = scale
        ingang2Layout.cameraDistance = scale
        ingang2Back.cameraDistance = scale
        clubLayout.cameraDistance = scale
        clubBack.cameraDistance = scale
        etcLayout.cameraDistance = scale
        etcBack.cameraDistance = scale
        bathroomLayout.cameraDistance = scale
        bathroomBack.cameraDistance = scale

        //Init listener
        val onClickListener = View.OnClickListener {
            val front = when (it.typeTV.text) {
                getString(R.string.ingang1) -> ingang1Layout
                getString(R.string.ingang2) -> ingang2Layout
                getString(R.string.club) -> clubLayout
                getString(R.string.etc) -> etcLayout
                getString(R.string.bathroom) -> bathroomLayout
                else -> null
            }
            val back = when (it.typeTV.text) {
                getString(R.string.ingang1) -> ingang1Back
                getString(R.string.ingang2) -> ingang2Back
                getString(R.string.club) -> clubBack
                getString(R.string.etc) -> etcBack
                getString(R.string.bathroom) -> bathroomBack
                else -> null
            }
            if (isBackShowing[it.typeTV.text] == true) {
                if (front != null && back != null) {
                    flipView(back, front)
                    isBackShowing[it.typeTV.text.toString()] = false
                }
            } else {
                if (front != null && back != null) {
                    flipView(front, back)
                    isBackShowing[it.typeTV.text.toString()] = true
                }
            }
        }

        //Set listener
        ingang1Layout.typeTV.setOnClickListener(onClickListener)
        ingang2Layout.typeTV.setOnClickListener(onClickListener)
        clubLayout.typeTV.setOnClickListener(onClickListener)
        etcLayout.typeTV.setOnClickListener(onClickListener)
        bathroomLayout.typeTV.setOnClickListener(onClickListener)
    }

    private fun flipView(front: View, back: View) {
        val flipOutSet = AnimatorInflater.loadAnimator(this, R.animator.flip_out) as AnimatorSet
        val flipInSet = AnimatorInflater.loadAnimator(this, R.animator.flip_in) as AnimatorSet
        flipOutSet.setTarget(front)
        flipInSet.setTarget(back)
        flipOutSet.start()
        flipInSet.start()
    }

    private fun initBottomSheet() {
        val arrayAdapter = ArrayAdapter(this@SpreadsheetActivity, android.R.layout.simple_spinner_dropdown_item, names)
        nameSpinner.adapter = arrayAdapter
        if (names.contains(name)) nameSpinner.setSelection(names.indexOf(name))
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

    @SuppressLint("SetTextI18n")
    private fun updateNumber(view: View, number: Int) {
        view.countTV.text = "${number}명"
    }

    private fun isSameValues(list1: ArrayList<String>, list2: ArrayList<String>): Boolean {
        if (list1.size != list2.size) return false
        for (i in (0 until list1.size)) {
            if (list1[i] != list2[i]) return false
        }
        return true
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

    companion object {
        const val NETWORK_ERROR = 0
        const val SERVER_ERROR = 1
    }
}
