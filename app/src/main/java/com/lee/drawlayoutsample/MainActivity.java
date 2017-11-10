package com.lee.drawlayoutsample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lee.drawlayoutsample.utils.LogUtils;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends FragmentActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "MainActivity";
    private int realImageWidth = 0;
    private int realImageHeight = 0;
    private ScaleGestureDetector mScaleGestureDetector;
    private ScaleGestureListener mScaleGestureListener = new ScaleGestureListener();
    private Set<View> mViewList = new HashSet<View>();
    private Set<View> mFocusViewList = new HashSet<View>();
    private Set<View> mAdjustViewList = new HashSet<View>();
    private long downTime = 0;
    private float downX = 0f;
    private float downY = 0f;
    private float downImageX = 0f;
    private float downImageY = 0f;


    private int imageW = 0;
    private int imageH = 0;

    private float locationX = 0f;
    private float locationY = 0f;
    private DisplayMetrics mDisplayMetrics;
    private int mStatusBarHeight = 0;
    private FrameLayout mRootView;
    private ImageView mCurrentImageView;
    private boolean canvasZoomEnabled = false;

    private Bitmap mTempBitmap;
    private Bitmap mFreespace;


    private FrameLayout mContent;
    private float mCurrentScale = 1f;
    private float mContentDownX = 0f;
    private float mContentDownY = 0f;
    private boolean mMoved = false;
    private ImageView mTextTools;
    private ImageView backgroundView;
    private ImageView foregroundView;
    private FrameLayout.LayoutParams mContentLayoutParams;
    private float mStartX;
    private float mStartY;
    private static final int STROKE_WIDTH = 5;
    private URLInfo urlInfo=new URLInfo();
    private LabelInfo labelInfo = new LabelInfo();
    private URL url;
    private Canvas canvas;
    private Paint paint;
    private Path path;
    private String jsonString;

    private int parseType;
    public static final int PARSE_URLINFO=0;
    public static final int PARSE_SQUARE=1;
    public static final int LOAD_IMAGE=2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        try {
            url = new URL("http://67.216.209.114/ask.php");
        }
        catch(Exception e) {
        }
        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mStatusBarHeight = getStatusBarHeight(this);
        mRootView = (FrameLayout) findViewById(R.id.root);
        mContent = (FrameLayout) findViewById(R.id.content);
        backgroundView = (ImageView) findViewById(R.id.imageView);
        foregroundView = (ImageView) findViewById(R.id.foregroundView);


        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(this);
        mContentLayoutParams = (FrameLayout.LayoutParams) mContent.getLayoutParams();
        mContentLayoutParams.width = mDisplayMetrics.widthPixels - 312;
        mContentLayoutParams.height = mDisplayMetrics.heightPixels - mStatusBarHeight - 107;



        mContent.setOnTouchListener(null);

        mScaleGestureDetector = new ScaleGestureDetector(this, mScaleGestureListener);


        ImageView lineImageView = (ImageView) findViewById(R.id.lineIcon);
        lineImageView.setOnTouchListener(null);
        lineImageView.setOnClickListener(null);
        ImageView rect = (ImageView) findViewById(R.id.rectIcon);
        //rect.setOnTouchListener(mTouchListener);
        rect.setOnClickListener(mSquareBarClickListener);


        Button edListener = findViewById(R.id.button);
        edListener.setOnClickListener(medOnClickListener);

        Button resetListener = findViewById(R.id.button4);
        resetListener.setOnClickListener(mResetListener);


        findViewById(R.id.clear).setOnClickListener(this);
        findViewById(R.id.rotate).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.copy).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.done).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.forward).setOnClickListener(this);
        findViewById(R.id.send_request).setOnClickListener(this);
        findViewById(R.id.paint).setOnClickListener(this);
        findViewById(R.id.erase).setOnClickListener(this);
        mTextTools = (ImageView) findViewById(R.id.text);
        mTextTools.setOnClickListener(this);
        mViewList.clear();
        parseType=PARSE_URLINFO;
        sendRequestWithHttpURLConnection();

    }

    private void layoutViews() {

        mViewList.clear();
        if (labelInfo.getSquare()!=null){
            for (Square square: labelInfo.getSquare()){
                float xmax=square.getXmax();
                float xmin=square.getXmin();
                float ymax=square.getYmax();
                float ymin=square.getYmin();
                int realXmin = (int)(xmin*imageW/realImageWidth);//real: the location on the screen
                int realYmin = (int)(ymin*imageH/realImageHeight);
                int realXmax = (int)(xmax*imageW/realImageWidth);
                int realYmax = (int)(ymax*imageH/realImageHeight);
                float realWidth=realXmax-realXmin;
                float realHeight=realYmax-realYmin;

                mContentLayoutParams = new FrameLayout.LayoutParams((int)realWidth, (int)realHeight);

                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setOnTouchListener(new SquareTouchListener(imageView));
                imageView.setTag(square);
                imageView.setX(realXmin);
                imageView.setY(realYmin);
                imageView.setBackgroundResource(R.drawable.border_shape);
                mViewList.add(imageView);
                mAdjustViewList.clear();
                mFocusViewList.clear();
                mContent.addView(imageView, mContentLayoutParams);

            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mRootView.removeAllViews();

        recycle(mFreespace,mTempBitmap);
    }

    private void recycle(Bitmap... bitmaps) {
        if (bitmaps != null && bitmaps.length > 0) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null) bitmap.recycle();
            }
        }
    }






    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.paint:
                foregroundView.setOnTouchListener(new FreespaceTouchListener(foregroundView));
                paint.setColor(Color.rgb(2,192,1));
                for (View view:mViewList){
                    view.setVisibility(View.GONE);
                }
                Toast.makeText(v.getContext(),"Paint enabled",Toast.LENGTH_SHORT).show();
                break;
            case R.id.erase:
                foregroundView.setOnTouchListener(new FreespaceTouchListener(foregroundView));
                paint.setColor(Color.BLACK);

                for (View view:mViewList){
                    view.setVisibility(View.GONE);
                }
                Toast.makeText(v.getContext(),"Erasor enabled",Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                for (View view : new ArrayList<View>(mFocusViewList)) {
                    removeViews(view);
                }
                for (View view : new ArrayList<View>(mAdjustViewList)) {
                    removeViews(view);
                }

                mContent.requestLayout();
                break;
            case R.id.send_request:
                //transform all the labeled data
                for (View squareView: mViewList){
                    Square square = (Square) squareView.getTag();

                    float realWidth=squareView.getWidth();
                    float realHeight=squareView.getHeight();
                    float width=realWidth/imageW*realImageWidth;
                    float height=realHeight/imageH*realImageHeight;
                    int xmin= (int) (squareView.getX()*realImageWidth/imageW);
                    int ymin= (int) (squareView.getY()*realImageHeight/imageH);
                    square.setXmax(xmin+(int)width);
                    square.setXmin(xmin);
                    square.setYmin(ymin);
                    square.setYmax(ymin+(int)height);
                    mContent.removeView(squareView);
                }
                //clear the current view list
                mViewList.clear();
                mAdjustViewList.clear();
                mFocusViewList.clear();

                //upload labeldata and freespace
                uploadJson(urlInfo.getSquareURL());
                uploadImage(urlInfo.getLayerURL());

                //fetch new url info
                parseType=PARSE_URLINFO;
                try {
                    url = new URL("http://67.216.209.114/ask.php");
                }
                catch(Exception e) {
                }
                sendRequestWithHttpURLConnection();
                break;
            default:
                break;
        }
    }

    private void removeViews(View view) {
        mContent.removeView(view);
        mViewList.remove(view);
        labelInfo.getSquare().remove(view.getTag());
        mFocusViewList.remove(view);
        mAdjustViewList.remove(view);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }


    //private void createMemento(View view, boolean isDelete, boolean isNewElement) {

