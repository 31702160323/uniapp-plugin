package com.xzh.musicnotification.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.xzh.musicnotification.service.NotificationReceiver

class PendingIntentInfo(val id: Int, val index: Int, val eXTRA: String) {

    companion object {
        @SuppressLint("UnspecifiedImmutableFlag")
        fun addOnClickPendingIntents(
            views: RemoteViews,
            context: Context,
            vararg pendingIntentInfoList: PendingIntentInfo
        ) {
            for (item in pendingIntentInfoList) {
                val intent = Intent(context.packageName + NotificationReceiver.ACTION_STATUS_BAR)
                intent.putExtra(
                    NotificationReceiver.EXTRA,
                    item.eXTRA
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    views.setOnClickPendingIntent(
                        item.id,
                        PendingIntent.getBroadcast(
                            context,
                            item.index,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                } else {
                    views.setOnClickPendingIntent(
                        item.id,
                        PendingIntent.getBroadcast(
                            context,
                            item.index,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                }
            }
        }
    }
}