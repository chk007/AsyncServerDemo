package com.example.chk.asyncserver.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @author chaman,my gmail is chkitm@gmail.com
 */
public final class DrawableDecoder {
    /**
     * 工具类，私有化构造器
     */
    private DrawableDecoder() { }
    /**
     * 图片压缩时，默认的高度
     */
    private static final int DEFAULT_IMAGE_HEIGHT = 120;
    /**
     * 图片压缩时，默认的宽度
     */
    private static final int DEFAULT_IMAGE_WIDTH = 120;

    /**
     * 提供图片的缩略图
     * @param aImagePath 图片在SD卡中的路径
     * @return 返回默认压缩高度、宽度的图片缩略图
     */
    public static Drawable decodeAndCompressImage(String aImagePath) {
        return decodeAndCompressImage(aImagePath, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    /**
     * 图片上传完成之后，在UI界面提供图片的缩略图
     * @param aImagePath 图片在SD卡中的路径
     * @param width 图片压缩后的宽度
     * @param height 图片压缩后的高度
     * @return 返回上传成功的图片缩略图的Bitmap
     */
    public static Drawable decodeAndCompressImage(String aImagePath, int width, int height) {
        if (aImagePath == null) {
            return null;
        }
        if (width < 0) {
           width = DEFAULT_IMAGE_WIDTH;
        }
        if (height <= 0) {
           height = DEFAULT_IMAGE_HEIGHT;
        }
        Bitmap bitmap;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(aImagePath, option); //bitmap此时为null
        option.inJustDecodeBounds = false;
        int radioWidth = option.outWidth / width;
        int radioHeight = option.outHeight / height;
        int radio = radioWidth > radioHeight ? radioWidth : radioHeight;
        if (radio < 1) {
            radio = 1;
        }
        option.inSampleSize = radio;
        bitmap = BitmapFactory.decodeFile(aImagePath, option);
        if (bitmap != null) {
            return new BitmapDrawable(bitmap);
        }
        return null;
    }
}
