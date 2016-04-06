package com.viewer.phone.photoviewer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.viewer.phone.photoviewer.R;
import com.viewer.phone.photoviewer.model.ImageViewItem;
import com.viewer.phone.photoviewer.utils.NativeImageLoader;
import com.viewer.phone.photoviewer.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chaojie on 2015/6/30.
 */
public class ImageAdapter extends BaseAdapter {

    private Handler mHandler;
    private HashMap<Integer, ImageViewItem> imageViewItems;
    int mGalleryItemBackground;
    private Context mContext;
    private int screenWidth;
    private int screenHeight;
    private List<String> photoPathList = new ArrayList<String>();
    protected LayoutInflater mInflater;
    //private Point mPoint = new Point(300, 400);//用来封装ImageView的宽和高的对象

    public ImageAdapter(Context c, int screenWidth, int screenHeight, HashMap<Integer, ImageViewItem> imageViewItems, Handler mHandler) {
        mContext = c;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        mInflater = LayoutInflater.from(c);
        this.imageViewItems = imageViewItems;
        this.mHandler = mHandler;
    }

    /**
     * 创建倒影效果
     * @return
     */
    public ImageView createReflectedImages(String path) {
        if (path != null && !path.isEmpty()) {
            //返回原图解码之后的bitmap对象
           // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;
            bfOptions.inPurgeable = true;
            bfOptions.inTempStorage = new byte[12 * 1024];
            bfOptions.inSampleSize = 2;
            bfOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bfOptions);
            bfOptions.inSampleSize = Utils.computeSampleSize(bfOptions, -1, 128*128);
            //这里一定要将其设置回false，因为之前我们将其设置成了true
            bfOptions.inJustDecodeBounds = false;

            Bitmap originalImage = BitmapFactory.decodeFile(path, bfOptions);
            if (originalImage == null) {
                return null;
            }
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //创建矩阵对象
            Matrix matrix = new Matrix();

            //指定一个角度以0,0为坐标进行旋转
            // matrix.setRotate(30);

            //指定矩阵(x轴不变，y轴相反)
            matrix.preScale(1, -1);

            //将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //创建一个宽度不变，高度为原图+倒影图高度的位图
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //将上面创建的位图初始化到画布
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * 参数一:为渐变起初点坐标x位置，
             * 参数二:为y轴位置，
             * 参数三和四:分辨对应渐变终点，
             * 最后参数为平铺方式，
             * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //设置阴影
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //用已经定义好的画笔构建一个矩形阴影渐变效果
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + Utils.reflectionGap, paint);

