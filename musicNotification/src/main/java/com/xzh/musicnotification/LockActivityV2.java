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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.xzh.musicnotification.service.PlayServiceV2;
import com.xzh.musicnotification.utils.ImageUtils;
import com.xzh.musicnotification.view.SlidingFinishLayout;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.utils.UniLogUtils;

public class LockActivityV2 extends AppCompatActivity implements SlidingFinishLayout.OnSlidingFinishListener, View.OnClickListener {

    private ImageView lockDate;
    private TextView tvAudioName;
    private TextView tvAudio;
    private ImageView favouriteView;
    private ImageView playView;
    private PlayServiceV2 mServiceV2;
    private ServiceConnection connection;
    private TimeChangeReceiver mReceiver;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        fullScreen(this);
        setContentView(R.layout.activity_lock);

        initView();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        mReceiver = new TimeChangeReceiver();
        registerReceiver(mReceiver, intentFilter);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                PlayServiceV2.ServiceBinder serviceBinder = (PlayServiceV2.ServiceBinder) iBinder;
                mServiceV2 = serviceBinder.getInstance();
                mServiceV2.setActivity(new WeakReference<>(LockActivityV2.this));
                updateUI(mServiceV2.getSongData());
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
            boolean xzhFavour = info.metaData.getBoolean("xzh_favour");
            UniLogUtils.i("XZH-musicNotification","xzh_favour" + xzhFavour);
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
        data.put("success", "操作成功");
        data.put("code", 0);

        final int viewId = view.getId();
        final int[] ids = new int[]{ R.id.previous_view, R.id.next_view, R.id.favourite_view, R.id.play_view };
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
            case PlayServiceV2.NotificationReceiver.EXTRA_PLAY:
                mServiceV2.Playing = !mServiceV2.Playing;
                mServiceV2.playOrPause(mServiceV2.Playing);
                eventName = "musicNotificationPause";
                break;
            case PlayServiceV2.NotificationReceiver.EXTRA_FAV:
                mServiceV2.Favour = !mServiceV2.Favour;
                mServiceV2.favour(mServiceV2.Favour);
                data.put("favourite", mServiceV2.Favour);
                eventName = "musicNotificationFavourite";
                break;
            default:
                data.put("success", "操作失败");
                data.put("code", -1);
                break;
        }

        mServiceV2.mWXSDKInstance.fireGlobalEventCallback(eventName, data);
    }

    /**
     * 重写物理返回键，使不能回退
     */
    @Override
    public void onBackPressed() {}

    @Override
    protected void onDestroy() {
        unbindService(connection);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /**
     * 滑动销毁锁屏页面
     */
    @Override
    public void onSlidingFinish() {
        finish();
    }

    public void updateUI(final JSONObject options){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (options.getString("songName") != null) {
                    tvAudio.setText(options.getString("songName"));
                }
                if (options.getString("artistsName") != null) {
                    tvAudioName.setText(options.getString("artistsName"));
                }
                favour(mServiceV2.Favour);
                playOrPause(mServiceV2.Playing);

                if (bitmap != null) bitmap.recycle();
                bitmap = ImageUtils.GetLocalOrNetBitmap(options.getString("picUrl"));
                if (bitmap != null) lockDate.setImageBitmap(bitmap);
            }
        });
    }

    public void playOrPause(boolean playing){
        if (playing) {
            playView.setImageResource(R.mipmap.note_btn_pause_white);
        } else {
            playView.setImageResource(R.mipmap.note_btn_play_white);
        }
    }

    public void favour(boolean isFavour){
        if (isFavour) {
            favouriteView.setImageResource(R.mipmap.note_btn_loved);
        } else {
            favouriteView.setImageResource(R.mipmap.note_btn_love_white);
        }
    }

    private class TimeChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}