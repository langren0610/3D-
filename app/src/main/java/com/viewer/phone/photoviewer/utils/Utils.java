package com.viewer.phone.photoviewer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by chaojie on 2015/6/30.
 */
public class Utils {
    public final static int reflectionGap = 4;////倒影图和原图之间的距离
    public final static int PHOTO_SPACINGS = -100;//图片的间距
    public final static int mMaxRotationAngle = 60;//最大转动角度
    public final static int mMaxZoom = -300;//最大缩放值
    public final static int UPDATE_IMAGEVIEW = 20;
    public final static String UPDATE_IMAGEVIEW_KEY = "UPDATE_IMAGEVIEW";
    public final static int SHOW_PAGE_PHOTO = 30;
    public final static String SHOW_PAGE_PHOTO_KEY = "SHOW_PAGE_PHOTO_KEY";
    public final static int SHOW_PHOTO = 31;

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap getBitmap(String path, int screenWidth, int screenHeight) {
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false;
        bfOptions.inPurgeable = true;
        bfOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //bfOptions.inTempStorage = new byte[5 * 1024 * 1024];
        bfOptions.inSampleSize = 2;
        bfOptions.inPurgeable = true;// 允许可清除
        bfOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfOptions);
        int minSideLength = Math.min(screenWidth, screenHeight);
        int inSampleSize = computeSampleSize(bfOptions, minSideLength, (screenWidth / 2) * (screenHeight / 2));
        if (inSampleSize <= 1) {
            inSampleSize = 2;
        }
        //inSampleSize = 10;
        //Log.i("aaaaaaaa", "inSampleSize:" + inSampleSize + ", screenWidth:" + screenWidth + ", screenHeight:" + screenHeight);
        bfOptions.inSampleSize = inSampleSize;
        //这里一定要将其设置回false，因为之前我们将其设置成了true
        bfOptions.inJustDecodeBounds = false;
        Bitmap originalImage = BitmapFactory.decodeFile(path, bfOptions);
        return originalImage;
    }
}
