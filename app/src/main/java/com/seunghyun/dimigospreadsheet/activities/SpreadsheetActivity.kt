package com.seunghyun.dimigospreadsheet.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.AnimatorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
import com.seunghyun.slidetodelete.enableSlideToDelete
import kotlinx.android.synthetic.main.activity_spreadsheet.*
import kotlinx.android.synthetic.main.counts_card.*
import kotlinx.android.synthetic.main.counts_card_back.*
import kotlinx.android.synthetic.main.enter_name_bottomsheet.*
import kotlinx.android.synthetic.main.name_item.view.*
import kotlinx.android.synthetic.main.network_error_screen.view.*
import kotlinx.android.synthetic.main.number_card_back.view.*
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

                    val checkIcon = getDrawable(R.drawable.ic_baseline_check_24px)!!
                    DrawableCompat.setTint(checkIcon, Color.WHITE)
                    enterButton.doneLoadingAnimation(Color.parseColor("#29B600"), checkIcon.toBitmap())

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

        spreadsheetModel.ingang1List.observe(this, Observer {
            if (!isSameValues(it, currentIngang1)) {
                currentIngang1 = it
                updateNames(ingang1Layout.namesLinearLayout, currentIngang1!!)
                updateNumber(ingang1Back, it.size)
            }
        })
        spreadsheetModel.ingang2List.observe(this, Observer {
            if (!isSameValues(it, currentIngang2)) {
                currentIngang2 = it
                updateNames(ingang2Layout.namesLinearLayout, currentIngang2!!)
                updateNumber(ingang2Back, it.size)
            }
        })
        spreadsheetModel.clubList.observe(this, Observer {
            if (!isSameValues(it, currentClub)) {
                currentClub = it
                updateNames(clubLayout.namesLinearLayout, currentClub!!)
                updateNumber(clubBack, it.size)
            }
        })
        spreadsheetModel.etcList.observe(this, Observer {
            if (!isSameValues(it, currentEtc)) {
                currentEtc = it
                updateNames(etcLayout.namesLinearLayout, currentEtc!!)
                updateNumber(etcBack, it.size)
            }
        })
        spreadsheetModel.bathroomList.observe(this, Observer {
            if (!isSameValues(it, currentBathroom)) {
                currentBathroom = it
                updateNames(bathroomLayout.namesLinearLayout, currentBathroom!!)
                updateNumber(bathroomBack, it.size)
            }
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

        ingang1Layout.namesLinearLayout.tag = getString(R.string.ingang1)
        ingang2Layout.namesLinearLayout.tag = getString(R.string.ingang2)
        clubLayout.namesLinearLayout.tag = getString(R.string.club)
        etcLayout.namesLinearLayout.tag = getString(R.string.etc)
        bathroomLayout.namesLinearLayout.tag = getString(R.string.bathroom)

        initEnterNameButton(ingang1Layout, 0)
        initEnterNameButton(ingang2Layout, 1)
        initEnterNameButton(clubLayout, 2)
        initEnterNameButton(etcLayout, 3)
        initEnterNameButton(bathroomLayout, 4)

        setFlipAnimation()

        dragHandle.setOnTouchListener { _, event ->
            val height = spreadsheet.height.toFloat()
            val minPercent = countsLayout.bottom / height
            val maxPercent = 1f

            val guideParams = guideLine.layoutParams as ConstraintLayout.LayoutParams
            val handleParams = dragHandle.layoutParams as ConstraintLayout.LayoutParams
            val y = event.y + height * handleParams.verticalBias - dragHandle.height / 2
            var percent = y / height
            if (percent < minPercent) percent = minPercent
            else if (percent > maxPercent) percent = maxPercent

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleImage.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_MOVE -> {
                    guideParams.guidePercent = percent
                    guideLine.layoutParams = guideParams
                }
                MotionEvent.ACTION_UP -> {
                    handleParams.verticalBias = percent
                    dragHandle.layoutParams = handleParams

                    handleImage.visibility = View.INVISIBLE
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun updateNames(parent: LinearLayout, namesList: ArrayList<String>) {
        repeat(parent.childCount - 1) {
            parent.removeViewAt(0)
        }
        namesList.forEach {
            val nameView = LayoutInflater.from(this@SpreadsheetActivity).inflate(R.layout.name_item, parent, false).apply {
                nameTV.text = it
                initNameView(parent, deletedTV, nameTV)
            }
            parent.addView(nameView, 0)
        }
    }

    private fun setDeleted(deletedTV: TextView) {
        deletedTV.apply {
            setOnTouchListener(null)
            setText(R.string.deleted)
            setBackgroundColor(Color.parseColor("#29B600"))
        }
    }

    private fun undoSetDeleted(parent: LinearLayout, nameTV: TextView, deletedTV: TextView) {
        initNameView(parent, deletedTV, nameTV)

        nameTV.apply {
            x = 0f
            alpha = 1f
        }
        deletedTV.apply {
            setText(R.string.deleting)
            setBackgroundColor(Color.parseColor("#ff4545"))
        }
    }

    private fun initNameView(parent: LinearLayout, deletedTV: TextView, nameTV: TextView) {
        deletedTV.enableSlideToDelete(parent, nameTV, 2000) {
            setDeleted(deletedTV)
            startDeleteProgress(nameTV) {
                undoSetDeleted(parent, nameTV, deletedTV)
            }
        }
    }


    private fun initEnterNameButton(container: View, index: Int) {
        val enterNameButtonClickListener = View.OnClickListener {
            if (it.tag == 4) {
                showBottomSheet(typeSpinner.adapter.count - 1)
            } else {
                showBottomSheet(it.tag.toString().toInt())
            }
        }
        val enterNameButton = layoutInflater.inflate(R.layout.enter_name_button, container.namesLinearLayout, false).apply {
            tag = index
            setOnClickListener(enterNameButtonClickListener)
        }
        container.namesLinearLayout.addView(enterNameButton)
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
            if (typeSpinner.selectedItem.toString() == getString(R.string.etc) && reasonInputET.text.toString().isBlank()) {
                reasonInputLayout.error = getString(R.string.enter_reason)
            } else {
                reasonInputLayout.error = ""
                enterButton.startAnimation {
                    val name = nameSpinner.selectedItem.toString()
                    val type = typeSpinner.selectedItem.toString()
                    if (!isNameExist(name, type)) EnterName(applicationContext, service, klass, type, name, reasonInputET.text.toString(), enterNameCallback).start()
                    else enterNameCallback.onReceive(null, SAME_NAME_ERROR)
                }
            }
        }
    }

    private fun isNameExist(name: String, typeToEnter: String): Boolean {
        if (typeToEnter == getString(R.string.bathroom) && currentBathroom?.contains(name) == false) return false
        else if (typeToEnter == getString(R.string.bathroom) && currentBathroom?.contains(name) == true) return true
        if (typeToEnter != getString(R.string.ingang2) && currentIngang1?.contains(name) == true) return true
        if (typeToEnter != getString(R.string.ingang1) && currentIngang2?.contains(name) == true) return true
        if (currentClub?.contains(name) == true) return true
        currentEtc?.forEach {
            if (it.contains(name)) return true
        }
        return false
    }

    private fun startDeleteProgress(view: View, failedCallback: () -> Unit) {
        val name = (view as TextView).text.toString()
        val parent = view.parent.parent as LinearLayout
        try {
            deleteTVName(name, parent, failedCallback)
        } catch (e: Exception) {
            failedCallback.invoke()
            Toast.makeText(this@SpreadsheetActivity, R.string.delete_failed, Toast.LENGTH_LONG).show()
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

    private fun deleteTVName(name: String, parent: LinearLayout, failedCallback: () -> Unit) {
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
            } catch (e: Exception) {
                runOnUiThread {
                    failedCallback.invoke()
                    Toast.makeText(this@SpreadsheetActivity, R.string.delete_failed, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private class EnterName(val context: Context, val service: Sheets, val klass: Int, val type: String, val name_: String, val reason: String, val callback: UpdateSheetValueCallback) : Thread() {
        override fun run() {
            try {
                var range = when (type) {
                    context.getString(R.string.ingang1) -> "${klass}반!C2:C30"
                    context.getString(R.string.ingang2) -> "${klass}반!D2:D30"
                    context.getString(R.string.club) -> "${klass}반!E2:E30"
                    context.getString(R.string.etc) -> "${klass}반!F2:F30"
                    context.getString(R.string.bathroom) -> "${klass}반!A10:A30"
                    else -> ""
                }
                val currentList = SpreadsheetHelper.getValues(service, range)
                val size = currentList?.size ?: 0
                val margin = if (type == context.getString(R.string.bathroom)) 10 else 2
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
