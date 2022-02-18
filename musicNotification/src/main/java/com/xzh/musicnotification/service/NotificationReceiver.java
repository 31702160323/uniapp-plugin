package com.xzh.musicnotification.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 接收Notification发送的广播
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_STATUS_BAR = NotificationReceiver.class.getPackage().getName() + ".NOTIFICATION_ACTIONS";
    public static final String EXTRA = "extra";
    public static final String EXTRA_PLAY = "play_pause";
    public static final String EXTRA_NEXT = "play_next";
    public static final String EXTRA_PRE = "play_previous";
    public static final String EXTRA_FAV = "play_favourite";
    private final IReceiverListener mListener;

    NotificationReceiver(IReceiverListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mListener.onReceive(intent.getAction(), intent.getStringExtra(EXTRA));
    }

    public interface IReceiverListener {
        void onReceive(String action, String extra);
    }
}
