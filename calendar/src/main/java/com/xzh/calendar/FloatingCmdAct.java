package com.xzh.calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FloatingCmdAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating_cmd);

        findViewById(R.id.setting_window_btn).setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                Toast.makeText(getApplicationContext(), "API < " + android.os.Build.VERSION_CODES.M, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.start_btn).setOnClickListener(v -> {
            startService(new Intent(getApplicationContext(), FloatingWindowService.class));
            Toast.makeText(getApplicationContext(), "启动服务", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.end_btn).setOnClickListener(v -> {
            stopService(new Intent(getApplicationContext(), FloatingWindowService.class));
            Toast.makeText(getApplicationContext(), "停止服务", Toast.LENGTH_SHORT).show();
        });

        if (!checkOverlayDisplayPermission()) {
            Toast.makeText(getApplicationContext(), "请允许应用显示悬浮窗", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private boolean checkOverlayDisplayPermission() {
        // API23以后需要检查权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        } else {
            return true;
        }
    }
}