package com.seunghyun.dimigospreadsheet.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.IdRes
import com.google.api.services.sheets.v4.model.ValueRange
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.activities.ReasonDialogActivity
import com.seunghyun.dimigospreadsheet.activities.SplashActivity
import com.seunghyun.dimigospreadsheet.models.SheetValue
import java.text.SimpleDateFormat
import java.util.*

class WidgetProvider : AppWidgetProvider() {
    companion object {
        const val WIDGET_ACTION = "com.seunghyun.dimigospreadsheet.WIDGET_ACTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null || intent == null) return

        if (intent.action == WIDGET_ACTION) {
            val viewId = intent.getIntExtra("viewId", 0)
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)

            if (viewId == R.id.refreshButton) loadStateFromServer(context, remoteViews)
            else switchState(context, remoteViews, viewId)

            AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null) return

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        setOnClickPendingIntent(context, remoteViews)

        loadStateFromServer(context, remoteViews)

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    private fun setOnClickPendingIntent(context: Context, remoteViews: RemoteViews) {
        remoteViews.setOnClickPendingIntent(R.id.ingang1, getButtonClickIntent(context, R.id.ingang1))
        remoteViews.setOnClickPendingIntent(R.id.ingang2, getButtonClickIntent(context, R.id.ingang2))
        remoteViews.setOnClickPendingIntent(R.id.club, getButtonClickIntent(context, R.id.club))
        remoteViews.setOnClickPendingIntent(R.id.etc, getButtonClickIntent(context, R.id.etc))
        remoteViews.setOnClickPendingIntent(R.id.bathroom, getButtonClickIntent(context, R.id.bathroom))
        remoteViews.setOnClickPendingIntent(R.id.refreshButton, getButtonClickIntent(context, R.id.refreshButton))
        remoteViews.setOnClickPendingIntent(R.id.openAppButton, PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java), 0))
    }

    private fun getButtonClickIntent(context: Context, @IdRes id: Int): PendingIntent {
        val intent = Intent(context, WidgetProvider::class.java).apply {
            action = WIDGET_ACTION
            putExtra("viewId", id)
        }
        return PendingIntent.getBroadcast(context, id, intent, 0)
    }

    private fun loadStateFromServer(context: Context, remoteViews: RemoteViews, isErrorContains: Boolean = false) {
        Thread {
            if (!isErrorContains) {
                setErrorVisibility(context, remoteViews, View.GONE)
            }
            setProgressBarVisibility(context, remoteViews, View.VISIBLE)
            try {
                remoteViews.setTextViewText(R.id.refreshTimeTV, getCurrentTime())

                val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
                val editor = preference.edit()
                val userType = preference.getString("userType", null)
                val name = preference.getString("name", null)
                val identity = preference.getString("identity", null)
                if (userType == null || name == null || identity == null) throw LoginRequiredException()
                if (userType != "S") throw TeacherCannotUseException()
                val klass = JSONParser.parseFromArray(identity, 0, "serial").subSequence(1, 2).toString().toInt()

                val sheetValue = SheetValue(SpreadsheetHelper.getValues(SpreadsheetHelper.getService(context), "${klass}반!1:30"))
                with(editor) {
                    putBoolean(context.getString(R.string.ingang1), sheetValue.ingang1.contains(name))
                    putBoolean(context.getString(R.string.ingang2), sheetValue.ingang2.contains(name))
                    putBoolean(context.getString(R.string.club), sheetValue.club.contains(name))
                    putBoolean(context.getString(R.string.bathroom), sheetValue.bathroom.contains(name))
                }

                var cnt = 0
                sheetValue.etc.forEach {
                    if (it.contains(name)) cnt++
                }
                editor.putBoolean(context.getString(R.string.etc), cnt != 0)

                editor.apply()

                loadBackgroundStates(context, remoteViews)

            } catch (e: LoginRequiredException) {
                e.printStackTrace()
                setLoginVisibility(context, remoteViews, View.VISIBLE)
            } catch (e: TeacherCannotUseException) {
                setTeacherVisibility(context, remoteViews, View.VISIBLE)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, remoteViews, View.VISIBLE)
            }
            setProgressBarVisibility(context, remoteViews, View.GONE)
        }.start()
    }

    private fun loadBackgroundStates(context: Context, remoteViews: RemoteViews) {
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)

        val idLists = listOf(listOf(R.string.ingang1, R.id.ingang1, R.color.colorPrimary)
                , listOf(R.string.ingang2, R.id.ingang2, R.color.colorPrimary)
                , listOf(R.string.club, R.id.club, R.color.colorPrimary)
                , listOf(R.string.etc, R.id.etc, R.color.colorPrimary)
                , listOf(R.string.bathroom, R.id.bathroom, R.drawable.bottom_rounded_background))

        idLists.forEach {
            if (preference.getBoolean(context.getString(it[0]), false)) {
                remoteViews.setInt(it[1], "setBackgroundResource", it[2])
                remoteViews.setTextColor(it[1], Color.WHITE)

            } else {
                remoteViews.setInt(it[1], "setBackgroundResource", android.R.color.transparent)
                remoteViews.setTextColor(it[1], Color.BLACK)
            }
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun switchState(context: Context, remoteViews: RemoteViews, viewId: Int) {
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        val identity = preference.getString("identity", null)
        if (identity == null) {
            loadStateFromServer(context, remoteViews)
            return
        }
        val klass = JSONParser.parseFromArray(identity, 0, "serial").subSequence(1, 2).toString().toInt()
        val name = preference.getString("name", "")

        if (isEnabled(context, viewId)) {
            deleteName(context, remoteViews, klass, name, viewId)
        } else {
            if (viewId == R.id.etc) {
                ReasonDialogActivity.callback = {
                    enterName(context, remoteViews, klass, preference.getString("name", ""), viewId, it)
                }

                val intent = Intent(context, ReasonDialogActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                enterName(context, remoteViews, klass, name, viewId)
            }
        }
    }

    private fun isEnabled(context: Context, viewId: Int): Boolean {
        val stringId = when (viewId) {
            R.id.ingang1 -> R.string.ingang1
            R.id.ingang2 -> R.string.ingang2
            R.id.club -> R.string.club
            R.id.etc -> R.string.etc
            R.id.bathroom -> R.string.bathroom
            else -> return false
        }

        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        return preference.getBoolean(context.getString(stringId), false)
    }

    private fun enterName(context: Context, remoteViews: RemoteViews, klass: Int, name: String?, id: Int, reason: String = "") {
        if (isNameExist(context, id)) {
            Toast.makeText(context, R.string.name_exist, Toast.LENGTH_LONG).show()
            return
        }
        Thread {
            setErrorVisibility(context, remoteViews, View.GONE)
            setTeacherVisibility(context, remoteViews, View.GONE)
            setProgressBarVisibility(context, remoteViews, View.VISIBLE)
            try {
                if (name == null) throw Exception()
                val service = SpreadsheetHelper.getService(context)
                var range = when (id) {
                    R.id.ingang1 -> "${klass}반!C2:C30"
                    R.id.ingang2 -> "${klass}반!D2:D30"
                    R.id.club -> "${klass}반!E2:E30"
                    R.id.etc -> "${klass}반!F2:F30"
                    R.id.bathroom -> "${klass}반!A10:A30"
                    else -> throw Exception()
                }
                val currentList = SpreadsheetHelper.getValues(service, range)
                val size = currentList?.size ?: 0
                val margin = if (id == R.id.bathroom) 10 else 2
                range = range.substring(0, 4) + (margin + size)

                val values = if (reason.isBlank()) {
                    ValueRange().setValues(listOf(listOf(name)))
                } else {
                    ValueRange().setValues(listOf(listOf("$reason - $name")))
                }
                SpreadsheetHelper.updateValues(service, range, values)
                loadStateFromServer(context, remoteViews)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, remoteViews, View.VISIBLE)
                loadStateFromServer(context, remoteViews, true)
            }
        }.start()
    }

    private fun deleteName(context: Context, remoteViews: RemoteViews, klass: Int, name: String?, id: Int) {
        Thread {
            setErrorVisibility(context, remoteViews, View.GONE)
            setTeacherVisibility(context, remoteViews, View.GONE)
            setProgressBarVisibility(context, remoteViews, View.VISIBLE)
            try {
                if (name == null) throw Exception()
                val range = when (id) {
                    R.id.ingang1 -> "${klass}반!C2:C30"
                    R.id.ingang2 -> "${klass}반!D2:D30"
                    R.id.club -> "${klass}반!E2:E30"
                    R.id.etc -> "${klass}반!F2:F30"
                    R.id.bathroom -> "${klass}반!A10:A30"
                    else -> ""
                }
                SpreadsheetHelper.deleteValueInRange(SpreadsheetHelper.getService(context), range, name, true)
                loadStateFromServer(context, remoteViews)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, remoteViews, View.VISIBLE)
                loadStateFromServer(context, remoteViews, true)
            }
        }.start()
    }

    private fun isNameExist(context: Context, viewId: Int): Boolean {
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        val ingang1Enabled = preference.getBoolean(context.getString(R.string.ingang1), false)
        val ingang2Enabled = preference.getBoolean(context.getString(R.string.ingang2), false)
        val clubEnabled = preference.getBoolean(context.getString(R.string.club), false)
        val etcEnabled = preference.getBoolean(context.getString(R.string.etc), false)
        val bathroomEnabled = preference.getBoolean(context.getString(R.string.bathroom), false)

        if (viewId == R.id.bathroom && !bathroomEnabled) return false
        else if (viewId == R.id.bathroom && bathroomEnabled) return true
        if (viewId != R.id.ingang2 && ingang1Enabled) return true
        if (viewId != R.id.ingang1 && ingang2Enabled) return true
        if (clubEnabled || etcEnabled) return true
        return false
    }

    private fun setLoginVisibility(context: Context, remoteViews: RemoteViews, visibility: Int) {
        remoteViews.apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.loginRequired))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setTeacherVisibility(context: Context, remoteViews: RemoteViews, visibility: Int) {
        remoteViews.apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.teacher_cannot_use))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setErrorVisibility(context: Context, remoteViews: RemoteViews, visibility: Int) {
        remoteViews.apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.error_occurred))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setProgressBarVisibility(context: Context, remoteViews: RemoteViews, visibility: Int) {
        remoteViews.setViewVisibility(R.id.progressBar, visibility)
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val dateFormat = SimpleDateFormat("MM/dd a h:mm")
        val date = Date()
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }
}