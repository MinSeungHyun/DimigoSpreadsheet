package com.seunghyun.dimigospreadsheet.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
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
        Log.d("testing", "action: ${intent.action}, extra: ${intent.getIntExtra("viewId", 0)}")
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("testing", "onUpdate")
        if (context == null || appWidgetManager == null) return

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
}