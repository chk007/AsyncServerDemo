package com.example.chk.asyncserver.datas;

import android.graphics.drawable.Drawable;

/**
 * Main Activity GridView Item data
 * @author chaman
 */
public final class ImageGridViewBean {
    /**
     * 图片资源
     */
    private Drawable drawable;
    /**
     * 图片本地的路径
     */
    private String imagePath;
    /**
     * ProgressBar的上传进度
     */
    private int progressStatus;

    public ImageGridViewBean(Drawable aDrawable, int aProgressStatus, String aImagePath) {
        drawable = aDrawable;
        progressStatus = aProgressStatus;
        imagePath = aImagePath;
    }
    public void setDrawable(Drawable aDrawable) {
        drawable = aDrawable;
    }
    public void setProgressStatus(int aProgressStatus) {
        progressStatus = aProgressStatus;
    }
    public Drawable getDrawable() {
        return drawable;
    }
    public int getProgressStatus() {
        return progressStatus;
    }
    public String getImagePath() {
        return this.imagePath;
    }
}
