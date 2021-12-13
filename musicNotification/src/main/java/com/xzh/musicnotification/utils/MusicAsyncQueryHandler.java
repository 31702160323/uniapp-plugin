package com.xzh.musicnotification.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicAsyncQueryHandler extends AsyncQueryHandler {
    private final OnCallbackListener mCallbackListener;

    public MusicAsyncQueryHandler(ContentResolver cr, OnCallbackListener callbackListener) {
        super(cr);
        this.mCallbackListener = callbackListener;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        List<SongBean> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                    SongBean songBean = new SongBean();
                    songBean.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));             //ID
                    songBean.musicName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));        //歌名
                    songBean.musicArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));     //歌手
                    songBean.musicAlbum = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑
                    songBean.musicAlbumID = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));     //专辑ID
                    songBean.musicAlbumURl = "";                                                                       //专辑图片路径
                    songBean.musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));         //路径
                    songBean.musicYear = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));  //发布年份
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        songBean.musicDuration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); //时长
                    }
                    songBean.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));         //文件大小

                    //获取本地音乐专辑图片
//                    Cursor cursorTwo = this.mCr.get().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
//                            MediaStore.Audio.Albums._ID + "=?", new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))},
//                            null);
//
//                    if (cursorTwo == null) throw new AssertionError();
//                    if (cursorTwo.moveToFirst()) {
//                        String path = cursorTwo.getString(cursorTwo.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
//                        if (path != null) songBean.musicAlbumURl = path;
//                    }

                    list.add(songBean);

//                    cursorTwo.close();
                }
            }
            // 释放资源
            cursor.close();
        }
        this.mCallbackListener.onCallbackListener(list);
    }

    public static class SongBean {
        public int id;                   //ID
        public String musicName;         //歌名
        public String musicArtist;     //歌手
        public String musicAlbum;        //专辑
        public int musicAlbumID;     //专辑ID
        public String musicAlbumURl = "";//专辑图片路径
        public String musicPath;         //路径
        public String musicYear;         //发布年份
        public String musicDuration;     //时长
        public Long size;                 //文件大小
    }

    public interface OnCallbackListener {
        void onCallbackListener(List<SongBean> list);
    }
}
