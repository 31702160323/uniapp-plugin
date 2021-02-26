package com.xzh.filepicker;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class FilePickerModule extends UniModule {

    @UniJSMethod(uiThread = false)
    public void filterSortFileByName(UniJSCallback callback) {
        File[] files = new File(Environment.getExternalStorageDirectory().getPath()).listFiles();
        List<FileInfo> filesInfo = new ArrayList<>();
        for (File file : files) {
            filesInfo.add(new FileInfo()
                    .setFileName(file.getName())
                    .setFileSize(file.isDirectory() ? "" : getFileSize(file))
                    .setFileType(String.valueOf(file.isDirectory()))
                    .setFileDate(getFileDate(file))
            );
        }
        callback.invoke(filesInfo);
    }

    @UniJSMethod(uiThread = false)
    public void filterSortFileByName(String path, UniJSCallback callback) {
        File[] files = new File(path).listFiles();
        List<FileInfo> filesInfo = new ArrayList<>();
        for (File file : files) {
            filesInfo.add(new FileInfo()
                    .setFileName(file.getName())
                    .setFileSize(file.isDirectory() ? "" : getFileSize(file))
                    .setFileType(String.valueOf(file.isDirectory()))
                    .setFileDate(getFileDate(file))
            );
        }
        callback.invoke(filesInfo);
    }

    class FileInfo {
        private String fileName;
        private String fileSize;
        private String fileType;
        private String fileDate;

        public String getFileName() {
            return fileName;
        }

        public String getFileSize() {
            return fileSize;
        }

        public String getFileType() {
            return fileType;
        }

        public String getFileDate() {
            return fileDate;
        }

        public FileInfo setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public FileInfo setFileSize(String fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public FileInfo setFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public FileInfo setFileDate(String fileDate) {
            this.fileDate = fileDate;
            return this;
        }
    }


    //文件大小
    private static final long KB = 2 << 9;
    private static final long MB = 2 << 19;
    private static final long GB = 2 << 29;
    /**
     * 获得指定文件的大小
     *
     * @param file
     * @return
     */
    public String getFileSize(File file) {
        if (file.isFile()) {
            long fileLength = file.length();
            if (fileLength < KB) {
                return fileLength + "B";
            } else if (fileLength < MB) {
                return String.format(Locale.getDefault(), "%.2fKB", fileLength / (double) KB);
            } else if (fileLength < GB) {
                return String.format(Locale.getDefault(), "%.2fMB", fileLength / (double) MB);
            } else {
                return String.format(Locale.getDefault(), "%.2fGB", fileLength / (double) GB);
            }
        }
        return null;
    }

    //日期格式化
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    /**
     * 获得文件最近修改的时间
     *
     * @param file
     * @return
     */
    public String getFileDate(File file) {
        return DATE_FORMAT.format(new Date(file.lastModified()));
    }
}
