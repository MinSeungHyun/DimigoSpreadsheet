package com.seunghyun.dimigospreadsheet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.Result
import com.seunghyun.dimigospreadsheet.models.ServerCallback
import com.seunghyun.dimigospreadsheet.utils.ServerRequest
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private val loginCallback = object : ServerCallback {
        override fun onReceive(result: Result) {
            if (result.code == 200) {
                runOnUiThread {
                    loadingTV.setText(R.string.loading)
                }
                val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                    putExtra("token", result.content)
                }
                Handler(Looper.getMainLooper()).postDelayed(DelayHandler(intent, this@SplashActivity), 1000)
            } else {
                runOnUiThread {
                    loadingTV.setText(R.string.login_failed)
                }
                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                Handler(Looper.getMainLooper()).postDelayed(DelayHandler(intent, this@SplashActivity), 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_splash)

        val preference = getSharedPreferences(getString(R.string.preference_app_setting), Context.MODE_PRIVATE)

        val id = preference.getString("id", "")!!
        val pw = preference.getString("pw", "")!!
        if (id.isNotBlank() && pw.isNotBlank()) {
            ServerRequest.login(id, pw, loginCallback)
            loadingTV.setText(R.string.progress_login)
        } else {
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            Handler().postDelayed(DelayHandler(intent, this@SplashActivity), 1000)
        }
    }

    private class DelayHandler(val intent: Intent, val activity: Activity) : Runnable {
        override fun run() {
            activity.startActivity(intent)
            Animatoo.animateFade(activity)
            activity.finish()
        }
    }
}