            //创建一个ImageView用来显示已经画好的bitmapWithReflection
            ImageView imageView = new ImageView(mContext);
            imageView.setImageBitmap(bitmapWithReflection);
            //设置imageView大小 ，也就是最终显示的图片大小
            imageView.setLayoutParams(new GalleryFlow.LayoutParams(screenWidth / 2, screenHeight / 2));
            //imageView.setScaleType(ScaleType.MATRIX);
            originalImage.recycle();
            return imageView;
        }
        return null;
    }

    /**
     * 创建倒影效果
     * @return
     */
    public Bitmap createReflectedBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            //返回原图解码之后的bitmap对象
            // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            Bitmap originalImage = bitmap;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //创建矩阵对象
            Matrix matrix = new Matrix();

            //指定一个角度以0,0为坐标进行旋转
            // matrix.setRotate(30);

            //指定矩阵(x轴不变，y轴相反)
            matrix.preScale(1, -1);

            //将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //创建一个宽度不变，高度为原图+倒影图高度的位图
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //将上面创建的位图初始化到画布
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * 参数一:为渐变起初点坐标x位置，
             * 参数二:为y轴位置，
             * 参数三和四:分辨对应渐变终点，
             * 最后参数为平铺方式，
             * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //设置阴影
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //用已经定义好的画笔构建一个矩形阴影渐变效果
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + Utils.reflectionGap, paint);

            //创建一个ImageView用来显示已经画好的bitmapWithReflection
            //ImageView imageView = new ImageView(mContext);
            //imageView.setImageBitmap(bitmapWithReflection);
            //设置imageView大小 ，也就是最终显示的图片大小
           // imageView.setLayoutParams(new GalleryFlow.LayoutParams(screenWidth / 2, screenHeight / 2));
            //imageView.setScaleType(ScaleType.MATRIX);
            originalImage.recycle();
            return bitmapWithReflection;
        }
        return null;
    }

    /**
     * 创建倒影效果
     * @return
     */
    public Bitmap createReflectedBitmap(String path) {
        if (path != null && !path.isEmpty()) {
            //返回原图解码之后的bitmap对象
            // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;
            bfOptions.inPurgeable = true;
            bfOptions.inTempStorage = new byte[12 * 1024];
            bfOptions.inSampleSize = 2;
            bfOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bfOptions);
            bfOptions.inSampleSize = Utils.computeSampleSize(bfOptions, -1, 128*128);
            //这里一定要将其设置回false，因为之前我们将其设置成了true
            bfOptions.inJustDecodeBounds = false;

            Bitmap originalImage = BitmapFactory.decodeFile(path, bfOptions);
            if (originalImage == null) {
                return null;
            }
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //创建矩阵对象
            Matrix matrix = new Matrix();

            //指定一个角度以0,0为坐标进行旋转
            // matrix.setRotate(30);

            //指定矩阵(x轴不变，y轴相反)
            matrix.preScale(1, -1);

            //将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //创建一个宽度不变，高度为原图+倒影图高度的位图
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //将上面创建的位图初始化到画布
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * 参数一:为渐变起初点坐标x位置，
             * 参数二:为y轴位置，
             * 参数三和四:分辨对应渐变终点，
             * 最后参数为平铺方式，
             * 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //设置阴影
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //用已经定义好的画笔构建一个矩形阴影渐变效果
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + Utils.reflectionGap, paint);
            originalImage.recycle();
            return bitmapWithReflection;
        }
        return null;
    }

    @Override
    public int getCount() {
        return photoPathList == null ? 0 : photoPathList.size();
    }

    @Override
    public Object getItem(int i) {
        return photoPathList == null ? null : photoPathList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        try {
            ImageViewItem imageViewItem = imageViewItems.get(i);
            if (imageViewItem == null) {
                String path = photoPathList.get(i);
                ImageView imageView = new ImageView(mContext);
                imageView.setImageResource(R.drawable.loading);
                //设置imageView大小 ，也就是最终显示的图片大小
                imageView.setLayoutParams(new GalleryFlow.LayoutParams(screenWidth / 2, screenHeight / 2));
                view = imageView;
                ImageViewUpdateThread th = new ImageViewUpdateThread(imageView, path, i);
                th.start();
            } else {
                view = imageViewItem.getImageView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (view == null) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.loading);
            view = imageView;
        }
        return view;
    }

    public float getScale(boolean focused, int offset) {
        return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
    }

    public void setPhotoPathList(List<String> photoPathList) {
        this.photoPathList = photoPathList;
    }

    private class ViewHolder {
        public MyImageView mImageView;
    }

    private class ImageViewUpdateThread extends Thread {

        private int position;
        private String path;
        private ImageView imageView;

        public ImageViewUpdateThread(ImageView imageView, String path, int position) {
            this.imageView = imageView;
            this.path = path;
            this.position = position;
        }

        @Override
        public synchronized void run() {
            Bitmap bitmap =  createReflectedBitmap(path);
            try {
                if (bitmap != null) {
                    ImageViewItem imageViewItem = new ImageViewItem();
                    imageViewItem.setImageView(imageView);
                    imageViewItem.setBitmap(bitmap);
                    imageViewItem.setPosition(position);
                    imageViewItem.setPath(path);
                    imageViewItems.put(position, imageViewItem);
                    Message msg = new Message();
                    msg.what = Utils.UPDATE_IMAGEVIEW;
                    Bundle bundle = new Bundle();
                    bundle.putInt(Utils.UPDATE_IMAGEVIEW_KEY, position);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.run();
        }
    }
}
