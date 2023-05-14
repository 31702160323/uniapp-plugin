package com.xzh.musicnotification.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.xzh.musicnotification.service.NotificationReceiver;

public class PendingIntentInfo {
    private final int Id;
    private final int Index;
    private final String EXTRA;

    public PendingIntentInfo(int id, int index, String EXTRA){
        this.Id = id;
        this.Index = index;
        this.EXTRA = EXTRA;
    }

    public int getId() {
        return Id;
    }

    public int getIndex() {
        return Index;
    }

    public String getEXTRA() {
        return EXTRA;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static void addOnClickPendingIntents(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList){
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent intent = new Intent(context.getPackageName() + NotificationReceiver.ACTION_STATUS_BAR);
            intent.putExtra(NotificationReceiver.EXTRA,
                    item.getEXTRA());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setOnClickPendingIntent( item.getId(),
                        PendingIntent.getBroadcast(context, item.getIndex(), intent, PendingIntent.FLAG_IMMUTABLE));
            } else {
                views.setOnClickPendingIntent( item.getId(),
                        PendingIntent.getBroadcast(context, item.getIndex(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }
}
