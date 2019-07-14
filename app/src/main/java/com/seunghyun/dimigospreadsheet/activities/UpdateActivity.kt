package com.seunghyun.dimigospreadsheet.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_update.*

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_update)

        updateButton.setOnClickListener {
            openStore(this@UpdateActivity)
        }
    }

    private fun openStore(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        Log.d("testing", context.packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.store_not_found), Toast.LENGTH_LONG).show()
        }
    }
}
