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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.taobao.weex.utils.WXViewUtils;

import java.util.Map;
import java.util.Objects;

import io.dcloud.PandoraEntryActivity;
import io.dcloud.feature.uniapp.utils.UniResourceUtils;

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

        Log.d("TAG", "invoke: ");

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

    public void openAppIntent(RemoteViews views, Context context, PendingIntentInfo... pendingIntentInfoList) {
        for (PendingIntentInfo item : pendingIntentInfoList) {
            Intent intent = new Intent(context, PandoraEntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            views.setOnClickPendingIntent(item.getId(), PendingIntent.getActivity(context, item.getIndex() + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
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
        if ("com.xzh.widget.MusicWidget".equals(intent.getAction()) && context.getPackageName().equals(intent.getPackage())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_widget);

            switch (Objects.requireNonNull(intent.getStringExtra("type"))) {
                case "update":
                    views.setTextViewText(R.id.title_view, intent.getStringExtra("songName"));
                    views.setTextViewText(R.id.tip_view, intent.getStringExtra("artistsName"));
                    if (intent.getBooleanExtra("favour", false)) {
                        views.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_loved);
                    } else {
                        views.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
                    }

                    AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.image_view, views, new ComponentName(context, MusicWidget.class));

                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(intent.getStringExtra("picUrl"))
                            .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(5)))
                            .sizeMultiplier(0.8f)
                            .override(WXViewUtils.dip2px(75), WXViewUtils.dip2px(75))
                            .format(DecodeFormat.PREFER_RGB_565)
                            .into(appWidgetTarget);
                    break;
                case "playOrPause":
                    if (intent.getBooleanExtra("playing", false)) {
                        views.setImageViewResource(R.id.play_view, R.drawable.note_btn_pause_white);
                    } else {
                        views.setImageViewResource(R.id.play_view, R.drawable.note_btn_play_white);
                    }
                    break;
                case "favour":
                    Log.d("TAG", "onReceive: favour" + intent.getBooleanExtra("favour", false));
                    if (intent.getBooleanExtra("favour", false)) {
                        views.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_loved);
                    } else {
                        views.setImageViewResource(R.id.favourite_view, R.drawable.note_btn_love_white);
                    }
                    break;
                case "bg":
                    if (intent.getStringExtra("themeColor") != null) {
                        views.setImageViewBitmap(R.id.bg_view, getBgBitmap(context, UniResourceUtils.getColor(intent.getStringExtra("themeColor"))));
                    }
                    if (intent.getStringExtra("titleColor") != null) {
                        views.setInt(R.id.title_view, "setTextColor", UniResourceUtils.getColor(intent.getStringExtra("titleColor")));
                    }
                    if (intent.getStringExtra("artistColor") != null) {
                        views.setInt(R.id.tip_view, "setTextColor", UniResourceUtils.getColor(intent.getStringExtra("artistColor")));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(intent.getStringExtra("type")));
            }
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MusicWidget.class), views);
        }
        super.onReceive(context, intent);
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

