package com.seunghyun.dimigospreadsheet.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_teacher_spreadsheet.*

class TeacherSpreadsheetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_spreadsheet)
        title = ""
        titleTV.text = "1학년 1반"
    }
}
