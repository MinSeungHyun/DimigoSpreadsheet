package com.seunghyun.dimigospreadsheet

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.InputStreamReader
import java.util.*

private const val APPLICATION_NAME = "DimigoSpreadsheet"
private const val SPREADSHEET_ID = "1l4fEOCp2EahtHnpM44Y7blWf1CUqRLSe2EFVCzXjLQo"

class MainActivity : AppCompatActivity() {
    private val jsonFactory = JacksonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val scopes: List<String> = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val service = Sheets.Builder(httpTransport, jsonFactory, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build()

        val response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, "사용 안내!A1:A20")
                .execute()
        Log.d("testing", response.getValues().toString())
    }

    private fun getCredentials(httpTransport: NetHttpTransport): Credential {
        val inputStream = resources.assets.open("Credentials.json")
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(FileDataStoreFactory(File("tokens")))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}
