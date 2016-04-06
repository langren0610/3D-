package com.viewer.phone.photoviewer.model;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by chaojie on 2015/7/1.
 */
public class ImageViewItem {

    private ImageView imageView;
    private Bitmap bitmap;
    private int position;
    private String path;

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
