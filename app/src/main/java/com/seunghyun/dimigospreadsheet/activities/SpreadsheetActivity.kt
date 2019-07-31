package com.seunghyun.dimigospreadsheet.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.AnimatorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.seunghyun.dimigospreadsheet.BuildConfig
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import com.seunghyun.dimigospreadsheet.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.counts_card.*
import kotlinx.android.synthetic.main.counts_card_back.*
import kotlinx.android.synthetic.main.enter_name_bottomsheet.*
import kotlinx.android.synthetic.main.network_error_screen.view.*
import kotlinx.android.synthetic.main.number_card_back.view.*
import kotlinx.android.synthetic.main.number_card_prototype.*
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import kotlinx.android.synthetic.main.number_card_prototype.view.typeTV
import kotlinx.android.synthetic.main.spreadsheet_prototype.*

class SpreadsheetActivity : AppCompatActivity() {
    private val spreadsheetModel by lazy { ViewModelProviders.of(this@SpreadsheetActivity, ViewModelFactory(service, klass))[SheetViewModel::class.java] }
    private var currentIngang1: ArrayList<String>? = null
    private var currentIngang2: ArrayList<String>? = null
    private var currentClub: ArrayList<String>? = null
    private var currentEtc: ArrayList<String>? = null
    private var currentBathroom: ArrayList<String>? = null
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
    private val enterNameCallback = object : UpdateSheetValueCallback {
        override fun onReceive(values: MutableCollection<Any>?, error: Int?) {
            runOnUiThread {
                if (error == null) {
                    networkOk()
                    enterButton.doneLoadingAnimation(Color.GREEN, getDrawable(R.drawable.ic_baseline_check_24px)!!.toBitmap())
                    Handler().postDelayed({
                        enterButton.revertAnimation()
                    }, 1000)
                } else {
                    if (error == SAME_NAME_ERROR) {
                        Toast.makeText(this@SpreadsheetActivity, R.string.name_exist, Toast.LENGTH_LONG).show()
                        enterButton.revertAnimation()
                    } else {
                        networkError(error)
                        enterButton.revertAnimation()
                    }
                }
            }
        }
    }

    private val onNameClickListener = View.OnClickListener {
        val name = (it as TextView).text.toString()
        val parent = it.parent as LinearLayout
        AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setPositiveButton(R.string.delete) { _, _ ->
                    try {
                        deleteTVName(name, parent)
                    } catch (e: Exception) {
                        Toast.makeText(this@SpreadsheetActivity, R.string.delete_failed, Toast.LENGTH_LONG).show()
                    }
                }
                .setTitle(getString(R.string.delete_title, name))
                .show()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spreadsheet)
        setSupportActionBar(customToolbar)
        title = ""
        toolbarTitle.text = "${grade}학년 ${klass}반"
        spreadsheetModel.isRunning.value = true

        MobileAds.initialize(this, getString(R.string.admob_app_id))
        adView.loadAd(AdRequest.Builder().build())

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

