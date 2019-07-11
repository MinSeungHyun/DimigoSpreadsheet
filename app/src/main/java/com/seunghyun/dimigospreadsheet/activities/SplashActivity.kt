package com.seunghyun.dimigospreadsheet.activities

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash)

        val preference = getSharedPreferences(getString(R.string.preference_app_setting), Context.MODE_PRIVATE)

        val id = preference.getString("id", "")!!
        val pw = preference.getString("pw", "")!!
        if (id.isNotBlank() && pw.isNotBlank()) {
        }
    }
}
