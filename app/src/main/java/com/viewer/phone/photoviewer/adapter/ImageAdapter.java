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
    //private Point mPoint = new Point(300, 400);//������װImageView�Ŀ�͸ߵĶ���

    public ImageAdapter(Context c, int screenWidth, int screenHeight, HashMap<Integer, ImageViewItem> imageViewItems, Handler mHandler) {
        mContext = c;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        mInflater = LayoutInflater.from(c);
        this.imageViewItems = imageViewItems;
        this.mHandler = mHandler;
    }

    /**
     * ������ӰЧ��
     * @return
     */
    public ImageView createReflectedImages(String path) {
        if (path != null && !path.isEmpty()) {
            //����ԭͼ����֮���bitmap����
           // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;
            bfOptions.inPurgeable = true;
            bfOptions.inTempStorage = new byte[12 * 1024];
            bfOptions.inSampleSize = 2;
            bfOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bfOptions);
            bfOptions.inSampleSize = Utils.computeSampleSize(bfOptions, -1, 128*128);
            //����һ��Ҫ�������û�false����Ϊ֮ǰ���ǽ������ó���true
            bfOptions.inJustDecodeBounds = false;

            Bitmap originalImage = BitmapFactory.decodeFile(path, bfOptions);
            if (originalImage == null) {
                return null;
            }
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //�����������
            Matrix matrix = new Matrix();

            //ָ��һ���Ƕ���0,0Ϊ���������ת
            // matrix.setRotate(30);

            //ָ������(x�᲻�䣬y���෴)
            matrix.preScale(1, -1);

            //������Ӧ�õ���ԭͼ֮�У�����һ����Ȳ��䣬�߶�Ϊԭͼ1/2�ĵ�Ӱλͼ
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //����һ����Ȳ��䣬�߶�Ϊԭͼ+��Ӱͼ�߶ȵ�λͼ
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //�����洴����λͼ��ʼ��������
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * ����һ:Ϊ�������������xλ�ã�
             * ������:Ϊy��λ�ã�
             * ����������:�ֱ��Ӧ�����յ㣬
             * ������Ϊƽ�̷�ʽ��
             * ��������Ϊ����Gradient�ǻ���Shader�࣬��������ͨ��Paint��setShader�����������������
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //������Ӱ
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //���Ѿ�����õĻ��ʹ���һ��������Ӱ����Ч��
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + Utils.reflectionGap, paint);

            //����һ��ImageView������ʾ�Ѿ����õ�bitmapWithReflection
            ImageView imageView = new ImageView(mContext);
            imageView.setImageBitmap(bitmapWithReflection);
            //����imageView��С ��Ҳ����������ʾ��ͼƬ��С
            imageView.setLayoutParams(new GalleryFlow.LayoutParams(screenWidth / 2, screenHeight / 2));
            //imageView.setScaleType(ScaleType.MATRIX);
            originalImage.recycle();
            return imageView;
        }
        return null;
    }

    /**
     * ������ӰЧ��
     * @return
     */
    public Bitmap createReflectedBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            //����ԭͼ����֮���bitmap����
            // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            Bitmap originalImage = bitmap;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //�����������
            Matrix matrix = new Matrix();

            //ָ��һ���Ƕ���0,0Ϊ���������ת
            // matrix.setRotate(30);

            //ָ������(x�᲻�䣬y���෴)
            matrix.preScale(1, -1);

            //������Ӧ�õ���ԭͼ֮�У�����һ����Ȳ��䣬�߶�Ϊԭͼ1/2�ĵ�Ӱλͼ
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //����һ����Ȳ��䣬�߶�Ϊԭͼ+��Ӱͼ�߶ȵ�λͼ
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //�����洴����λͼ��ʼ��������
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * ����һ:Ϊ�������������xλ�ã�
             * ������:Ϊy��λ�ã�
             * ����������:�ֱ��Ӧ�����յ㣬
             * ������Ϊƽ�̷�ʽ��
             * ��������Ϊ����Gradient�ǻ���Shader�࣬��������ͨ��Paint��setShader�����������������
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //������Ӱ
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //���Ѿ�����õĻ��ʹ���һ��������Ӱ����Ч��
            canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + Utils.reflectionGap, paint);

            //����һ��ImageView������ʾ�Ѿ����õ�bitmapWithReflection
            //ImageView imageView = new ImageView(mContext);
            //imageView.setImageBitmap(bitmapWithReflection);
            //����imageView��С ��Ҳ����������ʾ��ͼƬ��С
           // imageView.setLayoutParams(new GalleryFlow.LayoutParams(screenWidth / 2, screenHeight / 2));
            //imageView.setScaleType(ScaleType.MATRIX);
            originalImage.recycle();
            return bitmapWithReflection;
        }
        return null;
    }

    /**
     * ������ӰЧ��
     * @return
     */
    public Bitmap createReflectedBitmap(String path) {
        if (path != null && !path.isEmpty()) {
            //����ԭͼ����֮���bitmap����
            // Bitmap originalImage = BitmapFactory.decodeResource(mContext.getResources(), imageId);
            BitmapFactory.Options bfOptions = new BitmapFactory.Options();
            bfOptions.inDither = false;
            bfOptions.inPurgeable = true;
            bfOptions.inTempStorage = new byte[12 * 1024];
            bfOptions.inSampleSize = 2;
            bfOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bfOptions);
            bfOptions.inSampleSize = Utils.computeSampleSize(bfOptions, -1, 128*128);
            //����һ��Ҫ�������û�false����Ϊ֮ǰ���ǽ������ó���true
            bfOptions.inJustDecodeBounds = false;

            Bitmap originalImage = BitmapFactory.decodeFile(path, bfOptions);
            if (originalImage == null) {
                return null;
            }
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            //�����������
            Matrix matrix = new Matrix();

            //ָ��һ���Ƕ���0,0Ϊ���������ת
            // matrix.setRotate(30);

            //ָ������(x�᲻�䣬y���෴)
            matrix.preScale(1, -1);

            //������Ӧ�õ���ԭͼ֮�У�����һ����Ȳ��䣬�߶�Ϊԭͼ1/2�ĵ�Ӱλͼ
            Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
                    height/2, width, height/2, matrix, false);

            //����һ����Ȳ��䣬�߶�Ϊԭͼ+��Ӱͼ�߶ȵ�λͼ
            Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                    (height + height / 2), Bitmap.Config.ARGB_8888);

            //�����洴����λͼ��ʼ��������
            Canvas canvas = new Canvas(bitmapWithReflection);
            canvas.drawBitmap(originalImage, 0, 0, null);

            Paint deafaultPaint = new Paint();
            deafaultPaint.setAntiAlias(false);
//    canvas.drawRect(0, height, width, height + reflectionGap,deafaultPaint);
            canvas.drawBitmap(reflectionImage, 0, height + Utils.reflectionGap, null);
            Paint paint = new Paint();
            paint.setAntiAlias(false);

            /**
             * ����һ:Ϊ�������������xλ�ã�
             * ������:Ϊy��λ�ã�
             * ����������:�ֱ��Ӧ�����յ㣬
             * ������Ϊƽ�̷�ʽ��
             * ��������Ϊ����Gradient�ǻ���Shader�࣬��������ͨ��Paint��setShader�����������������
             */
            LinearGradient shader = new LinearGradient(0,originalImage.getHeight(), 0,
                    bitmapWithReflection.getHeight() + Utils.reflectionGap,0x70ffffff, 0x00ffffff, Shader.TileMode.MIRROR);
            //������Ӱ
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            //���Ѿ�����õĻ��ʹ���һ��������Ӱ����Ч��
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
                //����imageView��С ��Ҳ����������ʾ��ͼƬ��С
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
