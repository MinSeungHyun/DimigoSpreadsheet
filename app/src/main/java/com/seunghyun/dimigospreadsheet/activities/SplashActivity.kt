package com.seunghyun.dimigospreadsheet.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.GetSheetValueCallback
import com.seunghyun.dimigospreadsheet.models.Result
import com.seunghyun.dimigospreadsheet.models.ServerCallback
import com.seunghyun.dimigospreadsheet.utils.JSONParser
import com.seunghyun.dimigospreadsheet.utils.JWTDecoder
import com.seunghyun.dimigospreadsheet.utils.ServerRequest
import com.seunghyun.dimigospreadsheet.utils.SpreadsheetHelper
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private val loginCallback = object : ServerCallback {
        override fun onReceive(result: Result) {
            if (result.code == 200) {
                val token = JSONParser.parse(result.content!!, "token")
                val identity = JSONParser.parse(JWTDecoder.getBody(token), "identity")
                val userType = JSONParser.parseFromArray(identity, 0, "user_type")
                val name = JSONParser.parseFromArray(identity, 0, "name")

                with(getSharedPreferences(getString(R.string.preference_app_setting), Context.MODE_PRIVATE).edit()) {
                    putString("identity", identity)
                    putString("userType", userType)
                    putString("name", name)
                    apply()
                }

                val intent: Intent
                if (userType == "S") {
                    val studentId = JSONParser.parseFromArray(identity, 0, "serial")
                    val grade = studentId.subSequence(0, 1).toString().toInt()
                    val klass = studentId.subSequence(1, 2).toString().toInt()

                    runOnUiThread {
                        loadingTV.text = getString(R.string.welcome).format(name)
                    }

                    intent = Intent(this@SplashActivity, SpreadsheetActivity::class.java).apply {
                        putExtra("name", name)
                        putExtra("grade", grade)
                        putExtra("class", klass)
                    }

                    val getNameCallback = object : GetSheetValueCallback {
                        override fun onReceive(values: List<List<Any>>) {
                            if (values[0][0] != "error") {
                                val names = ArrayList<String>()
                                values.forEach { if (it.isNotEmpty()) names.add(it[0].toString()) }
                                intent.putExtra("names", names.toTypedArray())

                                Handler(Looper.getMainLooper()).postDelayed(DelayHandler(intent, this@SplashActivity), 0)
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@SplashActivity, values[0][1].toString(), Toast.LENGTH_LONG).show()
                                }
                                Handler(Looper.getMainLooper()).postDelayed(DelayHandler(Intent(this@SplashActivity, LoginActivity::class.java), this@SplashActivity), 0)
                            }
                        }
                    }

                    object : Thread() {
                        override fun run() {
                            try {
                                val service = SpreadsheetHelper.getService(applicationContext)
                                var names = SpreadsheetHelper.getValues(service, "${klass}반 명단!A:A")
                                if (names == null) names = ArrayList(listOf(ArrayList(listOf("error", getString(R.string.names_not_found))))).toList()
                                getNameCallback.onReceive(names)

                            } catch (e: GoogleJsonResponseException) {
                                e.printStackTrace()
                                val names = ArrayList(listOf(ArrayList(listOf("error", getString(R.string.server_error))))).toList()
                                getNameCallback.onReceive(names)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                val names = ArrayList(listOf(ArrayList(listOf("error", getString(R.string.check_internet))))).toList()
                                getNameCallback.onReceive(names)
                            }
                        }
                    }.start()
                } else {
                    runOnUiThread {
                        loadingTV.text = getString(R.string.welcome_teacher).format(name)
                    }

                    intent = Intent(this@SplashActivity, TeacherSpreadsheetActivity::class.java)
                    Handler(Looper.getMainLooper()).postDelayed(DelayHandler(intent, this@SplashActivity), 1000)
                }
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
