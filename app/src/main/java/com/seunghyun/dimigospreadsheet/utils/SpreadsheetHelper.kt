package com.seunghyun.dimigospreadsheet.utils

import android.content.Context
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange

class SpreadsheetHelper {
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

        fun getValues(service: Sheets, range: String): List<List<Any>>? {
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