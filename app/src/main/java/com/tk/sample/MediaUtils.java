package com.tk.sample;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;


/**
 * Created by TK on 2016/9/30.
 */
public final class MediaUtils {

    private static final String IMG_PREFIX = "IMG_";
    private static final String IMG_SUFFIX = ".jpeg";

    private static final String VIDEO_PREFIX = "VIDEO_";
    private static final String VIDEO_SUFFIX = ".mp4";

    /**
     * 创建临时文件
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static final File createCameraTmpFile(Context context) throws IOException {
        File dir = null;
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            dir = new File(Environment.getExternalStorageDirectory() + "/tempCamera/");
        }
        return File.createTempFile(IMG_PREFIX, IMG_SUFFIX, dir);
    }

    /**
     * 创建临时文件
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static final File createVideoTmpFile(Context context) throws IOException {
        File dir = null;
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } else {
            dir = new File(Environment.getExternalStorageDirectory() + "/tempCamera/");
        }
        return File.createTempFile(VIDEO_PREFIX, VIDEO_SUFFIX, dir);
    }

    /**
     * 读取图片大小
     *
     * @param file
     * @return
     */
    public static String getFileSize(File file) {
        return getFormatSize(file.length());
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
//            return size + "Byte";
            return "0B";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
