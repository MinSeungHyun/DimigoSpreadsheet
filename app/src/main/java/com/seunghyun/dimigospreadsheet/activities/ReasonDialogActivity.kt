package com.seunghyun.dimigospreadsheet.activities

import android.graphics.Point
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_reason_dialog.*

class ReasonDialogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_reason_dialog)

        val displaySize = Point()
        windowManager.defaultDisplay.getSize(displaySize)
        container.layoutParams.width = (displaySize.x / 1.2).toInt()
    }
}