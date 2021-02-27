package com.xzh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class MusicWidget extends AppWidgetProvider {

    class PendingIntentInfo {
        private int Id;
        private int Index;

        public PendingIntentInfo(int id, int index){
            this.Id = id;
            this.Index = index;
        }

        public int getId() {
            return Id;
        }

        public int getIndex() {
            return Index;
        }
    }

    public void openAppIntent(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList){
        for (PendingIntentInfo item: pendingIntentInfoList) {
            Intent intent = new Intent("io.dcloud.PandoraEntry");
            intent.setClassName(context, "io.dcloud.PandoraEntryActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        //打开应用
        this.openAppIntent(views, context,
                new PendingIntentInfo(R.id.image_view, 0),
                new PendingIntentInfo(R.id.play_view, 1),
                new PendingIntentInfo(R.id.previous_view, 2),
                new PendingIntentInfo(R.id.next_view, 3),
                new PendingIntentInfo(R.id.favourite_view, 4)
        );

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // 第一个widget被创建时调用
    @Override
    public void onEnabled(Context context) {
        // 在第一个 widget 被创建时，开启服务
        Log.d("MusicNotificationModule", "onEnabled: 在第一个 widget 被创建时，开启服务");
//        PlayServiceV2.startMusicService(context);
        super.onEnabled(context);
    }

    // 最后一个widget被删除时调用
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        Log.d("MusicNotificationModule", "onDisabled: 在最后一个 widget 被删除时，终止服务");
//        PlayServiceV2.stopMusicService(context);
    }
}

