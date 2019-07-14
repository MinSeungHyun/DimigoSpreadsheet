package com.seunghyun.dimigospreadsheet.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.seunghyun.dimigospreadsheet.R
import kotlinx.android.synthetic.main.activity_update.*

class UpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(R.layout.activity_update)

        val updateInfo = getAppUpdateInfo(this@UpdateActivity)
        val inAppUpdateAvailability = updateInfo != null && isUpdateAvailable(updateInfo)

        updateButton.setOnClickListener {
            if (inAppUpdateAvailability) {
                startUpdate(this@UpdateActivity, updateInfo!!)
            } else {
                openStore(this@UpdateActivity)
            }
        }
    }

    private fun openStore(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        Log.d("testing", context.packageName)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.store_not_found), Toast.LENGTH_LONG).show()
        }
    }

    private fun getAppUpdateInfo(context: Context): AppUpdateInfo? {
        val appUpdateManager = AppUpdateManagerFactory.create(context)
        val appUpdateInfo = appUpdateManager.appUpdateInfo
        var updateInfo: AppUpdateInfo? = null
        appUpdateInfo.addOnCompleteListener {
            if (it.isComplete) {
                try {
                    updateInfo = it.result
                } catch (e: Exception) {
                    return@addOnCompleteListener
                }
            }
        }
        return updateInfo
    }

    private fun isUpdateAvailable(updateInfo: AppUpdateInfo): Boolean {
        return (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
    }

    private fun startUpdate(activity: Activity, updateInfo: AppUpdateInfo) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.IMMEDIATE, activity, 1)
    }
}