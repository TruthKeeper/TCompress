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
            //获取原图信息
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            //Bitmap此时不占用内存
            Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            //获取图片旋转角度
            int photoDegree = readPictureDegree(file.getAbsolutePath());
            //图片最大分辨率
            int imageHeight = 1280;
            int imageWidth = 720;
            // 缩放比
            float ratio = 1f;
            if (options.outWidth >= options.outHeight && options.outWidth > imageWidth) {
                // 如果图片宽度比高度大,以宽度为基准
                ratio = options.outWidth / imageWidth;
                options.outWidth = imageWidth;
                options.outHeight = (int) (options.outHeight / ratio);
            } else if (options.outWidth < options.outHeight && options.outHeight > imageHeight) {
                // 如果图片高度比宽度大，以高度为基准
                ratio = options.outHeight / imageHeight;
                options.outHeight = imageHeight;
                options.outWidth = (int) (options.outWidth / ratio);
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = (int) ratio;
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
            String resultStr = TCompress.compressBitmap(result,
                    result.getWidth(),
                    result.getHeight(),
                    90,
                    output.getAbsolutePath().getBytes(),
                    true);
            if (!result.isRecycled()) {
                result.recycle();
                result = null;
            }
            return resultStr.equals("1") ? output : null;
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
