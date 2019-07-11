package com.seunghyun.dimigospreadsheet.activities

import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.Result
import com.seunghyun.dimigospreadsheet.models.ServerCallback
import com.seunghyun.dimigospreadsheet.utils.ServerRequest
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private val loginCallback = object : ServerCallback {
        override fun onReceive(result: Result) {
            when (result.code) {
                null -> runOnUiThread {
                    errorTV.setText(R.string.check_internet)
                    errorTV.visibility = View.VISIBLE
                    loginButton.revertAnimation()
                }
                200 -> {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
                else -> runOnUiThread {
                    errorTV.setText(R.string.check_id_pw)
                    errorTV.visibility = View.VISIBLE
                    loginButton.revertAnimation()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_login)

        //로그인 설명에 링크 걸기
        val filter = Linkify.TransformFilter { _, _ -> "" }
        val pattern = Pattern.compile("인원체크 스프레드시트")
        Linkify.addLinks(loginDescriptionTV, pattern, "http://dimigo18.tk", null, filter)

        //로그인 버튼 클릭 애니메이션
        loginButton.setOnClickListener {
            errorTV.visibility = View.GONE
            loginButton.startAnimation {
                ServerRequest.login(idInputET.text.toString(), pwInputET.text.toString(), loginCallback)
            }
        }
    }
}
