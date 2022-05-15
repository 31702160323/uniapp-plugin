package com.xzh.musicnotification.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class MusicAsyncQueryHandler extends AsyncQueryHandler {
    private final OnCallbackListener mCallbackListener;

    public MusicAsyncQueryHandler(ContentResolver cr, OnCallbackListener callbackListener) {
        super(cr);
        this.mCallbackListener = callbackListener;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        JSONArray list = new JSONArray();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                    JSONObject songBean = new JSONObject();
                    songBean.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));             //ID
                    songBean.put("musicName", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));        //歌名
                    songBean.put("musicArtist", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));     //歌手
                    songBean.put("musicAlbum", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));//专辑
                    songBean.put("musicAlbumID", cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));     //专辑ID
                    songBean.put("musicAlbumURl", "");                                                                       //专辑图片路径
                    songBean.put("musicPath", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));         //路径
                    songBean.put("musicYear", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)));  //发布年份
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        songBean.put("musicDuration", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))); //时长
                    }
                    songBean.put("size", cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));         //文件大小
                    list.add(songBean);
                }
            }
            // 释放资源
            cursor.close();
        }
        this.mCallbackListener.onCallbackListener(list);
    }

    public interface OnCallbackListener {
        void onCallbackListener(JSONArray list);
    }
}
