package com.seunghyun.dimigospreadsheet.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
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
                    val editor = getSharedPreferences(getString(R.string.preference_app_setting), Context.MODE_PRIVATE).edit()
                    editor.putString("id", idInputET.text.toString())
                    editor.putString("pw", pwInputET.text.toString())
                    editor.apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    Animatoo.animateFade(this@LoginActivity)
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
                if (currentFocus != null) {
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                }
                idInputET.clearFocus()
                pwInputET.clearFocus()
                ServerRequest.login(idInputET.text.toString(), pwInputET.text.toString(), loginCallback)
            }
        }
    }
}
