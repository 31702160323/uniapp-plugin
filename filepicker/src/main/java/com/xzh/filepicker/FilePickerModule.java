package com.xzh.filepicker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class FilePickerModule extends UniModule {
//    public static final String TAG = FilePickerModule.class.getSimpleName();
    public static final int PICK_FILE = 1;

    public static final String DOC = "application/msword";
    public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String XLS = "application/vnd.ms-excel application/x-excel";
    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PPT = "application/vnd.ms-powerpoint";
    public static final String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String PDF = "application/pdf";

    private UniJSCallback mCallback;

    @UniJSMethod()
    public void chooseFileAction(UniJSCallback callback) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多选
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{ DOC, DOCX, XLS, XLSX, PPT, PPTX, PDF });
        if (!(mUniSDKInstance.getContext() instanceof Activity)) {
            JSONObject callbackObject = new JSONObject();
            callbackObject.put("code", "1");
            callbackObject.put("msg", "mUniSDKInstance.getContext() 无法强转成 Activity");
            callback.invoke(callbackObject);
            return;
        }
        mCallback = callback;
        ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, PICK_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            final Uri uri = data.getData();
            final ClipData clipData = data.getClipData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject callbackObject = new JSONObject();
                    JSONArray data = new JSONArray();
                    if (uri != null) {
                        addFile(uri, data);
                    } else if (clipData != null) {
                        for (int i = 0, len = clipData.getItemCount(); i < len; i++) {
                            Uri itemUri = clipData.getItemAt(i).getUri();
                            addFile(itemUri, data);
                        }
                    }
                    callbackObject.put("data", data);
                    callbackObject.put("msg", "success");
                    callbackObject.put("code", "0");

                    if (mCallback != null) {
                        mCallback.invoke(callbackObject);
                    }
                }
            }).start();
        }
    }

    private void addFile(Uri uri, JSONArray data) {
        FileInfo fileInfo = getFileNameByUri(uri);
        String path = copyUriToExternalFilesDir(uri, fileInfo);
        JSONObject callbackData = new JSONObject();
        String[] suffix = fileInfo.fileName.split("\\.");
        callbackData.put("suffix", suffix[suffix.length - 1]);
        callbackData.put("size", fileInfo.size);
        callbackData.put("path", path);
        callbackData.put("name", fileInfo.fileName);
        data.add(callbackData);
    }

    private FileInfo getFileNameByUri(Uri uri) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = String.valueOf(System.currentTimeMillis());
        Cursor cursor = mUniSDKInstance.getContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            fileInfo.fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            fileInfo.size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
            cursor.close();
        }
        return fileInfo;
    }

    static class FileInfo {
        String fileName;
        long size;
    }

    private String copyUriToExternalFilesDir(Uri uri,FileInfo fileInfo) {
        try {
            InputStream inputStream = mUniSDKInstance.getContext().getContentResolver().openInputStream(uri);
            File tempDir = mUniSDKInstance.getContext().getExternalFilesDir("temp");
            if (inputStream != null && tempDir != null) {
                File file = new File(String.format("%s/%s", tempDir, fileInfo.fileName));
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                byte[] byteArray = new byte[1024];
                int bytes = bis.read(byteArray);
                while (bytes > 0) {
                    bos.write(byteArray, 0, bytes);
                    bos.flush();
                    bytes = bis.read(byteArray);
                }
                bos.close();
                fos.close();
//                ((Activity)mUniSDKInstance.getContext()).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                });
                return String.valueOf(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

//    @UniJSMethod(uiThread = false)
//    public void filterSortFileByName(UniJSCallback callback) {
//        File[] files = new File(Environment.getExternalStorageDirectory().getPath()).listFiles();
//        List<FileInfo> filesInfo = new ArrayList<>();
//        for (File file : files) {
//            filesInfo.add(new FileInfo()
//                    .setFileName(file.getName())
//                    .setFileSize(file.isDirectory() ? "" : getFileSize(file))
//                    .setFileType(String.valueOf(file.isDirectory()))
//                    .setFileDate(getFileDate(file))
//            );
//        }
//        callback.invoke(filesInfo);
//    }
//
//    @UniJSMethod(uiThread = false)
//    public void filterSortFileByName(String path, UniJSCallback callback) {
//        File[] files = new File(path).listFiles();
//        List<FileInfo> filesInfo = new ArrayList<>();
//        for (File file : files) {
//            filesInfo.add(new FileInfo()
//                    .setFileName(file.getName())
//                    .setFileSize(file.isDirectory() ? "" : getFileSize(file))
//                    .setFileType(String.valueOf(file.isDirectory()))
//                    .setFileDate(getFileDate(file))
//            );
//        }
//        callback.invoke(filesInfo);
//    }
//
//    class FileInfo {
//        private String fileName;
//        private String fileSize;
//        private String fileType;
//        private String fileDate;
//
//        public String getFileName() {
//            return fileName;
//        }
//
//        public String getFileSize() {
//            return fileSize;
//        }
//
//        public String getFileType() {
//            return fileType;
//        }
//
//        public String getFileDate() {
//            return fileDate;
//        }
//
//        public FileInfo setFileName(String fileName) {
//            this.fileName = fileName;
//            return this;
//        }
//
//        public FileInfo setFileSize(String fileSize) {
//            this.fileSize = fileSize;
//            return this;
//        }
//
//        public FileInfo setFileType(String fileType) {
//            this.fileType = fileType;
//            return this;
//        }
//
//        public FileInfo setFileDate(String fileDate) {
//            this.fileDate = fileDate;
//            return this;
//        }
//    }
//
//
//    //文件大小
//    private static final long KB = 2 << 9;
//    private static final long MB = 2 << 19;
//    private static final long GB = 2 << 29;
//    /**
//     * 获得指定文件的大小
//     *
//     * @param file
//     * @return
//     */
//    public String getFileSize(File file) {
//        if (file.isFile()) {
//            long fileLength = file.length();
//            if (fileLength < KB) {
//                return fileLength + "B";
//            } else if (fileLength < MB) {
//                return String.format(Locale.getDefault(), "%.2fKB", fileLength / (double) KB);
//            } else if (fileLength < GB) {
//                return String.format(Locale.getDefault(), "%.2fMB", fileLength / (double) MB);
//            } else {
//                return String.format(Locale.getDefault(), "%.2fGB", fileLength / (double) GB);
//            }
//        }
//        return null;
//    }
//
//    //日期格式化
//    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//    /**
//     * 获得文件最近修改的时间
//     *
//     * @param file
//     * @return
//     */
//    public String getFileDate(File file) {
//        return DATE_FORMAT.format(new Date(file.lastModified()));
//    }
}
