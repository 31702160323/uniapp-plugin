package com.xzh.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.taobao.weex.utils.WXViewUtils;

import java.util.Objects;

import io.dcloud.feature.uniapp.utils.UniResourceUtils;

/**
 * Implementation of App Widget functionality.
 */
public class MusicWidget extends AppWidgetProvider {

    private boolean xzhFavour;

    public static final class PendingIntentInfo {
        private final int Id;
        private final int Index;
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
        this.openAppIntent(views, context, new PendingIntentInfo(R.id.image_view, 0));
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

        if (xzhFavour) {
            views.setViewVisibility(R.id.favourite_view, View.VISIBLE);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent);
        if (intent == null ||
                !"com.xzh.widget.MusicWidget".equals(intent.getAction()) ||
                !context.getPackageName().equals(intent.getStringExtra("packageName"))
        ) {
            return;
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

        //打开应用
        this.openAppIntent(views, context, new PendingIntentInfo(R.id.image_view, 0));
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

        switch (Objects.requireNonNull(intent.getStringExtra("type"))) {
            case "update":
                Log.d("XZH-musicNotification", "onReceive: " + intent.getStringExtra("songName"));
                views.setTextViewText(R.id.title_view, intent.getStringExtra("songName"));
                views.setTextViewText(R.id.tip_view, intent.getStringExtra("artistsName"));

                AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.image_view, views, new ComponentName(context, MusicWidget.class));

                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(intent.getStringExtra("picUrl"))
                        .sizeMultiplier(0.8f)
                        .override(WXViewUtils.dip2px(70), WXViewUtils.dip2px(70))
                        .format(DecodeFormat.PREFER_RGB_565)
                        .into(appWidgetTarget);
                break;
            case "playOrPause":
                if (intent.getBooleanExtra("playing", false)) {
                    views.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
                } else {
                    views.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
                }
                break;
            case "favour":
                if (intent.getBooleanExtra("favour", false)) {
                    views.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
                } else {
                    views.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
                }
                break;
            case "bg":
                if (intent.getStringExtra("bg") != null) {
                    views.setInt(R.id.bg_view, "setBackgroundColor", UniResourceUtils.getColor(intent.getStringExtra("bg")));
                }
                if (intent.getStringExtra("title") != null) {
                    views.setInt(R.id.title_view, "setTextColor", UniResourceUtils.getColor(intent.getStringExtra("title")));
                }
                if (intent.getStringExtra("tip") != null) {
                    views.setInt(R.id.tip_view, "setTextColor", UniResourceUtils.getColor(intent.getStringExtra("tip")));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(intent.getStringExtra("type")));
        }
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MusicWidget.class), views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            xzhFavour = info.metaData.getBoolean("xzh_favour");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // 第一个widget被创建时调用
    @Override
    public void onEnabled(Context context) {
        // 在第一个 widget 被创建时，开启服务
        super.onEnabled(context);
    }

    // 最后一个widget被删除时调用
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
    }
}

