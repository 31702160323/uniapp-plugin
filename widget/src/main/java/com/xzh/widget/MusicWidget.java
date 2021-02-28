package com.xzh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.facebook.common.internal.ByteStreams.copy;

/**
 * Implementation of App Widget functionality.
 */
public class MusicWidget extends AppWidgetProvider {

    class PendingIntentInfo {
        private int Id;
        private int Index;
        private String EXTRA;

        public PendingIntentInfo(int id, int index) {
            this.Id = id;
            this.Index = index;
        }

        public PendingIntentInfo(int id, int index, String EXTRA) {
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
    }

    public void openAppIntent(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList) {
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent intent = new Intent("io.dcloud.PandoraEntry");
            intent.setClassName(context, "io.dcloud.PandoraEntryActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex(), intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    public void addOnClickPendingIntents(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList) {
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent playIntent = new Intent(context.getPackageName() + ".NOTIFICATION_ACTIONS");
            playIntent.putExtra("extra",
                    item.getEXTRA());
            views.setOnClickPendingIntent(item.getId(),
                    PendingIntent.getBroadcast(context, item.getIndex(), playIntent, PendingIntent.FLAG_UPDATE_CURRENT));
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

    private void initWidget(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        this.addOnClickPendingIntents(views, context,
                //点击播放按钮要发送的广播
                new PendingIntentInfo(R.id.play_view, 1, "play_pause"),
                //点击上一首按钮要发送的广播
                new PendingIntentInfo(R.id.previous_view, 2, "play_previous"),
                //点击下一首按钮要发送的广播
                new PendingIntentInfo(R.id.next_view, 3, "play_next"),
                //点击收藏按钮要发送的广播
                new PendingIntentInfo(R.id.favourite_view, 4, "play_favourite")
        );

        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);
        // Instruct the widget manager to update the widget
        mAppWidgetManager.updateAppWidget(new ComponentName(context, MusicWidget.class), views);
    }

    /**
     * 得到本地或者网络上的bitmap url - 网络或者本地图片的绝对路径,比如:
     * A.网络路径: url="http://blog.foreverlove.us/girl2.png" ;
     * B.本地路径:url="file://mnt/sdcard/photo/image.png";
     * C.支持的图片格式 ,png, jpg,bmp,gif等等
     *
     * @param path 图片路径
     * @return Bitmap
     */
    public Bitmap GetLocalOrNetBitmap(String path) {
        Bitmap bitmap;
        InputStream in;
        BufferedOutputStream out;
        try {
            in = new BufferedInputStream(new URL(path).openStream(), 2 * 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 2 * 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null ||
                !"com.xzh.widget.MusicWidget".equals(intent.getAction()) ||
                !context.getPackageName().equals(intent.getStringExtra("packageName"))
        ) {
            return;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);
        AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(context);
        switch (intent.getStringExtra("type")) {
            case "update":
                views.setTextViewText(R.id.title_view, intent.getStringExtra("songName"));
                views.setTextViewText(R.id.tip_view, intent.getStringExtra("artistsName"));
                views.setImageViewBitmap(R.id.image_view, this.GetLocalOrNetBitmap(intent.getStringExtra("picUrl")));
                break;
            case "playOrPause":
                if (intent.getBooleanExtra("playing", false)) {
                    views.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
                } else {
                    views.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
                }
                break;
            case "favour":
                views.setViewVisibility(R.id.favourite_view, View.VISIBLE);
                if (intent.getBooleanExtra("favour", false)) {
                    views.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
                } else {
                    views.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
                }
                break;
            case "initWidget":
                initWidget(context);
                break;
            default:
                this.openAppIntent(views, context,
                        new PendingIntentInfo(R.id.image_view, 0),
                        new PendingIntentInfo(R.id.play_view, 1),
                        new PendingIntentInfo(R.id.previous_view, 2),
                        new PendingIntentInfo(R.id.next_view, 3),
                        new PendingIntentInfo(R.id.favourite_view, 4)
                );
                break;
        }
        mAppWidgetManager.updateAppWidget(new ComponentName(context, MusicWidget.class), views);
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

