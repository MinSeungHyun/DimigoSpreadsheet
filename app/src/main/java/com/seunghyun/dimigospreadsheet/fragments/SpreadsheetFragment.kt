package com.seunghyun.dimigospreadsheet.fragments

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AnimatorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.NetworkErrorCallback
import com.seunghyun.dimigospreadsheet.models.SheetViewModel
import kotlinx.android.synthetic.main.counts_card.view.*
import kotlinx.android.synthetic.main.counts_card_back.*
import kotlinx.android.synthetic.main.number_card_back.view.*
import kotlinx.android.synthetic.main.number_card_back.view.typeTV
import kotlinx.android.synthetic.main.number_card_prototype.view.*
import kotlinx.android.synthetic.main.spreadsheet_prototype.view.*

class SpreadsheetFragment(private val networkErrorCallback: NetworkErrorCallback, private val viewModel: SheetViewModel) : Fragment() {
    private lateinit var parent: View
    private val isBackShowing = HashMap<String, Boolean>()

    private var currentIngang1: ArrayList<String>? = null
    private var currentIngang2: ArrayList<String>? = null
    private var currentClub: ArrayList<String>? = null
    private var currentEtc: ArrayList<String>? = null
    private var currentBathroom: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isRunning.value = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        parent = inflater.inflate(R.layout.spreadsheet_prototype, container, false)
        initModel()
        initSheet()
        return parent
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.isRunning.value = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        viewModel.isShowing.postValue(isVisibleToUser)
    }

    override fun onResume() {
        super.onResume()
        if (userVisibleHint) viewModel.isShowing.postValue(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.isShowing.postValue(false)
    }

    @SuppressLint("SetTextI18n")
    private fun initModel() {
        viewModel.networkError.observe(this, Observer {
            networkErrorCallback.onError(it)
        })

        viewModel.currentTime.observe(this, Observer {
            val content = getString(R.string.current_time, it)
            val start = content.indexOf(it)
            val end = start + it.length
            val spannableString = SpannableString(content).apply {
                setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            currentTimeTV.text = spannableString
        })
        viewModel.totalCount.observe(this, Observer {
            parent.totalTV.text = getString(R.string.total) + it
        })
        viewModel.vacancyCount.observe(this, Observer {
            parent.vacancyTV.text = getString(R.string.vacancy) + it
        })
        viewModel.currentCount.observe(this, Observer {
            parent.currentTV.text = getString(R.string.current) + it
        })

        viewModel.ingang1List.observe(this, Observer {
            if (!isSameValues(it, currentIngang1)) {
                enterListToParent(parent.ingang1Layout.namesLayout, it)
                updateNumber(parent.ingang1Back, it.size)
            }
            currentIngang1 = it
        })
        viewModel.ingang2List.observe(this, Observer {
            if (!isSameValues(it, currentIngang2)) {
                enterListToParent(parent.ingang2Layout.namesLayout, it)
                updateNumber(parent.ingang2Back, it.size)
            }
            currentIngang2 = it
        })
        viewModel.clubList.observe(this, Observer {
            if (!isSameValues(it, currentClub)) {
                enterListToParent(parent.clubLayout.namesLayout, it)
                updateNumber(parent.clubBack, it.size)
            }
            currentClub = it
        })
        viewModel.etcList.observe(this, Observer {
            if (!isSameValues(it, currentEtc)) {
                enterListToParent(parent.etcLayout.namesLayout, it)
                updateNumber(parent.etcBack, it.size)
            }
            currentEtc = it
        })
        viewModel.bathroomList.observe(this, Observer {
            if (!isSameValues(it, currentBathroom)) {
                enterListToParent(parent.bathroomLayout.namesLayout, it)
                updateNumber(parent.bathroomBack, it.size)
            }
            currentBathroom = it
        })
    }

    private fun initSheet() {
        parent.ingang1Layout.typeTV.setText(R.string.ingang1)
        parent.ingang1Back.typeTV.setText(R.string.ingang1)
        parent.ingang2Layout.typeTV.setText(R.string.ingang2)
        parent.ingang2Back.typeTV.setText(R.string.ingang2)
        parent.clubLayout.typeTV.setText(R.string.club)
        parent.clubBack.typeTV.setText(R.string.club)
        parent.etcLayout.typeTV.setText(R.string.etc)
        parent.etcBack.typeTV.setText(R.string.etc)
        parent.bathroomLayout.typeTV.setText(R.string.bathroom)
        parent.bathroomBack.typeTV.setText(R.string.bathroom)

        setFlipAnimation()
    }

    private fun setFlipAnimation() {
        //CameraDistance setting
        val distance = 8000
        val scale = resources.displayMetrics.density * distance

        parent.countsLayout.tag = "countsLayout"
        parent.countsLayout.cameraDistance = scale
        parent.countsLayoutBack.cameraDistance = scale
        parent.ingang1Layout.cameraDistance = scale
        parent.ingang1Back.cameraDistance = scale
        parent.ingang2Layout.cameraDistance = scale
        parent.ingang2Back.cameraDistance = scale
        parent.clubLayout.cameraDistance = scale
        parent.clubBack.cameraDistance = scale
        parent.etcLayout.cameraDistance = scale
        parent.etcBack.cameraDistance = scale
        parent.bathroomLayout.cameraDistance = scale
        parent.bathroomBack.cameraDistance = scale

        //Init listener
        val onClickListener = View.OnClickListener {
            val (front, back) = when {
                it.tag == "countsLayout" -> listOf(parent.countsLayout, parent.countsLayoutBack)
                it.typeTV.text == getString(R.string.ingang1) -> listOf(parent.ingang1Layout, parent.ingang1Back)
                it.typeTV.text == getString(R.string.ingang2) -> listOf(parent.ingang2Layout, parent.ingang2Back)
                it.typeTV.text == getString(R.string.club) -> listOf(parent.clubLayout, parent.clubBack)
                it.typeTV.text == getString(R.string.etc) -> listOf(parent.etcLayout, parent.etcBack)
                it.typeTV.text == getString(R.string.bathroom) -> listOf(parent.bathroomLayout, parent.bathroomBack)
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
        parent.ingang1Layout.typeTV.setOnClickListener(onClickListener)
        parent.ingang2Layout.typeTV.setOnClickListener(onClickListener)
        parent.clubLayout.typeTV.setOnClickListener(onClickListener)
        parent.etcLayout.typeTV.setOnClickListener(onClickListener)
        parent.bathroomLayout.typeTV.setOnClickListener(onClickListener)
        parent.countsLayout.setOnClickListener(onClickListener)
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
        return AnimatorInflater.loadAnimator(requireContext(), id) as AnimatorSet
    }

    private fun isSameValues(list1: ArrayList<String>, list2: ArrayList<String>?): Boolean {
        if (list2 == null || list1.size != list2.size) return false
        for (i in (0 until list1.size)) {
            if (list1[i] != list2[i]) return false
        }
        return true
    }

    private fun enterListToParent(parent: LinearLayout, names: ArrayList<String>) {
        repeat(parent.childCount - 1) { parent.removeViewAt(0) }
        names.forEach {
            val nameTV = layoutInflater.inflate(R.layout.name_item, parent, false) as TextView
            nameTV.text = it
            parent.addView(nameTV, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNumber(view: View, number: Int) {
        view.countTV.text = "${number}명"
    }
}