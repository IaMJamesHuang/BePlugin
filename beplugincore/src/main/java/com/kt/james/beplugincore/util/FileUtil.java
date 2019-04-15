package com.kt.james.beplugincore.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.kt.james.beplugincore.BePluginGlobal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * author: James
 * 2019/4/11 15:43
 * version: 1.0
 */
public class FileUtil {

    public static boolean deleteDir(File file) {
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null && list.length > 0) {
                for (int i= 0 ;i < list.length; i++) {
                    deleteDir(list[i]);
                }
            }
        }
        LogUtil.d("delete", file.getAbsolutePath());
        return file.delete();
    }

    public static boolean checkPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return !path.contains("..") && !path.contains(" ");
    }

    public static boolean copyFile(String src, String dest) {
        try {
            return copyFile(new FileInputStream(src), dest);
        } catch (FileNotFoundException e) {
            LogUtil.printException("FileUtil.copyFile", e);
        }
        return false;
    }

    //todo 用okio重写
    public static boolean copyFile(final InputStream inputStream, String dest) {
        LogUtil.d("copyFile to " + dest);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //读写SD卡需要用户授权
            if (dest.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                int permission = BePluginGlobal.getHostApplication().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    LogUtil.e("6.0以上的系统，sd卡读写权限默认为未授权，需要在设置中开启" + dest);
                    return false;
                }
            }
        }

        FileOutputStream outputStream = null;
        try {
            File destFile = new File(dest);
            File parentFile = destFile.getParentFile();
            if (!parentFile.isDirectory() || !parentFile.exists()) {
                parentFile.mkdirs();
            }
            outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[48*1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            return true;
        } catch (Exception e) {
            LogUtil.printException("FileUtil.copyFile", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LogUtil.printException("FileUtil.copyFile", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtil.printException("FileUtil.copyFile", e);
                }
            }
        }
        return false;
    }

}
