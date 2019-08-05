package com.seunghyun.dimigospreadsheet.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.google.api.services.sheets.v4.model.ValueRange
import com.seunghyun.dimigospreadsheet.R
import com.seunghyun.dimigospreadsheet.activities.ReasonDialogActivity
import com.seunghyun.dimigospreadsheet.activities.SplashActivity
import com.seunghyun.dimigospreadsheet.models.SheetValue

class WidgetProvider : AppWidgetProvider() {
    companion object {
        const val WIDGET_ACTION = "com.seunghyun.dimigospreadsheet.WIDGET_ACTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null || intent == null) return

        if (intent.action == WIDGET_ACTION) {
            val viewId = intent.getIntExtra("viewId", 0)
            if (viewId == R.id.refreshButton) loadStateFromServer(context)
            else switchState(context, viewId)
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null) return

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setOnClickPendingIntent(R.id.ingang1, getButtonClickIntent(context, R.id.ingang1))
            setOnClickPendingIntent(R.id.ingang2, getButtonClickIntent(context, R.id.ingang2))
            setOnClickPendingIntent(R.id.club, getButtonClickIntent(context, R.id.club))
            setOnClickPendingIntent(R.id.etc, getButtonClickIntent(context, R.id.etc))
            setOnClickPendingIntent(R.id.bathroom, getButtonClickIntent(context, R.id.bathroom))
            setOnClickPendingIntent(R.id.refreshButton, getButtonClickIntent(context, R.id.refreshButton))
            setOnClickPendingIntent(R.id.openAppButton, PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java), 0))
        }

        loadStateFromServer(context)

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    private fun getButtonClickIntent(context: Context, @IdRes id: Int): PendingIntent {
        val intent = Intent(context, WidgetProvider::class.java).apply {
            action = WIDGET_ACTION
            putExtra("viewId", id)
        }
        return PendingIntent.getBroadcast(context, id, intent, 0)
    }

    private fun loadStateFromServer(context: Context, isErrorContains: Boolean = false) {
        Thread {
            if (!isErrorContains) {
                setErrorVisibility(context, View.GONE)
            }
            setProgressBarVisibility(context, View.VISIBLE)
            try {
                val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
                val editor = preference.edit()
                val userType = preference.getString("userType", null)
                val name = preference.getString("name", null)
                val identity = preference.getString("identity", null)
                if (userType == null || name == null || identity == null) throw LoginRequiredException()
                if (userType != "S") throw TeacherCannotUseException()
                val klass = JSONParser.parseFromArray(identity, 0, "serial").subSequence(1, 2).toString().toInt()

                val sheetValue = SheetValue(SpreadsheetHelper.getValues(SpreadsheetHelper.getService(context), "${klass}반!1:30"))
                editor.putBoolean(context.getString(R.string.ingang1), sheetValue.ingang1.contains(name))
                editor.putBoolean(context.getString(R.string.ingang2), sheetValue.ingang2.contains(name))
                editor.putBoolean(context.getString(R.string.club), sheetValue.club.contains(name))
                editor.putBoolean(context.getString(R.string.bathroom), sheetValue.bathroom.contains(name))

                var cnt = 0
                sheetValue.etc.forEach {
                    if (it.contains(name)) cnt++
                }
                editor.putBoolean(context.getString(R.string.etc), cnt != 0)

                editor.apply()

                loadBackgroundStates(context)

            } catch (e: LoginRequiredException) {
                e.printStackTrace()
                setLoginVisibility(context, View.VISIBLE)
            } catch (e: TeacherCannotUseException) {
                setTeacherVisibility(context, View.VISIBLE)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, View.VISIBLE)
            }
            setProgressBarVisibility(context, View.GONE)
        }.start()
    }

    private fun loadBackgroundStates(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
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

    private fun switchState(context: Context, viewId: Int) {
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        val identity = preference.getString("identity", null)
        if (identity == null) {
            loadStateFromServer(context)
            return
        }
        val klass = JSONParser.parseFromArray(identity, 0, "serial").subSequence(1, 2).toString().toInt()
        val name = preference.getString("name", "")

        if (isEnabled(context, viewId)) {
            deleteName(context, klass, name, viewId)
        } else {
            if (viewId == R.id.etc) {
                ReasonDialogActivity.callback = {
                    enterName(context, klass, preference.getString("name", ""), viewId, it)
                }

                val intent = Intent(context, ReasonDialogActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                enterName(context, klass, name, viewId)
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

    private fun enterName(context: Context, klass: Int, name: String?, id: Int, reason: String = "") {
        Thread {
            setErrorVisibility(context, View.GONE)
            setTeacherVisibility(context, View.GONE)
            setProgressBarVisibility(context, View.VISIBLE)
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
                loadStateFromServer(context)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, View.VISIBLE)
                loadStateFromServer(context, true)
            }
        }.start()
    }

    private fun deleteName(context: Context, klass: Int, name: String?, id: Int) {
        Thread {
            setErrorVisibility(context, View.GONE)
            setTeacherVisibility(context, View.GONE)
            setProgressBarVisibility(context, View.VISIBLE)
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
                SpreadsheetHelper.deleteValueInRange(SpreadsheetHelper.getService(context), range, name)
                loadStateFromServer(context)
            } catch (e: Exception) {
                e.printStackTrace()
                setErrorVisibility(context, View.VISIBLE)
                loadStateFromServer(context, true)
            }
        }.start()
    }

    private fun setLoginVisibility(context: Context, visibility: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.loginRequired))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setTeacherVisibility(context: Context, visibility: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.teacher_cannot_use))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setErrorVisibility(context: Context, visibility: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.error_occurred))
            setViewVisibility(R.id.errorTV, visibility)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setProgressBarVisibility(context: Context, visibility: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        remoteViews.setViewVisibility(R.id.progressBar, visibility)
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }
}