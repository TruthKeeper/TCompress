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

    /**
     * 压缩图片
     *
     * @param context
     * @param file
     * @return
     */
    public static File compressImg(@NonNull Context context, @NonNull File file) {
        return compressImg(context, file, 720, 720);
    }

    /**
     * 压缩图片
     *
     * @param context
     * @param file
     * @param imageWidth  图片最大宽
     * @param imageHeight 图片最大高
     * @return
     */
    public static File compressImg(@NonNull Context context, @NonNull File file, int imageWidth, int imageHeight) {
        File output = null;
        try {
            if (!file.exists()) {
                return null;
            }
            if (file.length() < 50 * 1024) {
                //50KB以下过滤
                return file;
            }
            output = File.createTempFile(Long.toString(System.currentTimeMillis()), ".jpg", context.getCacheDir());
            //获取原图信息
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //Bitmap此时不占用内存
            Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            //获取图片旋转角度
            int photoDegree = readPictureDegree(file.getAbsolutePath());
            options.inJustDecodeBounds = false;
            //采样率缩放
            options.inSampleSize = calculSampleSize(options, imageWidth, imageHeight);
            //获取真实Bitmap
            Bitmap result = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            if (0 != photoDegree) {
                Matrix matrix = new Matrix();
                matrix.postRotate(photoDegree);
                //旋转图片
                result = Bitmap.createBitmap(result, 0, 0, result.getWidth(),
                        result.getHeight(), matrix, true);
            }

            //最大图片大小 100KB
            int maxSize = 100;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 100;
            result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            while (baos.toByteArray().length > maxSize * 1024) {
                // 重置baos即清空baos
                baos.reset();
                // 每次都减少10
                quality -= 10;
                if (quality < 20) {
                    quality = 20;
                }
                // 这里压缩quality%，把压缩后的数据存放到baos中
                result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            //JNI压缩
            String resultStr = compressBitmap(result,
                    result.getWidth(),
                    result.getHeight(),
                    90,
                    output.getAbsolutePath().getBytes(),
                    true);
            if (!result.isRecycled()) {
                result.recycle();
                result = null;
            }
            return "1".equals(resultStr) ? output : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算新的采样率
     *
     * @param options
     * @return
     */
    private static int calculSampleSize(BitmapFactory.Options options, int imageWidth, int imageHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        while (((width / inSampleSize) > imageWidth)
                || ((height / inSampleSize) > imageHeight)) {
            //建议为2的幂
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    /**
     * 读取旋转角度
     *
     * @param path
     * @return
     */
    private static int readPictureDegree(String path) {
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
