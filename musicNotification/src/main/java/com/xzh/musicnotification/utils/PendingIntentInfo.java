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

    public PendingIntentInfo(int id, int index){
        this.Id = id;
        this.Index = index;
    }

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

    public static void openAppIntent(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList){
        for (PendingIntentInfo item: pendingIntentInfoList) {
            Intent intent = new Intent("io.dcloud.PandoraEntry");
            intent.setClassName(context, "io.dcloud.PandoraEntryActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
