package com.xzh.musicnotification.service

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 接收Notification发送的广播
 */
class NotificationReceiver internal constructor(private val mListener: IReceiverListener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action.equals(Intent.ACTION_SCREEN_OFF)) {
                mListener.onScreenReceive()
            } else if (intent.action.equals(Intent.ACTION_HEADSET_PLUG)) {
                mListener.onHeadsetReceive(intent.getIntExtra("state", 0))
            } else if (intent.action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                mListener.onBluetoothReceive(intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTING))
            } else {
                mListener.onMusicReceive(intent.getStringExtra(EXTRA))
            }
        } catch (e: Exception) {
            mListener.onMusicReceive("")
        }
    }

    interface IReceiverListener {
        fun onScreenReceive()
        fun onHeadsetReceive(extra: Int)
        fun onBluetoothReceive(extra: Int)
        fun onMusicReceive(extra: String?)
    }

    companion object {
        const val ACTION_STATUS_BAR = ".NOTIFICATION_ACTIONS"
        const val EXTRA = "extra"
        const val EXTRA_PLAY = "play_pause"
        const val EXTRA_NEXT = "play_next"
        const val EXTRA_PRE = "play_previous"
        const val EXTRA_FAV = "play_favourite"
        const val EXTRA_CLOSE = "close"
    }
}