package com.seunghyun.dimigospreadsheet.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //초기화
        val name = intent.getStringExtra("name")
        val studentId = intent.getStringExtra("studentId")
        val grade = intent.getIntExtra("grade", 0)
        val klass = intent.getIntExtra("class", 0)
        val names = intent.getStringArrayExtra("names")

        val arrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, names)
        nameSpinner.adapter = arrayAdapter
        nameTV.text = "$studentId $name"
        enterDescriptionTV.text = getString(R.string.enter_description).format(grade, klass)
        val filter = Linkify.TransformFilter { _, _ -> "" }
        val pattern = Pattern.compile(getString(R.string.go_to_sheet))
        Linkify.addLinks(goToSheetButton, pattern, "http://dimigo18.tk", null, filter)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 3) {
                    reasonInputLayout.visibility = View.VISIBLE
                } else {
                    reasonInputLayout.visibility = View.GONE
                    reasonInputET.setText("")
                }
            }
        }
    }

    companion object {
        private const val APPLICATION_NAME = "DimigoSpreadsheet"
        private const val SPREADSHEET_ID = "1l4fEOCp2EahtHnpM44Y7blWf1CUqRLSe2EFVCzXjLQo"
        private val jsonFactory = JacksonFactory.getDefaultInstance()
        private val httpTransport = NetHttpTransport()
        private val scopes = listOf("https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/drive")


        private fun getCredentials(context: Context): Credential {
            val inputStream = context.resources.assets.open("Credentials.json")
            return GoogleCredential.fromStream(inputStream).createScoped(scopes)
        }

        fun getService(context: Context): Sheets {
            return Sheets.Builder(httpTransport, jsonFactory, getCredentials(context))
                    .setApplicationName(APPLICATION_NAME)
                    .build()
        }

        fun getValues(service: Sheets, range: String): List<List<Any>> {
            return service.spreadsheets().values()
                    .get(SPREADSHEET_ID, range)
                    .execute().getValues()
        }

        fun updateValues(service: Sheets, range: String, values: ValueRange): MutableCollection<Any> {
            service.spreadsheets().values().update(SPREADSHEET_ID, range, values).apply {
                valueInputOption = "RAW"
                return execute().values
            }
        }
    }
}
