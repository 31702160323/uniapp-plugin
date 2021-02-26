package com.xzh.musicnotification.view;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.xzh.musicnotification.R;
import com.xzh.musicnotification.utils.PendingIntentInfo;

/**
 * Implementation of App Widget functionality.
 */
public class MusicWidget extends AppWidgetProvider {

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        //打开应用
        PendingIntentInfo.openAppIntent(views, context,
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