        spreadsheetModel.currentTime.observe(this, Observer {
            val content = getString(R.string.current_time, it)
            val start = content.indexOf(it)
            val end = start + it.length
            val spannableString = SpannableString(content).apply {
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@SpreadsheetActivity, R.color.colorPrimary)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            currentTimeTV.text = spannableString
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

        spreadsheetModel.currentTime.observe(this, Observer {
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

        ingang1Layout.namesLayout.tag = getString(R.string.ingang1)
        ingang2Layout.namesLayout.tag = getString(R.string.ingang2)
        clubLayout.namesLayout.tag = getString(R.string.club)
        etcLayout.namesLayout.tag = getString(R.string.etc)
        bathroomLayout.namesLayout.tag = getString(R.string.bathroom)

        initEnterNameButton(ingang1Layout, 0)
        initEnterNameButton(ingang2Layout, 1)
        initEnterNameButton(clubLayout, 2)
        initEnterNameButton(etcLayout, 3)
        initEnterNameButton(bathroomLayout, 4)

        setFlipAnimation()

        dragHandle.setOnTouchListener { _, event ->
            val guideParams = guideLine.layoutParams as ConstraintLayout.LayoutParams
            val handleParams = dragHandle.layoutParams as ConstraintLayout.LayoutParams
            val height = spreadsheet.height
            val y = event.y + height * handleParams.verticalBias - dragHandle.height / 2
            val percent = y / height

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    guideParams.guidePercent = percent
                    guideLine.layoutParams = guideParams
                }
                MotionEvent.ACTION_UP -> {
                    handleParams.verticalBias = percent
                    dragHandle.layoutParams = handleParams
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun initEnterNameButton(container: View, index: Int) {
        val enterNameButtonClickListener = View.OnClickListener {
            showBottomSheet(it.tag.toString().toInt())
        }
        val enterNameButton = layoutInflater.inflate(R.layout.enter_name_button, namesLayout, false)
        enterNameButton.tag = index
        enterNameButton.setOnClickListener(enterNameButtonClickListener)
        container.namesLayout.addView(enterNameButton, 0)
    }

    private fun showBottomSheet(selectedType: Int) {
        typeSpinner.setSelection(selectedType)
        val reasonInputVisibility = if (selectedType == 3) View.VISIBLE else View.GONE
        Thread {
            while (reasonInputLayout.visibility != reasonInputVisibility);
            runOnUiThread {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }.start()
    }

    private fun setFlipAnimation() {
        //CameraDistance setting
        val distance = 8000
        val scale = resources.displayMetrics.density * distance

        countsLayout.tag = "countsLayout"
        countsLayout.cameraDistance = scale
        countsLayoutBack.cameraDistance = scale
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
            val (front, back) = when {
                it.tag == "countsLayout" -> listOf(countsLayout, countsLayoutBack)
                it.typeTV.text == getString(R.string.ingang1) -> listOf(ingang1Layout, ingang1Back)
                it.typeTV.text == getString(R.string.ingang2) -> listOf(ingang2Layout, ingang2Back)
                it.typeTV.text == getString(R.string.club) -> listOf(clubLayout, clubBack)
                it.typeTV.text == getString(R.string.etc) -> listOf(etcLayout, etcBack)
                it.typeTV.text == getString(R.string.bathroom) -> listOf(bathroomLayout, bathroomBack)
                else -> listOf(null, null)
            }
            val index: String = if (it.tag == "countsLayout") it.tag.toString() else it.typeTV.text.toString()

            if (front == null || back == null) return@OnClickListener
            if (isBackShowing[index] == true) {
                if (it.tag == "countsLayout") flipView(back, front, true)
                else flipView(back, front)
                isBackShowing[index] = false
            } else {
                if (it.tag == "countsLayout") flipView(front, back, true)
                else flipView(front, back)
                isBackShowing[index] = true
            }
        }

        //Set listener
        countsLayout.setOnClickListener(onClickListener)
        ingang1Layout.typeTV.setOnClickListener(onClickListener)
        ingang2Layout.typeTV.setOnClickListener(onClickListener)
        clubLayout.typeTV.setOnClickListener(onClickListener)
        etcLayout.typeTV.setOnClickListener(onClickListener)
        bathroomLayout.typeTV.setOnClickListener(onClickListener)
    }

    private fun flipView(front: View, back: View, flipVertically: Boolean = false) {
        val (flipOutSet, flipInSet) =
                if (!flipVertically) listOf(loadAnimatorSet(R.animator.flip_out), loadAnimatorSet(R.animator.flip_in))
                else listOf(loadAnimatorSet(R.animator.flip_out_vertical), loadAnimatorSet(R.animator.flip_in_vertical))
        flipOutSet.setTarget(front)
        flipInSet.setTarget(back)
        flipOutSet.start()
        flipInSet.start()
    }

    private fun loadAnimatorSet(@AnimatorRes id: Int): AnimatorSet {
        return AnimatorInflater.loadAnimator(this, id) as AnimatorSet
    }

    private fun initBottomSheet() {
        val arrayAdapter = ArrayAdapter(this@SpreadsheetActivity, android.R.layout.simple_spinner_item, names)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        nameSpinner.adapter = arrayAdapter
        if (names.contains(name)) nameSpinner.setSelection(names.indexOf(name))

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectionItem = typeSpinner.selectedItem.toString()
                if (selectionItem.substring(0, 1).isBlank()) {
                    reasonInputET.setText(selectionItem.trim().drop(2))
                    typeSpinner.setSelection(3)
                    return
                }
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
                    val name = nameSpinner.selectedItem.toString()
                    val type = typeSpinner.selectedItem.toString()
                    if (!isNameExist(name, type)) EnterName(service, klass, type, name, reasonInputET.text.toString(), enterNameCallback).start()
                    else enterNameCallback.onReceive(null, SAME_NAME_ERROR)
                }
            }
        }
    }

    private fun isNameExist(name: String, typeToEnter: String): Boolean {
        if (typeToEnter == "화장실" && currentBathroom?.contains(name) == false) return false
        else if (typeToEnter == "화장실" && currentBathroom?.contains(name) == true) return true
        if (typeToEnter != "인강실 (2, 3타임)" && currentIngang1?.contains(name) == true) return true
        if (typeToEnter != "인강실 (1타임)" && currentIngang2?.contains(name) == true) return true
        if (currentClub?.contains(name) == true) return true
        currentEtc?.forEach {
            if (it.contains(name)) return true
        }
        return false
    }

    private fun enterListToParent(parent: LinearLayout, names: ArrayList<String>) {
        repeat(parent.childCount - 1) { parent.removeViewAt(0) }
        names.forEach {
            val nameTV = layoutInflater.inflate(R.layout.name_item, parent, false) as TextView
            nameTV.text = it
            nameTV.setOnClickListener(onNameClickListener)
            parent.addView(nameTV, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNumber(view: View, number: Int) {
        view.countTV.text = "${number}명"
    }

    private fun isSameValues(list1: ArrayList<String>, list2: ArrayList<String>?): Boolean {
        if (list2 == null || list1.size != list2.size) return false
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

    private fun deleteTVName(name: String, parent: LinearLayout) {
        val range = when (parent.tag) {
            getString(R.string.ingang1) -> "${klass}반!C2:C30"
            getString(R.string.ingang2) -> "${klass}반!D2:D30"
            getString(R.string.club) -> "${klass}반!E2:E30"
            getString(R.string.etc) -> "${klass}반!F2:F30"
            getString(R.string.bathroom) -> "${klass}반!A10:A30"
            else -> ""
        }
        if (range.isBlank()) throw Exception()
        Thread {
            try {
                SpreadsheetHelper.deleteValueInRange(service, range, name).toString()
                runOnUiThread {
                    Toast.makeText(this@SpreadsheetActivity, R.string.delete_succeeded, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SpreadsheetActivity, R.string.delete_failed, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private class EnterName(val service: Sheets, val klass: Int, val type: String, val name_: String, val reason: String, val callback: UpdateSheetValueCallback) : Thread() {
        override fun run() {
            try {
                var range = when (type) {
                    "인강실 (1타임)" -> "${klass}반!C2:C30"
                    "인강실 (2, 3타임)" -> "${klass}반!D2:D30"
                    "동아리" -> "${klass}반!E2:E30"
                    "기타" -> "${klass}반!F2:F30"
                    "화장실" -> "${klass}반!A10:A30"
                    else -> ""
                }
                val currentList = SpreadsheetHelper.getValues(service, range)
                val size = currentList?.size ?: 0
                val margin = if (type == "화장실") 10 else 2
                range = range.substring(0, 4) + (margin + size)

                val values = if (reason.isBlank()) {
                    ValueRange().setValues(listOf(listOf(name_)))
                } else {
                    ValueRange().setValues(listOf(listOf("$reason - $name_")))
                }
                val result = SpreadsheetHelper.updateValues(service, range, values)
                callback.onReceive(result)
            } catch (e: Exception) {
                callback.onReceive(null, NETWORK_ERROR)
            }
        }
    }

    companion object {
        const val NETWORK_ERROR = 0
        const val SERVER_ERROR = 1
        const val SAME_NAME_ERROR = 2
    }
}