//        if (mInfoList == null) {
//            mInfoList = new ArrayList<>();
//        }
//        mCurrentIndex++;
//
////        LogUtils.d("add mCurrentIndex: " + mCurrentIndex);
//
//        ViewInfo viewInfo = (ViewInfo) view.getTag();
////        LogUtils.d("viewInfo: " + viewInfo);
//        if (isNewElement) { //刚刚从侧边栏拖拽出来的元素
////            LogUtils.d("新增元素");
//            viewInfo.width = viewInfo.width == 0 ? view.getWidth() : viewInfo.width;
//            viewInfo.height = viewInfo.height == 0 ? view.getHeight() : viewInfo.height;
//            viewInfo.x = viewInfo.x == 0 ? (int) view.getX() : viewInfo.x;
//            viewInfo.y = viewInfo.y == 0 ? (int) view.getY() : viewInfo.y;
//            mInfoList.add(viewInfo);
//        } else { //改变现有元素
////            LogUtils.d("改变现有元素");
//            if (isDelete) {
////                LogUtils.i("删除元素");
//                for (int i = mInfoList.size() - 1; i >= 0; i--) {
//                    ViewInfo info = mInfoList.get(i);
//                    if (info.id == viewInfo.id && info.realId == viewInfo.realId) {
////                        LogUtils.i("删除重复元素: " + info);
//                        mInfoList.remove(info);
//                    }
//                }
//            } else {
//                ViewInfo newViewInfo = new ViewInfo(viewInfo.id, view.getRotation());
//                newViewInfo.realId = ++mRealInfoId;
//                newViewInfo.width = view.getWidth();
//                newViewInfo.height = view.getHeight();
//                newViewInfo.x = view.getX();
//                newViewInfo.y = view.getY();
//                newViewInfo.type = viewInfo.type;
//                newViewInfo.color = mCurrentColor;
//
//                for (int i = mInfoList.size() - 1; i >= 0; i--) {
//                    ViewInfo info = mInfoList.get(i);
//                    if (info.id == viewInfo.id && info.realId == viewInfo.realId) {
////                        LogUtils.i("删除重复元素: " + info);
//                        mInfoList.remove(info);
//                        view.setTag(newViewInfo);
//                    }
//                }
//                mInfoList.add(newViewInfo);
//            }
//        }
//
//        List<ViewInfo> infos = new ArrayList<>();
//
//        infos.addAll(mInfoList);
//        mOriginator.setInfos(infos);
//        mCreataker.createMemento(mOriginator.createMemento(), mCurrentIndex);

    //}


    private View.OnClickListener mSquareBarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ImageView imageView = new ImageView(MainActivity.this);
            mCurrentImageView = imageView;
            Square square = new Square();
            labelInfo.getSquare().add(square);
            imageView.setTag(square);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(R.drawable.border_shape_blue);
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            locationX = location[0];
            locationY = location[1];
            imageView.setX(locationX + 300);
            imageView.setY(locationY + 300);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(v.getWidth() * 4, v.getHeight() * 4);
            mContent.addView(imageView, params);
            mViewList.add(imageView);
            mAdjustViewList.clear();
            mFocusViewList.clear();
            mAdjustViewList.add(imageView);
            imageView.setOnTouchListener(new SquareTouchListener(imageView));
        }
    };


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

