package com.tk.tcompress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图片工具类
 *
 * @author KINCAI
 */
public class TCompress {
    static {
        // libjpeg
        System.loadLibrary("jpeg");
        System.loadLibrary("imgcompress");
    }

    /**
     * 本地方法 JNI处理图片
     *
     * @param bitmap   bitmap
     * @param width    宽度
     * @param height   高度
     * @param quality  图片质量 100表示不变 越小就压缩越严重
     * @param fileName 文件路径的byte数组
     * @param optimize 是否采用哈弗曼表数据计算
     * @return "0"失败, "1"成功
     */
    public static native String compressBitmap(Bitmap bitmap, int width,
                                               int height, int quality, byte[] fileName, boolean optimize);

    public static File compressImg(Context context, @NonNull File file) {
        File output = null;
        try {
            if (!file.exists()) {
                return null;
            }
            if (file.length() < 50 * 1024) {
                return file;
            }
            output = File.createTempFile(Long.toString(System.currentTimeMillis()), ".jpg", context.getCacheDir());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            // 旋转图片
            int photoDegree = readPictureDegree(file.getAbsolutePath());
            // 获取尺寸压缩倍数
            float ratio = getRatioSize(bitmap.getWidth(), bitmap.getHeight());
            Matrix matrix = new Matrix();
            matrix.setScale(1 / ratio, 1 / ratio);
            if (photoDegree != 0) {
                matrix.postRotate(photoDegree);
            }
            // 创建新的图片
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            // 最大图片大小 100KB
            int maxSize = 100;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            while (baos.toByteArray().length > maxSize * 1024) {
                // 重置baos即清空baos
                baos.reset();
                // 每次都减少10
                options -= 10;
                if (options < 20) {
                    options = 20;
                }
                // 这里压缩options%，把压缩后的数据存放到baos中
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }
            String result = TCompress.compressBitmap(bitmap,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    90,
                    output.getAbsolutePath().getBytes(),
                    true);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
            return result.equals("1") ? output : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取缩放比例
     *
     * @param bitmapWidth
     * @param bitmapHeight
     * @return
     */
    private static float getRatioSize(int bitmapWidth, int bitmapHeight) {
        //图片最大分辨率
        int imageHeight = 1280;
        int imageWidth = 720;
        // 缩放比
        float ratio = 1f;
        if (bitmapWidth >= bitmapHeight && bitmapWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitmapWidth / imageWidth;
        } else if (bitmapWidth < bitmapHeight && bitmapHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitmapHeight / imageHeight;
        }
        if (ratio <= 0)
            ratio = 1f;
        return ratio;
    }


    /**
     * 读取旋转角度
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


}
