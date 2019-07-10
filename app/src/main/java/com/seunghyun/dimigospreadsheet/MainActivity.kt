package com.seunghyun.dimigospreadsheet

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange

private const val APPLICATION_NAME = "DimigoSpreadsheet"
private const val SPREADSHEET_ID = "1l4fEOCp2EahtHnpM44Y7blWf1CUqRLSe2EFVCzXjLQo"

class MainActivity : AppCompatActivity() {
    private val jsonFactory = JacksonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val scopes = listOf("https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/drive")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val service = Sheets.Builder(httpTransport, jsonFactory, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build()

        object : Thread() {
            override fun run() {
                val response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "test!A1")
                        .execute()
                Log.d("testing", response.getValues().toString())

                val request: Sheets.Spreadsheets.Values.Update = service.spreadsheets().values().update(SPREADSHEET_ID, "test!C5:D6", ValueRange().setValues(listOf(listOf("test2", "test3"), listOf("test4", "test5"))))
                request.valueInputOption = "RAW"
                Log.d("testing", request.execute().values.toString())
            }
        }.start()

    }

    private fun getCredentials(): Credential {
        val inputStream = resources.assets.open("Credentials.json")
        return GoogleCredential.fromStream(inputStream).createScoped(scopes)
    }
}
