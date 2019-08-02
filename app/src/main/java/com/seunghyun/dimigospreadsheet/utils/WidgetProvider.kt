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
import com.seunghyun.dimigospreadsheet.R

class WidgetProvider : AppWidgetProvider() {
    companion object {
        const val WIDGET_ACTION = "com.seunghyun.dimigospreadsheet.WIDGET_ACTION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (context == null || intent == null) return

        if (intent.action == WIDGET_ACTION) {
            val viewId = intent.getIntExtra("viewId", 0)
            switchBackgroundState(context, viewId)
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null) return

        loadBackgroundStates(context)

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter).apply {
            setOnClickPendingIntent(R.id.ingang1, getButtonClickIntent(context, R.id.ingang1))
            setOnClickPendingIntent(R.id.ingang2, getButtonClickIntent(context, R.id.ingang2))
            setOnClickPendingIntent(R.id.club, getButtonClickIntent(context, R.id.club))
            setOnClickPendingIntent(R.id.etc, getButtonClickIntent(context, R.id.etc))
            setOnClickPendingIntent(R.id.bathroom, getButtonClickIntent(context, R.id.bathroom))
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

    private fun loadBackgroundStates(context: Context) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)

        val idLists = listOf(listOf(R.string.ingang1, R.id.ingang1, R.id.ingang1Background)
                , listOf(R.string.ingang2, R.id.ingang2, R.id.ingang2Background)
                , listOf(R.string.club, R.id.club, R.id.clubBackground)
                , listOf(R.string.etc, R.id.etc, R.id.etcBackground)
                , listOf(R.string.bathroom, R.id.bathroom, R.id.bathroomBackground))

        idLists.forEach {
            if (preference.getBoolean(context.getString(it[0]), false)) {
                remoteViews.setViewVisibility(it[2], View.VISIBLE)
                remoteViews.setTextColor(it[1], Color.WHITE)

            } else {
                remoteViews.setViewVisibility(it[2], View.INVISIBLE)
                remoteViews.setTextColor(it[1], Color.BLACK)
            }
        }
        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }

    private fun switchBackgroundState(context: Context, viewId: Int) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_enter)
        val preference = context.getSharedPreferences(context.getString(R.string.preference_app_setting), Context.MODE_PRIVATE)
        val editor = preference.edit()

        val (stringId, backgroundId) = when (viewId) {
            R.id.ingang1 -> listOf(R.string.ingang1, R.id.ingang1Background)
            R.id.ingang2 -> listOf(R.string.ingang2, R.id.ingang2Background)
            R.id.club -> listOf(R.string.club, R.id.clubBackground)
            R.id.etc -> listOf(R.string.etc, R.id.etcBackground)
            R.id.bathroom -> listOf(R.string.bathroom, R.id.bathroomBackground)
            else -> return
        }

        if (preference.getBoolean(context.getString(stringId), false)) {
            remoteViews.setViewVisibility(backgroundId, View.INVISIBLE)
            remoteViews.setTextColor(viewId, Color.BLACK)
            editor.putBoolean(context.getString(stringId), false).apply()
        } else {
            remoteViews.setViewVisibility(backgroundId, View.VISIBLE)
            remoteViews.setTextColor(viewId, Color.WHITE)
            editor.putBoolean(context.getString(stringId), true).apply()
        }

        AppWidgetManager.getInstance(context).updateAppWidget(ComponentName(context, WidgetProvider::class.java), remoteViews)
    }
}