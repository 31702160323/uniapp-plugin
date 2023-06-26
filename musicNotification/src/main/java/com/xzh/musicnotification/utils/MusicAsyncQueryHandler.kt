package com.xzh.musicnotification.utils

import android.annotation.SuppressLint
import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject


class MusicAsyncQueryHandler(cr: ContentResolver?) : AsyncQueryHandler(cr) {
    private var mCallbackListener: OnCallbackListener? = null

    fun setOnCallbackListener(callbackListener: OnCallbackListener?): MusicAsyncQueryHandler {
        mCallbackListener = callbackListener
        return this
    }

    fun startQuery() {
        super.startQuery(
            0,
            null,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Audio.AudioColumns.IS_MUSIC
        )
    }

    @SuppressLint("Range")
    override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor) {
        super.onQueryComplete(token, cookie, cursor)
        val list = JSONArray()
        while (cursor.moveToNext()) {
            if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                val songBean = JSONObject()
                songBean["id"] =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)) //ID
                songBean["musicName"] =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) //歌名
                songBean["musicArtist"] =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) //歌手
                songBean["musicAlbum"] =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)) //专辑
                songBean["musicAlbumID"] =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)) //专辑ID
                songBean["musicAlbumURl"] = "" //专辑图片路径
                songBean["musicPath"] =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) //路径
                songBean["musicYear"] =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)) //发布年份
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    songBean["musicDuration"] =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) //时长
                }
                songBean["size"] =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) //文件大小
                list.add(songBean)
            }
        }
        // 释放资源
        cursor.close()
        this.mCallbackListener?.onCallbackListener(list)
    }

    interface OnCallbackListener {
        fun onCallbackListener(list: JSONArray?)
    }
}