//        switch (checkedId) {
//            case R.id.rb_black:
//                mCurrentColor = 0;
//                mLineBitmap = mLineBitmapBlack;
//
//                break;
//            case R.id.rb_green:
//                mCurrentColor = 1;
//                mLineBitmap = mLineBitmapGreen;
//
//                break;
//            case R.id.rb_red:
//                mCurrentColor = 2;
//                mLineBitmap = mLineBitmapRed;
//
//                break;
//            default:
//                LogUtils.e("default checkedId: " + checkedId, new IllegalArgumentException());
//                break;
//        }
    }


    private class SquareTouchListener implements View.OnTouchListener {
        private ImageView imageView;

        SquareTouchListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int imageWidth=v.getWidth();
            int imageHeight=v.getHeight();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = imageView.getX();
                    mStartY = imageView.getY();
                    downX = event.getX();
                    downY = event.getY();

                    downTime = System.currentTimeMillis();
                    int[] location0 = new int[2];
                    imageView.getLocationOnScreen(location0);
                    int x0 = location0[0];
                    int y0 = location0[1];
                    Rect srcRect = new Rect();
                    srcRect.left = x0;
                    srcRect.right = x0 + imageView.getWidth();
                    srcRect.top = y0;
                    srcRect.bottom = y0 + imageView.getHeight();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float x1 = event.getX();
                    float y1 = event.getY();
                    float disX = x1 - downX;
                    float disY = y1 - downY;
                    LogUtils.i("disX : " + (int) disX + ", disY : " + (int) disY);

                    if ( mFocusViewList.contains(imageView)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                        params.width = imageWidth + (int) disX;
                        if (params.width < 50) {
                            params.width = 50;
                        }
                        params.height = imageHeight + (int) disY;
                        if (params.height < 50) {
                            params.height = 50;
                        }
                        imageView.requestLayout();
                        downX=x1;
                        downY=y1;
                    } else if (mAdjustViewList.contains(imageView)) {
                        imageView.setX(imageView.getX() + disX);
                        imageView.setY(imageView.getY() + disY);
                        imageView.requestLayout();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    float x = imageView.getX();
                    float y = imageView.getY();
                    long time = System.currentTimeMillis() - downTime;
                    if (time > 200) {
                        //it's long click
                    } else {
                        if (mAdjustViewList.contains(imageView)) {
                            mFocusViewList.clear();
                            mFocusViewList.add(imageView);
                            mAdjustViewList.clear();
                            imageView.setBackgroundResource(R.drawable.border_shape_green);
                            Toast.makeText(MainActivity.this, "resize mode", Toast.LENGTH_SHORT).show();
                        } else {
                            mAdjustViewList.clear();
                            mAdjustViewList.add(imageView);
                            mFocusViewList.clear();
                            imageView.setBackgroundResource(R.drawable.border_shape_blue);
                            Toast.makeText(MainActivity.this, "replace mode", Toast.LENGTH_SHORT).show();
                        }

                        if (Math.abs(x - downImageX) <= 5 && Math.abs(y - downImageY) <= 5) {
                            imageView.setBackgroundResource(R.drawable.border_shape_blue);
                        }
                    }
                    if (x < 0) {
                        x = 0;
                        imageView.setX(x);
                    } else if (x > (mDisplayMetrics.widthPixels - 312 - 100)) {
                        x = mDisplayMetrics.widthPixels - 100 - 312;
                        imageView.setX(x);
                    }

                    if (y <= 0) {
                        y = 0;
                        imageView.setY(y);
                    } else if (y > mDisplayMetrics.heightPixels - 100 - mStatusBarHeight - 107) {
                        y = mDisplayMetrics.heightPixels - 100 - mStatusBarHeight - 107;
                        imageView.setY(y);
                    }

                    if (Math.abs(mStartX - x) >= 3 || Math.abs(mStartY - y) >= 3) {
                        //createMemento(imageView, false, false);
                    }

                    return true;
            }
            return false;
        }
    }

    private class FreespaceTouchListener implements View.OnTouchListener{
        private ImageView imageView;

        FreespaceTouchListener(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.reset();
                    path.moveTo(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    path.lineTo(event.getX(), event.getY());
                    break;
            }
            canvas.drawPath(path, paint);
            foregroundView.setImageBitmap(mFreespace);
            return true;
        }

    }




    public Bitmap captureScreen() {
        // 允许当前窗口保存缓存信息
        for (int i = 0; i < mContent.getChildCount(); i++) {
            View view = mContent.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setCursorVisible(false);
            }
            view.clearFocus();
            view.setBackgroundResource(android.R.color.transparent);
        }
        mContent.setBackgroundResource(android.R.color.white);
        mContent.setDrawingCacheEnabled(true);
        mContent.measure(View.MeasureSpec.makeMeasureSpec((int) (mContent.getWidth() * 1), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((int) (mContent.getHeight() * 1), View.MeasureSpec.EXACTLY));
        mContent.layout(0, 0, mContent.getWidth(), mContent.getHeight());
        mContent.buildDrawingCache();

        // 去掉状态栏
        Bitmap cacheBmp = mContent.getDrawingCache();
        Bitmap resultBmp = null;
        if (null != cacheBmp) {
            int width = mContent.getWidth();
            int height = mContent.getHeight();
            resultBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(resultBmp);
            Rect srcRect = new Rect();
            srcRect.left = 0;
            srcRect.top = 0;
            srcRect.right = cacheBmp.getWidth();
            srcRect.bottom = cacheBmp.getHeight();

            Rect dstRect = new Rect();
            dstRect.left = 0;
            dstRect.top = 0;
            dstRect.right = width;
            dstRect.bottom = height;
            canvas.drawBitmap(cacheBmp, srcRect, dstRect, new Paint());
            cacheBmp.recycle();
        }

        // 销毁缓存信息
        mContent.setDrawingCacheEnabled(false);
        mContent.destroyDrawingCache();
        return resultBmp;
    }


    private boolean inRange(Rect srcRect, Rect checkRect) {
        return srcRect.contains(checkRect);
    }



    public class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float dSpan = detector.getCurrentSpan() - detector.getPreviousSpan();
            float dScale = (dSpan * 0.1f) / 100f;
            float scale = mCurrentScale + dScale;
            if (scale > 4.0f) {
                scale = 4.0f;
            } else if (scale < 0.5f) {
                scale = 0.5f;
            }
            mContent.setScaleX(scale);
            mContent.setScaleY(scale);
            mCurrentScale = scale;
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMoved = false;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    private View.OnClickListener medOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!canvasZoomEnabled) {
                mContent.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mScaleGestureDetector.onTouchEvent(event);
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                mContentDownX = event.getX();
                                mContentDownY = event.getY();
                                for (View view : mViewList) {
                                    view.setOnTouchListener(null);
                                    view.setBackgroundResource(R.drawable.border_shape);
                                }
                                mFocusViewList.clear();
                                mMoved = true;
                                return true;
                            case MotionEvent.ACTION_MOVE:
                                int w = mContent.getWidth();
                                int h = mContent.getHeight();
                                float maxW = (w * (mCurrentScale - 1f)) / 2;
                                float maxH = (h * (mCurrentScale - 1f)) / 2;
                                if (event.getPointerCount() <= 1 && mCurrentScale > 1f && mMoved) {
                                    Log.e(TAG, "move");
                                    mMoved = true;
                                    float disX = mContentDownX - event.getX();
                                    float disY = mContentDownY - event.getY();
                                    if (((int) disY + mContent.getScrollY()) >= -maxH
                                            && ((int) disY + mContent.getScrollY()) <= maxH
                                            && ((int) disX + mContent.getScrollX()) >= -maxW
                                            && ((int) disX + mContent.getScrollX()) <= maxW) {
                                        mContent.scrollBy((int) disX, (int) disY);
                                    }
                                    mContentDownX = event.getX();
                                    mContentDownY = event.getY();
                                    return true;
                                } else {
                                    Log.e(TAG, "not move");
                                    mMoved = false;
                                }
                                return false;
                            case MotionEvent.ACTION_UP:

                                mMoved = false;
                                return false;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                for (View view : mViewList) {
                    view.setOnTouchListener(null);
                }

                Toast.makeText(MainActivity.this, "listener enabled!", Toast.LENGTH_SHORT).show();
            } else {
                mContent.setOnTouchListener(null);
                for (View view : mViewList) {
                    view.setOnTouchListener(new SquareTouchListener((ImageView) view));
                }
                Toast.makeText(MainActivity.this, "listener disabled!", Toast.LENGTH_SHORT).show();
            }
            canvasZoomEnabled = (!canvasZoomEnabled);
        }
    };

    private View.OnClickListener mResetListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mContent.scrollTo(0, 0);
            mContent.setScaleX(1f);
            mContent.setScaleY(1f);
            mCurrentScale = 1f;
            foregroundView.setOnTouchListener(null);
            for (View view:mViewList){
                view.setVisibility(View.VISIBLE);
            }
        }
    };

    private void loadImageFromNetwork() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;

                try {
                    URL m_url = new URL(urlInfo.getLayerURL());
                    con = (HttpURLConnection) m_url.openConnection();
                    InputStream in = con.getInputStream();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    mTempBitmap = BitmapFactory.decodeStream(in, null, options);
                    realImageHeight = options.outHeight;
                    realImageWidth = options.outWidth;
                    Message message=new Message();
                    message.what=parseType;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(con!=null){
                        con.disconnect();
                    }
                }
            }
        }).start();

    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            String response;
            Gson gson=new Gson();
            switch (msg.what){
                case PARSE_URLINFO:
                    response=(String)msg.obj;
                    urlInfo= gson.fromJson(response, URLInfo.class);
                    Picasso.with(backgroundView.getContext())
                            .load(urlInfo.getBackgroundURL())
                            .error(R.drawable.example)
                            .into(backgroundView);
                    parseType = PARSE_SQUARE;
                    try {
                        url = new URL(urlInfo.getSquareURL());
                    }catch(Exception e){
                    }
                    sendRequestWithHttpURLConnection();
                    Toast.makeText(MainActivity.this,"Parse Success!"+urlInfo.getBackgroundURL(),Toast.LENGTH_SHORT).show();
                    break;
                case PARSE_SQUARE:
                    parseType=LOAD_IMAGE;
                    loadImageFromNetwork();
                    response=(String)msg.obj;
                    labelInfo= gson.fromJson(response, LabelInfo.class);
                    break;
                case LOAD_IMAGE:
                    imageH=foregroundView.getHeight();
                    imageW=foregroundView.getWidth();
                    mFreespace = Bitmap.createScaledBitmap(mTempBitmap, imageW, imageH, true);
                    mTempBitmap.recycle();
                    canvas=new Canvas(mFreespace);
                    path = new Path();
                    paint = new Paint();
                    paint.setColor(Color.rgb(2,192,1));
                    //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(50);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    foregroundView.setImageBitmap(mFreespace);
                    foregroundView.setAlpha(40);
                    layoutViews();
                    break;

            }

        }
    };
    private void sendRequestWithHttpURLConnection(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                try{
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    InputStream in=connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    BufferedReader buff=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line=null;
                    while((line=buff.readLine())!=null){
                        response.append(line);
                    }
                    Message message=new Message();
                    message.what=parseType;
                    message.obj=response.toString();
                    handler.sendMessage(message);
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private int uploadJson(final String selectedFilePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int serverResponseCode = 0;
                HttpURLConnection connection;
                DataOutputStream dataOutputStream;
                Gson gson=new Gson();
                jsonString=gson.toJson(labelInfo);
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String[] parts = selectedFilePath.split("/");
                final String fileName = parts[parts.length - 1];
                try {
                    URL url = new URL("http://67.216.209.114/UploadToServer.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", selectedFilePath);
                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName+ "\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(jsonString);
                    bufferSize = maxBufferSize;
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if (serverResponseCode == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(),
                                        "UTF-8"));

                        String retData = null;

                        String responseData = "";

                        while ((retData = in.readLine()) != null)

                        {

                            responseData += retData;

                        }

                        in.close();
                        System.out.println(responseData);
                        //Toast.makeText(MainActivity.this, responseData+jsonString, Toast.LENGTH_SHORT).show();

                    }

                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();


        return 0;
    }

    private int uploadImage(final String selectedFilePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int serverResponseCode = 0;
                HttpURLConnection connection;
                DataOutputStream dataOutputStream;
                mTempBitmap = Bitmap.createScaledBitmap(mFreespace, realImageWidth, realImageHeight, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mTempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                mTempBitmap.recycle();
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String[] parts = selectedFilePath.split("/");
                final String fileName = parts[parts.length - 1];
                try {
                    URL url = new URL("http://67.216.209.114/UploadToServer.php");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", selectedFilePath);
                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName+ "\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.write(baos.toByteArray());
                    bufferSize = maxBufferSize;
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if (serverResponseCode == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(connection.getInputStream(),
                                        "UTF-8"));

                        String retData = null;

                        String responseData = "";

                        while ((retData = in.readLine()) != null)

                        {

                            responseData += retData;

                        }

                        in.close();
                        System.out.println(responseData);
                        //Toast.makeText(MainActivity.this, responseData+jsonString, Toast.LENGTH_SHORT).show();

                    }

                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
        return 0;
    }

}
