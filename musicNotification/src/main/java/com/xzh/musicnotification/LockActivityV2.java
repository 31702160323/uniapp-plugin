package com.xzh.musicnotification;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.view.SlidingFinishLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.utils.UniUtils;

public class LockActivityV2 extends AppCompatActivity implements SlidingFinishLayout.OnSlidingFinishListener, View.OnClickListener {

    private ImageView lockDate;
    private TextView tvAudioName;
    private TextView tvAudio;
    private ImageView favouriteView;
    private ImageView playView;
    private WeakReference<PlayServiceV2> mServiceV2;
    private ServiceConnection connection;
    private TimeChangeReceiver mReceiver;
    private int mWidth;
    private int mHeight;
    private boolean xzhFavour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        }
        fullScreen(this);
        setContentView(R.layout.activity_lock);

        initView();

        WindowManager windowManager = this.getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        mWidth = displayMetrics.widthPixels;
        mHeight = displayMetrics.heightPixels;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        mReceiver = new TimeChangeReceiver();
        registerReceiver(mReceiver, intentFilter);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mServiceV2 = new WeakReference<>(((PlayServiceV2.ServiceBinder) iBinder).getInstance());
                mServiceV2.get().setActivity(LockActivityV2.this);
                if (UniUtils.isUiThread()) {
                    updateUI(mServiceV2.get().getSongData());
                } else {
                    runOnUiThread(() -> updateUI(mServiceV2.get().getSongData()));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        bindService(new Intent(this, PlayServiceV2.class), connection, BIND_AUTO_CREATE);
    }

    /**
     * 通过设置全屏，设置状态栏透明
     *
     * @param activity Activity
     */
    public static void fullScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);

            } else {
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                attributes.flags |= flagTranslucentStatus;
                window.setAttributes(attributes);
            }
        }
    }

    private void initView() {
        SlidingFinishLayout vLockRoot = findViewById(R.id.lock_root);
        vLockRoot.setOnSlidingFinishListener(this);

        lockDate = findViewById(R.id.iv_audio);
        tvAudioName = findViewById(R.id.tv_audio_name);
        tvAudio = findViewById(R.id.tv_audio);

        favouriteView = findViewById(R.id.favourite_view);
        try {
            ApplicationInfo info = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            xzhFavour = info.metaData.getBoolean("xzh_favour");
            if (xzhFavour) favouriteView.setVisibility(View.VISIBLE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
        String eventName = "musicNotificationError";
        Map<String, Object> data = new HashMap<>();
        data.put("message", "更新锁屏页成功");
        data.put("code", 0);

        final int viewId = view.getId();
        final int[] ids = new int[]{R.id.previous_view, R.id.next_view, R.id.favourite_view, R.id.play_view};
        final String[] EXTRAS = new String[]{
                PlayServiceV2.NotificationReceiver.EXTRA_PRE,
                PlayServiceV2.NotificationReceiver.EXTRA_NEXT,
                PlayServiceV2.NotificationReceiver.EXTRA_FAV,
                PlayServiceV2.NotificationReceiver.EXTRA_PLAY
        };

        for (int i = 0; i < ids.length; i++) {
            if (viewId != ids[i]) continue;
            EXTRA_TYPE = EXTRAS[i];
        }

        switch (EXTRA_TYPE) {
            case PlayServiceV2.NotificationReceiver.EXTRA_PRE:
                eventName = "musicNotificationPrevious";
                break;
            case PlayServiceV2.NotificationReceiver.EXTRA_NEXT:
                eventName = "musicNotificationNext";
                break;
            case PlayServiceV2.NotificationReceiver.EXTRA_FAV:
                mServiceV2.get().Favour = !mServiceV2.get().Favour;
                mServiceV2.get().favour(mServiceV2.get().Favour);
                data.put("favourite", mServiceV2.get().Favour);
                eventName = "musicNotificationFavourite";
                break;
            case PlayServiceV2.NotificationReceiver.EXTRA_PLAY:
                mServiceV2.get().Playing = !mServiceV2.get().Playing;
                mServiceV2.get().playOrPause(mServiceV2.get().Playing);
                eventName = "musicNotificationPause";
                break;
            default:
                data.put("message", "更新锁屏页失败");
                data.put("code", -6);
                break;
        }

        mServiceV2.get().mWXSDKInstance.get().fireGlobalEventCallback(eventName, data);
    }

    /**
     * 重写物理返回键，使不能回退
     */
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onRestart() {
        unbindService(connection);
        unregisterReceiver(mReceiver);
        super.onRestart();
    }

    /**
     * 滑动销毁锁屏页面
     */
    @Override
    public void onSlidingFinish() {
        finish();
    }

    public void updateUI(final JSONObject options) {
        if (options.getString("songName") != null) {
            tvAudioName.setText(options.getString("songName"));
        }
        if (options.getString("artistsName") != null) {
            tvAudio.setText(options.getString("artistsName"));
        }
        favour(mServiceV2.get().Favour);
        playOrPause(mServiceV2.get().Playing);

        Glide.with(this.getApplicationContext())
                .asBitmap()
                .load(options.getString("picUrl"))
                .sizeMultiplier(0.8f)
                .override(mWidth, mHeight)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(lockDate);
    }

    public void playOrPause(boolean playing) {
        if (playing) {
            playView.setImageResource(R.mipmap.note_btn_pause_white);
        } else {
            playView.setImageResource(R.mipmap.note_btn_play_white);
        }
    }

    public void favour(boolean isFavour) {
        if (xzhFavour) {
            if (isFavour) {
                favouriteView.setImageResource(R.mipmap.note_btn_loved);
            } else {
                favouriteView.setImageResource(R.mipmap.note_btn_love_white);
            }
        }
    }

    private static class TimeChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}