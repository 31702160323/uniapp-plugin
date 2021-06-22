package com.xzh.musicnotification.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.xzh.musicnotification.service.PlayServiceV2;

public class PendingIntentInfo {
    private int Id;
    private int Index;
    private String EXTRA;

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

    public static void addOnClickPendingIntents(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList){
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent playIntent = new Intent(PlayServiceV2.NotificationReceiver.ACTION_STATUS_BAR);
            playIntent.putExtra(PlayServiceV2.NotificationReceiver.EXTRA,
                    item.getEXTRA());
            views.setOnClickPendingIntent( item.getId(),
                    PendingIntent.getBroadcast(context, item.getIndex(), playIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
