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
import com.seunghyun.dimigospreadsheet.models.UpdateSheetValueCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private val updateCallback = object : UpdateSheetValueCallback {
        override fun onReceive(values: MutableCollection<Any>?) {
            runOnUiThread {
                enterButton.revertAnimation()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        val service = getService(this@MainActivity)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 3) {
                    reasonInputLayout.visibility = View.VISIBLE
                } else {
                    reasonInputLayout.error = ""
                    reasonInputET.setText("")
                    reasonInputLayout.visibility = View.GONE
                }
            }
        }

        enterButton.setOnClickListener {
            if (typeSpinner.selectedItem.toString() == "기타" && reasonInputET.text.toString().isBlank()) {
                reasonInputLayout.error = getString(R.string.enter_reason)
            } else {
                reasonInputLayout.error = ""
                enterButton.startAnimation {
                    EnterName(service, klass, typeSpinner.selectedItem.toString(), nameSpinner.selectedItem.toString(), reasonInputET.text.toString(), updateCallback).start()
                }
            }
        }
    }

    private class EnterName(val service: Sheets, val klass: Int, val type: String, val name_: String, val reason: String, val callback: UpdateSheetValueCallback) : Thread() {
        override fun run() {
            var range = ""
            when (type) {
                "인강실 (1타임)" -> range = "${klass}반!C30"
                "인강실 (2, 3타임)" -> range = "${klass}반!D30"
                "동아리" -> range = "${klass}반!E30"
                "기타" -> range = "${klass}반!F30"
            }
            val values = if (reason.isBlank()) {
                ValueRange().setValues(listOf(listOf(name_)))
            } else {
                ValueRange().setValues(listOf(listOf("$reason - $name_")))
            }
            try {
                val result = updateValues(service, range, values)
                callback.onReceive(result)
            } catch (e: Exception) {
                callback.onReceive(null)
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
