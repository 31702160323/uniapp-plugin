package com.xzh.musicnotification;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.xzh.musicnotification.service.NotificationReceiver;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.Utils;
import com.xzh.musicnotification.view.SlidingFinishLayout;

import java.lang.ref.WeakReference;

import io.dcloud.feature.uniapp.utils.UniUtils;

public class LockActivityV2 extends AppCompatActivity implements SlidingFinishLayout.OnSlidingFinishListener, View.OnClickListener, PlayServiceV2.OnClickListener {
    private int mWidth;
    private int mHeight;
    private boolean showFavour;
    private TextView tvAudio;
    private TextView tvAudioName;
    private ImageView lockDate;
    private ImageView playView;
    private ImageView favouriteView;
    private ServiceConnection connection;
    private WeakReference<PlayServiceV2.ServiceBinder> mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }
        Utils.fullScreen(this);
        setContentView(R.layout.activity_lock);

        initView();

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mBinder = new WeakReference<>((PlayServiceV2.ServiceBinder) iBinder);
                mBinder.get().setActivity(LockActivityV2.this);
                if (UniUtils.isUiThread()) {
                    update(mBinder.get().getSongData());
                } else {
                    runOnUiThread(() -> update(mBinder.get().getSongData()));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        bindService(new Intent(this, PlayServiceV2.class), connection, BIND_AUTO_CREATE);
    }

    private void initView() {
        SlidingFinishLayout vLockRoot = findViewById(R.id.lock_root);
        vLockRoot.setOnSlidingFinishListener(this);

        lockDate = findViewById(R.id.iv_audio);
        tvAudioName = findViewById(R.id.tv_audio_name);
        tvAudio = findViewById(R.id.tv_audio);

        favouriteView = findViewById(R.id.favourite_view);

        ApplicationInfo info = Utils.getApplicationInfo(this);
        if (info != null) {
            showFavour = info.metaData.getBoolean(Global.SHOW_FAVOUR);
            if (showFavour) favouriteView.setVisibility(View.VISIBLE);
        }

        playView = findViewById(R.id.play_view);
        favouriteView.setOnClickListener(this);
        playView.setOnClickListener(this);
        findViewById(R.id.previous_view).setOnClickListener(this);
        findViewById(R.id.next_view).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String EXTRA_TYPE = "";
        String eventName = Global.EVENT_MUSIC_NOTIFICATION_ERROR;
        JSONObject data = new JSONObject();
        data.put("message", "更新锁屏页成功");
        data.put("code", 0);

        final int viewId = view.getId();
        final int[] ids = new int[]{R.id.previous_view, R.id.next_view, R.id.favourite_view, R.id.play_view};
        final String[] EXTRAS = new String[]{
                NotificationReceiver.EXTRA_PRE,
                NotificationReceiver.EXTRA_NEXT,
                NotificationReceiver.EXTRA_FAV,
                NotificationReceiver.EXTRA_PLAY
        };

        for (int i = 0; i < ids.length; i++) {
            if (viewId != ids[i]) continue;
            EXTRA_TYPE = EXTRAS[i];
        }

        switch (EXTRA_TYPE) {
            case NotificationReceiver.EXTRA_PRE:
                eventName = Global.EVENT_MUSIC_NOTIFICATION_PREVIOUS;
                break;
            case NotificationReceiver.EXTRA_NEXT:
                eventName = Global.EVENT_MUSIC_NOTIFICATION_NEXT;
                break;
            case NotificationReceiver.EXTRA_FAV:
                mBinder.get().favour(!mBinder.get().getFavour());
                eventName = Global.EVENT_MUSIC_NOTIFICATION_FAVOURITE;
                break;
            case NotificationReceiver.EXTRA_PLAY:
                mBinder.get().playOrPause(!mBinder.get().getPlaying());
                eventName = Global.EVENT_MUSIC_NOTIFICATION_PAUSE;
                break;
            default:
                data.put("message", "更新锁屏页失败");
                data.put("code", -6);
                break;
        }

        mBinder.get().fireGlobalEventCallback(eventName, data);
    }

    /**
     * 重写物理返回键，使不能回退
     */
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    /**
     * 滑动销毁锁屏页面
     */
    @Override
    public void onSlidingFinish() {
        finish();
    }

    @Override
    public void playOrPause(boolean playing) {
        if (playing) {
            playView.setImageResource(R.drawable.note_btn_pause_white);
        } else {
            playView.setImageResource(R.drawable.note_btn_play_white);
        }
    }

    @Override
    public void favour(boolean isFavour) {
        if (!showFavour) return;
        if (isFavour) {
            favouriteView.setImageResource(R.drawable.note_btn_loved);
        } else {
            favouriteView.setImageResource(R.drawable.note_btn_love_white);
        }
    }

    @Override
    public void update(JSONObject options) {
        if (UniUtils.isUiThread()) {
            updateUI(options);
        } else {
            runOnUiThread(() -> updateUI(options));
        }
    }

    private void updateUI(JSONObject options) {
        if (options.getString(Global.KEY_SONG_NAME) != null) {
            tvAudioName.setText(options.getString(Global.KEY_SONG_NAME));
        }
        if (options.getString(Global.KEY_ARTISTS_NAME) != null) {
            tvAudio.setText(options.getString(Global.KEY_ARTISTS_NAME));
        }
        favour(mBinder.get().getFavour());
        playOrPause(mBinder.get().getPlaying());

        Glide.with(this.getApplicationContext())
                .asBitmap()
                .load(options.getString("picUrl"))
                .sizeMultiplier(0.8f)
                .override(mWidth, mHeight)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        lockDate.setImageBitmap(changeAlpha(resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
//                .into(lockDate);
    }

    public Bitmap changeAlpha(Bitmap bitmap) {
        try {
            int w = bitmap.getWidth();
            int h = bitmap .getHeight();

            int height = mHeight * w / mWidth;
            Bitmap result = Bitmap.createBitmap(w, height, Bitmap.Config.ARGB_8888);

            int r,g,b,a,color;

            int[] oldPx = new int[w * h];
            int[] newPx = new int[w * height];

            bitmap.getPixels(oldPx, 0, w, 0, 0, w, h);

            lockDate.setBackgroundColor(oldPx[100]);

            for (int x = 0; x < w; x++) {
                for (int y = 0; y < height; y++) {
                    if (y < h) {
                        color = oldPx[x + w * y];
                        if (w / h - y > 255) {
                            a = Color.alpha(color);
                        } else {
                            a = h - y;
                        }
                    } else {
                        color = oldPx[100];
                        a = Color.alpha(color);
                    }
                    r = Color.red(color);
                    g = Color.green(color);
                    b = Color.blue(color);

                    a = (a > 255 ? 255 : Math.max(a, 0));
                    newPx[x + w * y] = Color.argb(a,r, g, b);
                }
            }
            result.setPixels(newPx, 0, w, 0, 0, w, height);
            return result;
        } catch (Exception e) {
            return bitmap;
        }
    }
}