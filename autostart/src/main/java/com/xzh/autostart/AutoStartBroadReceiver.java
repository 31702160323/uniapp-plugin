package com.xzh.autostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartBroadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent thisIntent = new Intent("android.intent.action.MAIN");
            thisIntent.setClassName(context.getPackageName(), "io.dcloud.PandoraEntry");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(thisIntent);
        }
    }
}
