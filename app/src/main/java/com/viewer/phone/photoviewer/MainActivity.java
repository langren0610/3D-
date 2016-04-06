package com.viewer.phone.photoviewer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.viewer.phone.photoviewer.adapter.GalleryFlow;
import com.viewer.phone.photoviewer.adapter.ImageAdapter;
import com.viewer.phone.photoviewer.model.ImageViewItem;
import com.viewer.phone.photoviewer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements ViewSwitcher.ViewFactory, View.OnTouchListener {

    private int screenWidth;
    private int screenHeight;
    private final static int SCAN_OK = 1;
    List<String> photoPathList = new ArrayList<String>();
    private ImageAdapter adapter;
    private HashMap<Integer, ImageViewItem> imageViewItems = new HashMap<Integer, ImageViewItem>();
    private GalleryFlow galleryFlow;

    private PopupWindow longClickPopWindow;
    protected LayoutInflater mInflater;
    private View longClickPopWindowView;
    private ImageViewItem longClickImgView;

    private PopupWindow showPhotoPopWindow;
    private View showPhotoPageView;
    private ImageSwitcher mImageSwitcher;
    private float startX = 0;
    private int currentPosition;
    private Bitmap lastShowBitmap;
    /**
     * װ�ص�������
     */
    private LinearLayout linearLayout;
    /**
     * �������
     */
    private ImageView[] tips;

    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK:
                    adapter.setPhotoPathList(photoPathList);
                    adapter.notifyDataSetChanged();
                    break;
                case Utils.UPDATE_IMAGEVIEW:
                    if (msg.getData() != null) {
                        int position= msg.getData().getInt(Utils.UPDATE_IMAGEVIEW_KEY);
                        ImageViewItem imageViewItem = imageViewItems.get(position);
                        if (imageViewItem != null) {
                            Bitmap bitmap = imageViewItem.getBitmap();
                            ImageView iamgeview = imageViewItem.getImageView();
                            if (bitmap != null && iamgeview != null) {
                                iamgeview.setImageBitmap(bitmap);
                                iamgeview.setTag(imageViewItem);
                            }
                        }
                    }
                    break;
                case Utils.SHOW_PHOTO:
                    int position = -1;
                    if (msg.getData() != null) {
                        position = msg.getData().getInt(Utils.SHOW_PAGE_PHOTO_KEY);
                    }
                    if (lastShowBitmap != null && !lastShowBitmap.isRecycled() && position >= 0) {
                        BitmapDrawable bd = new BitmapDrawable(lastShowBitmap);
                        Bitmap bit = mImageSwitcher.getDrawingCache();
                        if (bit != null) {
                            bit.recycle();
                        }
                        mImageSwitcher.setImageDrawable(bd);
                        setImageBackground(position);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInflater = LayoutInflater.from(this);
        mContext = this;

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenWidth = metric.widthPixels;  // ��Ļ��ȣ����أ�
        screenHeight = metric.heightPixels;  // ��Ļ�߶ȣ����أ�

        adapter = new ImageAdapter(this, screenWidth, screenHeight, imageViewItems, mHandler);
        galleryFlow = (GalleryFlow) this.findViewById(R.id.Gallery01);
        galleryFlow.setFadingEdgeLength(0);
        galleryFlow.setSpacing(Utils.PHOTO_SPACINGS); //ͼƬ֮��ļ��
        galleryFlow.setAdapter(adapter);

        galleryFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
                if (!showPhotoPopWindow.isShowing()) {
                    showPhotoPopWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                    showPhotoPopWindow.update();
                }
                currentPosition = position;
                String path = photoPathList.get(currentPosition);
                CreateBitmapThread th = new CreateBitmapThread(path, currentPosition);
                th.start();
            }

        });
        galleryFlow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                releaseImageResources(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        galleryFlow.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                /*ImageViewItem imageViewItem = (ImageViewItem) view.getTag();
                if (imageViewItem != null) {
                    String path = imageViewItem.getPath();
                    //Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
                    if (!longClickPopWindow.isShowing()) {
                        longClickPopWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                        longClickPopWindow.update();
                    }
                }
                longClickImgView = imageViewItem;*/
                return false;
            }
        });
        galleryFlow.setSelection(0);

        longClickPopWindowView = mInflater.inflate(R.layout.pop_menu, null);
        longClickPopWindow = new PopupWindow(longClickPopWindowView, screenWidth / 2 , ViewGroup.LayoutParams.WRAP_CONTENT);
        longClickPopWindow.setBackgroundDrawable(new ColorDrawable(0));
        longClickPopWindow.setOutsideTouchable(true);
        longClickPopWindow.setFocusable(true);
        longClickPopWindowView.findViewById(R.id.delete_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (longClickImgView != null) {
                    String path = longClickImgView.getPath();
                    if (path != null && !path.isEmpty()) {
                        if (deletePhoto(path)) {
                            photoPathList.remove(path);
                            getPhoto();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                longClickImgView = null;
                if (longClickPopWindow.isShowing()) {
                    longClickPopWindow.dismiss();
                }
            }
        });
        longClickPopWindowView.findViewById(R.id.share_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (longClickImgView != null) {
                    String path = longClickImgView.getPath();
                    if (path != null && !path.isEmpty()) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        File shareFile = new File(path);
                        if (shareFile.exists() && shareFile.isFile()) {
                            shareIntent.setType("image/*");
                            shareIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(shareFile));
                        } else {
                            shareIntent.setType("text/plain");
                        }
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_photo)));
                    }
                }
                if (longClickPopWindow.isShowing()) {
                    longClickPopWindow.dismiss();
                }
            }
        });

        showPhotoPageView = mInflater.inflate(R.layout.image_pager, null);
        showPhotoPopWindow = new PopupWindow(showPhotoPageView, ViewGroup.LayoutParams.FILL_PARENT , ViewGroup.LayoutParams.FILL_PARENT);
        showPhotoPopWindow.setBackgroundDrawable(new ColorDrawable(0));
        showPhotoPopWindow.setOutsideTouchable(true);
        showPhotoPopWindow.setFocusable(true);
        mImageSwitcher = (ImageSwitcher) showPhotoPageView.findViewById(R.id.imageSwitcher1);

        //����Factory
        mImageSwitcher.setFactory(this);
        //����OnTouchListener������ͨ��Touch�¼����л�ͼƬ
        mImageSwitcher.setOnTouchListener(this);

        linearLayout = (LinearLayout) showPhotoPageView.findViewById(R.id.viewGroup);

        getPhoto();
    }

    private synchronized void releaseImageResources(int selectPosition) {
        int startIndex = galleryFlow.getFirstVisiblePosition();
        int endIndex = galleryFlow.getLastVisiblePosition();
        try {
            for (int i = 0; i < startIndex - 1; ++i) {
                ImageViewItem item = imageViewItems.get(i);
                if (item != null) {
                    if (item.getBitmap() != null) {
                        item.getBitmap().recycle();
                    }
                    ImageView view = item.getImageView();
                    view = null;
                    imageViewItems.remove(i);
                    System.gc();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ����ѡ�е�tip�ı���
     * @param selectItems
     */
    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    private boolean deletePhoto(String path) {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = MainActivity.this.getContentResolver();
        String sql = "";
        int count = mContentResolver.delete(mImageUri, MediaStore.Images.Media.DATA + " = ?", new String[] {path});
        if (count >= 1) {
            return true;
        }
        return false;
    }

    private void getPhoto() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "û��SD��", Toast.LENGTH_SHORT).show();
            return;
        }
        photoPathList.clear();
        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = MainActivity.this.getContentResolver();

                //ֻ��ѯjpeg��png��ͼƬ
                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);

                while (mCursor.moveToNext()) {
                    //��ȡͼƬ��·��
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    photoPathList.add(path);
                   /* if (photoPathList.size() >= 12) {
                        break;
                    }*/

                    //��ȡ��ͼƬ�ĸ�·����
                    String parentName = new File(path).getParentFile().getName();
                }

                mCursor.close();
                tips = new ImageView[photoPathList.size()];
                for(int i = 0; i< tips.length; i++){
                    ImageView mImageView = new ImageView(mContext);
                    tips[i] = mImageView;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    layoutParams.rightMargin = 3;
                    layoutParams.leftMargin = 3;

                    mImageView.setBackgroundResource(R.drawable.page_indicator_unfocused);
                    linearLayout.addView(mImageView, layoutParams);
                }

                //֪ͨHandlerɨ��ͼƬ���
                mHandler.sendEmptyMessage(SCAN_OK);

            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View makeView() {
        final ImageView i = new ImageView(this);
        i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return i;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    int position = -1;
                    float lastX = event.getX();
                    //̧���ʱ���X������ڰ��µ�ʱ�����ʾ��һ��ͼƬ
                    if(lastX > startX){
                        if(currentPosition > 0 && photoPathList != null && photoPathList.size() > 0){
                            //���ö���������Ķ����Ƚϼ򵥣������׵�ȥ���Ͽ����������
                            mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_in));
                            mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_out));
                            currentPosition--;
                            position = currentPosition % photoPathList.size();
                        }else{
                            Toast.makeText(getApplication(), R.string.first_page, Toast.LENGTH_SHORT).show();
                        }
                    }

                    if(lastX < startX && photoPathList != null && photoPathList.size() > 0){
                        if(currentPosition < photoPathList.size() - 1){
                            mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_in));
                            mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.lift_out));
                            currentPosition ++ ;
                            position = currentPosition;
                        }else{
                            Toast.makeText(getApplication(), R.string.last_page, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (position >= 0 && position < photoPathList.size()) {
                        String path = photoPathList.get(position);
                        if (path != null && !path.isEmpty()) {
                            CreateBitmapThread th = new CreateBitmapThread(path, position);
                            th.start();
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private class CreateBitmapThread extends Thread {

        private int position;
        private String  path;

        public CreateBitmapThread(String path, int position) {
            this.path = path;
            this.position = position;
        }

        @Override
        public synchronized void run() {
            lastShowBitmap = Utils.getBitmap(path, screenWidth, screenHeight);
            Message msg = new Message();
            msg.what = Utils.SHOW_PHOTO;
            Bundle bundle = new Bundle();
            bundle.putInt(Utils.SHOW_PAGE_PHOTO_KEY, position);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            super.run();
        }
    }
}
