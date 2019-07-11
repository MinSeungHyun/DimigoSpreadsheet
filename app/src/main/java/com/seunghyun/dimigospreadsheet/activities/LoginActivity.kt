package com.seunghyun.dimigospreadsheet.activities

import android.os.Bundle
import android.text.util.Linkify
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

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
            loginButton.startAnimation {

            }
        }
    }
}
