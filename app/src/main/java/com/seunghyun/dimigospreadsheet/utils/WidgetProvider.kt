package com.seunghyun.dimigospreadsheet.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.seunghyun.dimigospreadsheet.R
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
            else switchBackgroundState(context, viewId)
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null) return

        loadStateFromServer(context)

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setOnClickPendingIntent(R.id.ingang1, getButtonClickIntent(context, R.id.ingang1))
            setOnClickPendingIntent(R.id.ingang2, getButtonClickIntent(context, R.id.ingang2))
            setOnClickPendingIntent(R.id.club, getButtonClickIntent(context, R.id.club))
            setOnClickPendingIntent(R.id.etc, getButtonClickIntent(context, R.id.etc))
            setOnClickPendingIntent(R.id.bathroom, getButtonClickIntent(context, R.id.bathroom))
            setOnClickPendingIntent(R.id.refreshButton, getButtonClickIntent(context, R.id.refreshButton))
        }
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
    }

    private fun getButtonClickIntent(context: Context, @IdRes id: Int): PendingIntent {
        val intent = Intent(context, WidgetProvider::class.java).apply {
            action = WIDGET_ACTION
            putExtra("viewId", id)
        }
        return PendingIntent.getBroadcast(context, id, intent, 0)
    }

    private fun loadStateFromServer(context: Context) {
        Thread {
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
            } catch (e: TeacherCannotUseException) {
                preventTeacher(context)
            } catch (e: Exception) {
                e.printStackTrace()
                errorOccurred(context)
            }
            Log.d("testing", "end")
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

    private fun switchBackgroundState(context: Context, viewId: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        val editor = preference.edit()

        val (stringId, backgroundResource) = when (viewId) {
            R.id.ingang1 -> listOf(R.string.ingang1, R.color.colorPrimary)
            R.id.ingang2 -> listOf(R.string.ingang2, R.color.colorPrimary)
            R.id.club -> listOf(R.string.club, R.color.colorPrimary)
            R.id.etc -> listOf(R.string.etc, R.color.colorPrimary)
            R.id.bathroom -> listOf(R.string.bathroom, R.drawable.bottom_rounded_background)
            else -> return
        }

        if (preference.getBoolean(context.getString(stringId), false)) {
            remoteViews.setInt(viewId, "setBackgroundResource", android.R.color.transparent)
            remoteViews.setTextColor(viewId, Color.BLACK)
            editor.putBoolean(context.getString(stringId), false).apply()
        } else {
            remoteViews.setInt(viewId, "setBackgroundResource", backgroundResource)
            remoteViews.setTextColor(viewId, Color.WHITE)
            editor.putBoolean(context.getString(stringId), true).apply()
        }

        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun preventTeacher(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.teacher_cannot_use))
            setViewVisibility(R.id.errorTV, View.VISIBLE)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun errorOccurred(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setTextViewText(R.id.errorTV, context.getString(R.string.error_occurred))
            setViewVisibility(R.id.errorTV, View.VISIBLE)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun setProgressBarVisibility(context: Context, visibility: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        remoteViews.setViewVisibility(R.id.progressBar, visibility)
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }
}