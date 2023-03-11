package com.xzh.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;

import java.util.Map;
import java.util.Objects;

public class MusicWidget extends AppWidgetProvider {

    private boolean showFavour;
    private int themeColor;
    private int titleColor;
    private int artistColor;

    @SuppressLint("WrongConstant")
    public static void invoke(Context context, String type, Map<String, Object> options) {
        Intent intent = new Intent("com.xzh.widget.MusicWidget");
        intent.addFlags(0x01000000);
        intent.setPackage(context.getPackageName());
        intent.putExtra("type", type);

        for (String key : options.keySet()) {
            Object value = options.get(key);
            if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof String) {
                intent.putExtra(key, (String) value);
            } else if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (float) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (double) value);
            }
        }

        context.sendOrderedBroadcast(intent, null);
    }

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

    @SuppressLint("UnspecifiedImmutableFlag")
    public void openAppIntent(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList) {
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(context.getPackageName(), "io.dcloud.PandoraEntryActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex() + 1, intent, PendingIntent.FLAG_IMMUTABLE));
            } else {
                views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex() + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void addOnClickPendingIntents(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList) {
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent playIntent = new Intent(context.getPackageName() + ".NOTIFICATION_ACTIONS");
            playIntent.putExtra("extra",
                    item.getEXTRA());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setOnClickPendingIntent(item.getId(),
                        PendingIntent.getBroadcast(context, item.getIndex(), playIntent, PendingIntent.FLAG_IMMUTABLE));
            } else {
                views.setOnClickPendingIntent(item.getId(),
                        PendingIntent.getBroadcast(context, item.getIndex(), playIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            }
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

        if (showFavour) {
            views.setViewVisibility(R.id.favourite_view, View.VISIBLE);
        }
        if (themeColor != 0) {
            // 将转换好的Bitmap设置到ImageView上
            views.setImageViewBitmap(R.id.bg_view, getBgBitmap(context, themeColor));
        }
        if (titleColor != 0) {
            views.setInt(R.id.title_view, "setTextColor", titleColor);
        }
        if (artistColor != 0) {
            views.setInt(R.id.tip_view, "setTextColor", artistColor);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        super.onReceive(context, intent);
        if ("com.xzh.widget.MusicWidget".equals(intent.getAction()) && context.getPackageName().equals(intent.getPackage())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

            switch (Objects.requireNonNull(intent.getStringExtra("type"))) {
                case "update":
                    views.setTextViewText(R.id.title_view, intent.getStringExtra("songName"));
                    views.setTextViewText(R.id.tip_view, intent.getStringExtra("artistsName"));
                    views.setImageViewResource(R.id.favourite_view, intent.getBooleanExtra("favour", false) ? R.drawable.note_btn_loved : R.drawable.note_btn_love_white);

                    AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.image_view, views, new ComponentName(context, MusicWidget.class));

                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(intent.getStringExtra("picUrl"))
                            .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(5)))
                            .sizeMultiplier(0.8f)
                            .override(DrawableUtils.dip2px(75), DrawableUtils.dip2px(75))
                            .format(DecodeFormat.PREFER_RGB_565)
                            .into(appWidgetTarget);
                    break;
                case "playOrPause":
                    views.setImageViewResource(R.id.play_view, intent.getBooleanExtra("playing", false) ? R.drawable.note_btn_pause_white : R.drawable.note_btn_play_white);
                    break;
                case "favour":
                    views.setImageViewResource(R.id.favourite_view, intent.getBooleanExtra("favour", false) ? R.drawable.note_btn_loved : R.drawable.note_btn_love_white);
                    break;
                case "bg":
                    if (intent.getStringExtra("themeColor") != null) {
                        views.setImageViewBitmap(R.id.bg_view, getBgBitmap(context, Color.parseColor(intent.getStringExtra("themeColor"))));
                    }
                    if (intent.getStringExtra("titleColor") != null) {
                        views.setInt(R.id.title_view, "setTextColor", Color.parseColor(intent.getStringExtra("titleColor")));
                    }
                    if (intent.getStringExtra("artistColor") != null) {
                        views.setInt(R.id.tip_view, "setTextColor", Color.parseColor(intent.getStringExtra("artistColor")));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(intent.getStringExtra("type")));
            }
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MusicWidget.class), views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            showFavour = info.metaData.getBoolean("xzh_favour");
            themeColor = info.metaData.getInt("xzh_theme_color");
            titleColor = info.metaData.getInt("xzh_title_color");
            artistColor = info.metaData.getInt("xzh_artist_color");
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
        Intent intent = new Intent(context.getPackageName() + ".NOTIFICATION_ACTIONS");
        intent.putExtra("extra", "enabled");
        context.sendBroadcast(intent);
    }

    // 最后一个widget被删除时调用
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
    }

    private Bitmap getBgBitmap(Context context, int color) {
        // 透明度
        int alpha = 100;
        // 背景宽度
        int width = context.getResources().getDimensionPixelSize(R.dimen.dp_250);
        // 背景高度
        int height = context.getResources().getDimensionPixelSize(R.dimen.dp_70);
        // 圆角角度
//        int radius = context.getResources().getDimensionPixelSize(R.dimen.dp_5);
        int radius = 10;
        // 绘制背景
        Drawable drawable = DrawableUtils.roundShapeDrawable(color, alpha, width, height, radius);
        // 将绘制好的背景转换为Bitmap
        return DrawableUtils.drawableToBitmap(drawable);
    }
}

