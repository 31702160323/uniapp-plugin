package com.xzh.musicnotification.service;


import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 接收Notification发送的广播
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_STATUS_BAR = ".NOTIFICATION_ACTIONS";
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
        try {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                 mListener.onScreenReceive();
            } else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
                mListener.onHeadsetReceive(intent.getIntExtra("state", 0));
            } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
                mListener.onBluetoothReceive(intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0));
            } else {
                mListener.onMusicReceive(intent.getStringExtra(EXTRA));
            }
        } catch (Exception e) {
            mListener.onMusicReceive("");
        }
    }

    public interface IReceiverListener {
        void onScreenReceive();
        void onHeadsetReceive(int extra);
        void onBluetoothReceive(int extra);
        void onMusicReceive(String extra);
    }
}
