package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.models.GetSheetValueCallback
import com.seunghyun.dimigospreadsheet.utils.JSONParser
import com.seunghyun.dimigospreadsheet.utils.JWTDecoder
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

private const val APPLICATION_NAME = "DimigoSpreadsheet"
private const val SPREADSHEET_ID = "1l4fEOCp2EahtHnpM44Y7blWf1CUqRLSe2EFVCzXjLQo"

class MainActivity : AppCompatActivity() {
    private val jsonFactory = JacksonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val scopes = listOf("https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/drive")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //정보 불러오기
        val token = JSONParser.parse(intent.getStringExtra("token"), "token")
        val identity = JSONParser.parse(JWTDecoder.getBody(token), "identity")

        val name = JSONParser.parseFromArray(identity, 0, "name")
        val studentId = JSONParser.parseFromArray(identity, 0, "serial")
        val grade = studentId.subSequence(0, 1).toString().toInt()
        val klass = studentId.subSequence(1, 2).toString().toInt()

        //초기화
        nameTV.text = "$studentId $name"
        enterDescriptionTV.text = getString(R.string.enter_description).format(grade, klass)
        val filter = Linkify.TransformFilter { _, _ -> "" }
        val pattern = Pattern.compile(getString(R.string.go_to_sheet))
        Linkify.addLinks(goToSheetButton, pattern, "http://dimigo18.tk", null, filter)


        val service = Sheets.Builder(httpTransport, jsonFactory, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build()

        val getNameCallback = object : GetSheetValueCallback {
            override fun onReceive(values: List<List<Any>>?) {
                if (values != null) {
                    val names = ArrayList<String>()
                    values.forEach { names.add(it[0].toString()) }
                    val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, names)
                    runOnUiThread {
                        nameSpinner.adapter = adapter
                    }
                } else {
                    Log.d("testing", "failed")
                }
            }
        }

        object : Thread() {
            override fun run() {
                try {
                    val names = getValues(service, "${klass}반 명단!A:A")
                    getNameCallback.onReceive(names)
                } catch (e: Exception) {
                    getNameCallback.onReceive(null)
                }
            }
        }.start()
    }

    private fun getCredentials(): Credential {
        val inputStream = resources.assets.open("Credentials.json")
        return GoogleCredential.fromStream(inputStream).createScoped(scopes)
    }

    companion object {
        private fun getValues(service: Sheets, range: String): List<List<Any>> {
            return service.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute().getValues()
        }

        private fun updateValues(service: Sheets, range: String, values: ValueRange): MutableCollection<Any> {
            service.spreadsheets().values().update(SPREADSHEET_ID, range, values).apply {
                valueInputOption = "RAW"
                return execute().values
            }
        }
    }
}
