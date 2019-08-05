package com.seunghyun.dimigospreadsheet.activities

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_reason_dialog.*

class ReasonDialogActivity : AppCompatActivity() {
    companion object {
        lateinit var callback: (String) -> Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_reason_dialog)

        val displaySize = Point()
        windowManager.defaultDisplay.getSize(displaySize)
        container.layoutParams.width = (displaySize.x / 1.2).toInt()

        cancelButton.setOnClickListener { finish() }
        okButton.setOnClickListener {
            errorTV.visibility = View.INVISIBLE
            if (reasonET.text.toString().isBlank()) {
                errorTV.visibility = View.VISIBLE
            } else {
                try {
                    callback.invoke(reasonET.text.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                finish()
            }
        }
    }
}