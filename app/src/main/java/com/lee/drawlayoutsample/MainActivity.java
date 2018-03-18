package com.lee.drawlayoutsample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lee.drawlayoutsample.utils.LogUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
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
    private Set<View> mSquareList = new HashSet<View>();
    private Set<View> mFocusSquareList = new HashSet<View>();
    private Set<View> mAdjustSquareList = new HashSet<View>();
    private Set<BezierCurve> mCurveList = new HashSet<BezierCurve>();
    private Set<View> mFocusCurveList = new HashSet<View>();
    private Set<View> mAdjustCurveList = new HashSet<View>();
    private List<Curve> cList=new ArrayList<Curve>();
    private DrawPath dp;
    private static List<DrawPath> savePath= new ArrayList<DrawPath>();
    private long downTime = 0;
    private float downX = 0f;
    private float downY = 0f;
    private float downImageX = 0f;
    private float downImageY = 0f;
    private class DrawPath {
        public Path path;// 路径
        public Paint paint;// 画笔
    }


    private int imageW = 0;
    private int imageH = 0;

    private float locationX = 0f;
    private float locationY = 0f;
    private DisplayMetrics mDisplayMetrics;
    private int mStatusBarHeight = 0;
    private FrameLayout mRootView;
    private View mCurrentFocusView;
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
    private SquareInfo squareInfo = new SquareInfo();
    private CurveInfo curveInfo = new CurveInfo();

    private URL url;
    private Canvas canvas;
    private Paint paint;
    private Path path;
    private String jsonString;
    private TextView modeText;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private int parseType;
    public static final int PARSE_URLINFO=0;
    public static final int PARSE_SQUARE=1;
    public static final int LOAD_IMAGE=2;
    public static float startX=100;
    public static float startY=100;
    public static float controlX=100;
    public static float controlY=100;
    public static float endX=100;
    public static float endY=100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        try {
            url = new URL("http://139.196.85.93/ask.php");
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


        mContentLayoutParams = (FrameLayout.LayoutParams) mContent.getLayoutParams();
        mContentLayoutParams.width = mDisplayMetrics.widthPixels - 312;
        mContentLayoutParams.height = mDisplayMetrics.heightPixels - mStatusBarHeight - 107;



        mContent.setOnTouchListener(null);

        mScaleGestureDetector = new ScaleGestureDetector(this, mScaleGestureListener);


        ImageView rect1 = (ImageView) findViewById(R.id.rectIconCar);
        ImageView rect = (ImageView) findViewById(R.id.rectIconPerson);
        //rect.setOnTouchListener(mTouchListener);
        rect1.setOnClickListener(mSquareBarClickListener);
        rect.setOnClickListener(mSquareBarClickListener);


        Button edListener = findViewById(R.id.button);
        edListener.setOnClickListener(medOnClickListener);

        Button resetListener = findViewById(R.id.button4);
        resetListener.setOnClickListener(mResetListener);
        modeText=(TextView)findViewById(R.id.modeText);
        startX=mDisplayMetrics.widthPixels - 312-100;
        startY=mDisplayMetrics.heightPixels-mStatusBarHeight-107;
        endX=1000;
        endY=800;
        controlX=(startX+endX)/2;
        controlY=(startY+endY)/2;

        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.forward).setOnClickListener(this);
        findViewById(R.id.send_request).setOnClickListener(this);
        findViewById(R.id.paint).setOnClickListener(this);
        findViewById(R.id.erase).setOnClickListener(this);
        findViewById(R.id.lineIcon).setOnClickListener(this);
        findViewById(R.id.curve_only).setOnClickListener(this);
        findViewById(R.id.rider).setOnClickListener(mSquareBarClickListener);
        radioGroup=findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);
        mSquareList.clear();
        mCurveList.clear();
        parseType=PARSE_URLINFO;
        sendRequestWithHttpURLConnection();

    }

    private void layoutViews() {

        mSquareList.clear();
        mCurveList.clear();
        if (squareInfo.getSquare()!=null){
            for (Square square: squareInfo.getSquare()){
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
                if (square.getTypeofSquare().equals("person")){
                    imageView.setBackgroundResource(R.drawable.border_shape_red);
                }
                else if (square.getTypeofSquare().equals("car")){
                    imageView.setBackgroundResource(R.drawable.border_shape_yellow);
                }
                else{
                    imageView.setBackgroundResource(R.drawable.border_shape_purple);
                }
                mSquareList.add(imageView);
                mAdjustSquareList.clear();
                mFocusSquareList.clear();
                mContent.addView(imageView, mContentLayoutParams);

            }
        }

    }

    private void refreshSquareViews(){
        for (View v :mSquareList){
            if (mCurrentFocusView!=v){
                Square square = (Square) v.getTag();
                if (square.getTypeofSquare().equals("person")){
                    v.setBackgroundResource(R.drawable.border_shape_red);
                }
                else if (square.getTypeofSquare().equals("car")){
                    v.setBackgroundResource(R.drawable.border_shape_yellow);
                }
                else {
                    v.setBackgroundResource(R.drawable.border_shape_purple);
                }
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
            case R.id.curve_only:
                foregroundView.setVisibility(View.GONE);
                for (View view:mSquareList){
                    view.setVisibility(View.GONE);
                }
                break;
            case R.id.lineIcon:
                final BezierCurve bezierCurve = new BezierCurve(mContent.getContext());
                Curve curve = new Curve();
                bezierCurve.start.x=startX;
                bezierCurve.start.y=startY;
                bezierCurve.control.x=controlX;
                bezierCurve.control.y=controlY;
                bezierCurve.end.x=endX;
                bezierCurve.end.y=endY;
                bezierCurve.invalidate();
                if (curveInfo.getCurve()==null)
                    curveInfo.setCurve(cList);
                curveInfo.getCurve().add(curve);
                bezierCurve.setTag(curve);
                int[] location = new int[2];
                mContent.addView(bezierCurve);
                mCurveList.add(bezierCurve);
                mAdjustCurveList.clear();
                mFocusCurveList.clear();
                mAdjustCurveList.add(bezierCurve);
                mCurrentFocusView=bezierCurve;
                bezierCurve.setOnTouchListener(new BezierTouchListener(bezierCurve));
                foregroundView.setOnTouchListener(null);
                break;
            case R.id.paint:
                foregroundView.setOnTouchListener(new FreespaceTouchListener(foregroundView));
                foregroundView.setAlpha(100);
                paint.setStrokeWidth(50);
                radioButton=findViewById(R.id.thickness2);
                radioButton.setChecked(true);
                paint.setColor(Color.rgb(0,200,0));
                for (View view:mSquareList){
                    view.setVisibility(View.GONE);
                }
                for (View view:mCurveList){
                    view.setVisibility(View.GONE);
                }
                //Toast.makeText(v.getContext(),"Paint enabled",Toast.LENGTH_SHORT).show();
                modeText.setText("Paint");
                break;
            case R.id.erase:
                foregroundView.setAlpha(100);
                foregroundView.setOnTouchListener(new FreespaceTouchListener(foregroundView));
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(130);
                radioButton=findViewById(R.id.thickness3);
                radioButton.setChecked(true);
                for (View view:mSquareList){
                    view.setVisibility(View.GONE);
                }
                for (View view:mCurveList){
                    view.setVisibility(View.GONE);
                }
                //Toast.makeText(v.getContext(),"Erasor enabled",Toast.LENGTH_SHORT).show();
                modeText.setText("erasor");
                break;
            case R.id.delete:
                if (mFocusCurveList.contains(mCurrentFocusView)
                        || mAdjustCurveList.contains(mCurrentFocusView)){
                    removeCurveViews(mCurrentFocusView);
                    mCurrentFocusView=null;
                }
                else if (mFocusSquareList.contains(mCurrentFocusView)
                        || mAdjustSquareList.contains(mCurrentFocusView)){
                    removeViews(mCurrentFocusView);
                    mCurrentFocusView=null;
                }
                mContent.requestLayout();
                break;
            case R.id.send_request:
                boolean isOk = Dialog.showComfirmDialog(MainActivity.this,"Reminder","Are you sure?");
                if(!isOk) break;
                //transform all the labeled data
                for (View squareView: mSquareList){
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
                for (BezierCurve b: mCurveList){
                    Curve c=(Curve)b.getTag();

                    int startX= (int) (b.start.x*realImageWidth/imageW);
                    int startY= (int) (b.start.y*realImageHeight/imageH);
                    if (startX>=realImageWidth) startX=realImageWidth-1;
                    if (startY>=realImageHeight) startY=realImageHeight-1;
                    int endX= (int) (b.end.x*realImageWidth/imageW);
                    int endY= (int) (b.end.y*realImageHeight/imageH);
                    int controlX= (int) (b.control.x*realImageWidth/imageW);
                    int controlY= (int) (b.control.y*realImageHeight/imageH);
                    c.setStartX(startX);
                    c.setStartY(startY);
                    c.setEndX(endX);
                    c.setEndY(endY);
                    c.setControlX(controlX);
                    c.setControlY(controlY);
                    mContent.removeView(b);
                }
                //clear the current view list

                startX=mDisplayMetrics.widthPixels - 312-100;
                startY=mDisplayMetrics.heightPixels-mStatusBarHeight-107;
                endX=1000;
                endY=800;
                controlX=(startX+endX)/2;
                controlY=(startY+endY)/2;

                mSquareList.clear();
                mCurveList.clear();
                mAdjustSquareList.clear();
                mFocusSquareList.clear();
                mAdjustCurveList.clear();
                mFocusCurveList.clear();

                //upload labeldata and freespace
                uploadJson(urlInfo.getSquareURL());
                uploadImage(urlInfo.getLayerURL());


                //fetch new url info
                parseType=PARSE_URLINFO;
                try {
                    url = new URL("http://139.196.85.93/ask.php");
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
        mSquareList.remove(view);
        squareInfo.getSquare().remove(view.getTag());
        mFocusSquareList.remove(view);
        mAdjustSquareList.remove(view);

    }

    private void removeCurveViews(View view){
        mContent.removeView(view);
        mCurveList.remove(view);
        curveInfo.getCurve().remove(view.getTag());
        mFocusCurveList.remove(view);
        mAdjustCurveList.remove(view);
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
            Square square = new Square();
            squareInfo.getSquare().add(square);
            switch (v.getId()) {
                case R.id.rectIconCar:
                    square.setTypeofSquare("car");
                    break;
                case R.id.rectIconPerson:
                    square.setTypeofSquare("person");
                    break;
                case R.id.rider:
                    square.setTypeofSquare("rider");
                default:
                    break;
            }
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
            mCurrentFocusView= imageView;
            mSquareList.add(imageView);
            mAdjustSquareList.clear();
            mFocusSquareList.clear();
            mAdjustSquareList.add(imageView);
            refreshSquareViews();
            imageView.setOnTouchListener(new SquareTouchListener(imageView));
        }
    };


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

        switch (checkedId) {
            case R.id.thickness1:
                paint.setStrokeWidth(20);
                break;
            case R.id.thickness2:
                paint.setStrokeWidth(50);
                break;
            case R.id.thickness3:
                paint.setStrokeWidth(130);
                break;
        }
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

                    if ( mFocusSquareList.contains(imageView)) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                        params.width = imageWidth + (int) disX;
                        if (params.width < 30) {
                            params.width = 30;
                        }
                        params.height = imageHeight + (int) disY;
                        if (params.height < 30) {
                            params.height = 30;
                        }
                        imageView.requestLayout();
                        downX=x1;
                        downY=y1;
                    } else if (mAdjustSquareList.contains(imageView)) {
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
                        foregroundView.setOnTouchListener(null);
                        mCurrentFocusView= imageView;
                        if (mAdjustSquareList.contains(imageView)) {
                            mFocusSquareList.clear();
                            mFocusSquareList.add(imageView);
                            mAdjustSquareList.clear();
                            imageView.setBackgroundResource(R.drawable.border_shape_green);
                            //Toast.makeText(MainActivity.this, "resize mode", Toast.LENGTH_SHORT).show();
                            modeText.setText("resize");

                        } else {
                            mAdjustSquareList.clear();
                            mAdjustSquareList.add(imageView);
                            mFocusSquareList.clear();
                            imageView.setBackgroundResource(R.drawable.border_shape_blue);
                            //Toast.makeText(MainActivity.this, "replace mode", Toast.LENGTH_SHORT).show();
                            modeText.setText("replace");
                        }

                        refreshSquareViews();

                        if (Math.abs(x - downImageX) <= 5 && Math.abs(y - downImageY) <= 5) {
                            imageView.setBackgroundResource(R.drawable.border_shape_blue);
                        }
                    }
                    if (x < 0) {
                        x = 0;
                        imageView.setX(x);
                    } else if (x > (mDisplayMetrics.widthPixels - 312 - imageView.getWidth())) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                        params.width =(int)(mDisplayMetrics.widthPixels - 312 -x);
                        imageView.requestLayout();
                    }

                    if (y <= 0) {
                        y = 0;
                        imageView.setY(y);
                    } else if (y > mDisplayMetrics.heightPixels - imageView.getHeight() - mStatusBarHeight - 107) {
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                        params.height = (int) (mDisplayMetrics.heightPixels - mStatusBarHeight - 107 - y);
                        imageView.requestLayout();
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
                case MotionEvent.ACTION_UP:

            }
            canvas.drawPath(path, paint);
            foregroundView.setImageBitmap(mFreespace);
            return true;
        }

    }

    private class BezierTouchListener implements View.OnTouchListener {
        private BezierCurve b;

        BezierTouchListener(BezierCurve bezierCurve) {
            this.b=bezierCurve;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x=event.getX();
            float y=event.getY();
            int action=event.getAction();
//            if ((x>b.control.x-40 &&x<b.control.x+40
//                    && y>b.control.y-40 && y<b.control.y+40)
//                    ||(x>b.start.x-40 && x<b.start.x+40
//                    && y>b.start.y-40 && y<b.start.y+40)
//                    ||(x>b.end.x-40 && x<b.end.x+40
//                    && y>b.end.y-40 && y<b.end.y+40)) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    downTime = System.currentTimeMillis();
                    if (x > b.start.x - 120 && x < b.start.x + 120
                            && y > b.start.y - 120 && y < b.start.y + 60) {
                        b.setFocusPoint("start");
                        b.invalidate();
                    } else if (x > b.end.x - 60 && x < b.end.x + 60
                            && y > b.end.y - 60 && y < b.end.y + 60) {
                        b.setFocusPoint("end");
                        b.invalidate();
                    } else if (x > b.control.x - 60 && x < b.control.x + 60
                            && y > b.control.y - 60 && y < b.control.y + 60){
                        b.setFocusPoint("control");
                        b.invalidate();
                    }
                    else
                        return false;
                    return true;
                case MotionEvent.ACTION_UP:
                    long time = System.currentTimeMillis() - downTime;
                    if (time <= 200) {
                        foregroundView.setOnTouchListener(null);
                        mCurrentFocusView=b;
                        if (mAdjustCurveList.contains(b)) {
                            mFocusCurveList.clear();
                            mFocusCurveList.add(b);
                            mAdjustCurveList.clear();
                            b.status = "resize";
                            //Toast.makeText(MainActivity.this, "resize mode", Toast.LENGTH_SHORT).show();
                            modeText.setText("resize");
                        } else {
                            mAdjustCurveList.clear();
                            mAdjustCurveList.add(b);
                            mFocusCurveList.clear();
                            b.status = "replace";
                            //Toast.makeText(MainActivity.this, "replace mode", Toast.LENGTH_SHORT).show();
                            modeText.setText("replace");
                        }
                    }
                    if (x < 0) {
                        x = 0;
                        b.focusPoint.x=x;
                    } else if (x > (mDisplayMetrics.widthPixels - 312 )) {
                        x = mDisplayMetrics.widthPixels - 312;
                        b.focusPoint.x=x;
                    }



                    if (y <= 0) {
                        y = 0;
                        b.focusPoint.y=y;
                    } else if (y > mDisplayMetrics.heightPixels - mStatusBarHeight - 107) {
                        y = mDisplayMetrics.heightPixels - mStatusBarHeight - 107;
                        b.focusPoint.y=y;
                    }
                    b.invalidate();
                    startX=b.start.x;
                    startY=b.start.y;
                    controlX=b.control.x;
                    controlY=b.control.y;
                    endX=b.end.x;
                    endY=b.end.y;
                    return true;
                case MotionEvent.ACTION_MOVE:

                    float x1 = event.getX();
                    float y1 = event.getY();
                    float disX = x1 - downX;
                    float disY = y1 - downY;
                    if (mFocusCurveList.contains(b)) {
                        b.focusPoint.x = b.focusPoint.x + disX;
                        b.focusPoint.y = b.focusPoint.y + disY;
                        if (b.focusPoint!=b.control){
                            b.control.x=(b.start.x+b.end.x)/2;
                            b.control.y=(b.start.y+b.end.y)/2;
                        }
                        if (b.focusPoint==b.start){
                            float right=Math.abs(b.start.x-mDisplayMetrics.widthPixels+312);
                            float bottom= Math.abs(b.start.y-mDisplayMetrics.heightPixels+107+mStatusBarHeight);
                            float left = Math.abs(b.start.x);
                            if (right<=bottom && right<=left
                                    ){
                                b.start.x=mDisplayMetrics.widthPixels-312;
                            }
                            else if (bottom<=left && bottom <=right){
                                b.start.y=mDisplayMetrics.heightPixels-107-mStatusBarHeight;
                            }
                            else{
                                b.start.x=0;
                            }
                            if (b.start.x>mDisplayMetrics.widthPixels-312)
                                b.start.x=mDisplayMetrics.widthPixels-312;
                            if (b.start.x<0)
                                b.start.x=0;
                            if (b.start.y> mDisplayMetrics.heightPixels-107-mStatusBarHeight)
                                b.start.y=mDisplayMetrics.heightPixels-107-mStatusBarHeight;
                            b.control.x=(b.start.x+b.end.x)/2;
                            b.control.y=(b.start.y+b.end.y)/2;
                        }
                        downX = x1;
                        downY = y1;
                    } else if (mAdjustCurveList.contains(b)) {
                        b.start.x = b.start.x + disX;
                        b.start.y = b.start.y + disY;
                        b.end.x = b.end.x + disX;
                        b.end.y = b.end.y + disY;
                        b.control.x = b.control.x + disX;
                        b.control.y = b.control.y + disY;
                        if (b.focusPoint!=b.control){
                            b.control.x=(b.start.x+b.end.x)/2;
                            b.control.y=(b.start.y+b.end.y)/2;
                        }
                        //absorption
                        float right=Math.abs(b.start.x-mDisplayMetrics.widthPixels+312);
                        float bottom= Math.abs(b.start.y-mDisplayMetrics.heightPixels+107+mStatusBarHeight);
                        float left = Math.abs(b.start.x);
                        if (right<=bottom && right<=left
                                ){
                            b.start.x=mDisplayMetrics.widthPixels-312;
                        }
                        else if (bottom<=left && bottom <= right){
                            b.start.y=mDisplayMetrics.heightPixels-107-mStatusBarHeight;
                        }
                        else{
                            b.start.x=0;
                        }
                        if (b.start.x>mDisplayMetrics.widthPixels-312)
                            b.start.x=mDisplayMetrics.widthPixels-312;
                        if (b.start.x<0)
                            b.start.x=0;
                        if (b.start.y> mDisplayMetrics.heightPixels-107-mStatusBarHeight)
                            b.start.y=mDisplayMetrics.heightPixels-107-mStatusBarHeight;
//
//                        b.control.x=(b.start.x+b.end.x)/2;
//                        b.control.y=(b.start.y+b.end.y)/2;
                        downX=x1;
                        downY=y1;
                    }
                    b.invalidate();
                    return true;
            }
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
            if (scale > 10.0f) {
                scale = 10.0f;
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
                                for (View view : mSquareList) {
                                    view.setOnTouchListener(null);
                                }
                                mFocusSquareList.clear();
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

                for (View view : mSquareList) {
                    view.setOnTouchListener(null);
                }
                refreshSquareViews();
                foregroundView.setOnTouchListener(null);
                //Toast.makeText(MainActivity.this, "listener enabled!", Toast.LENGTH_SHORT).show();
                modeText.setText("listener enabled");


            } else {
                mContent.setOnTouchListener(null);
                for (View view : mSquareList) {
                    view.setOnTouchListener(new SquareTouchListener((ImageView) view));
                }
                //Toast.makeText(MainActivity.this, "listener disabled!", Toast.LENGTH_SHORT).show();
                modeText.setText("listener disabled");
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
            refreshSquareViews();
            foregroundView.setOnTouchListener(null);
            foregroundView.setAlpha(40);
            modeText.setVisibility(View.VISIBLE);


            foregroundView.setVisibility(View.VISIBLE);
            for (View view:mSquareList){
                view.setVisibility(View.VISIBLE);
            }
            for (View view:mCurveList){
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
                    con.setUseCaches(false);
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
                            .skipMemoryCache()
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
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
                    squareInfo= gson.fromJson(response, SquareInfo.class);
                    cList.clear();
                    curveInfo.setCurve(cList);
                    break;
                case LOAD_IMAGE:
                    imageH=foregroundView.getHeight();
                    imageW=foregroundView.getWidth();
                    if (mFreespace!=null) mFreespace.recycle();
                    mFreespace = Bitmap.createScaledBitmap(mTempBitmap, imageW, imageH, true);
                    canvas=new Canvas(mFreespace);
                    path = new Path();
                    paint = new Paint();
                    paint.setColor(Color.rgb(0,200,0));
                    //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(50);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    foregroundView.setImageBitmap(mFreespace);
                    foregroundView.setAlpha(40);
                    cList.clear();
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
                    connection.setUseCaches(false);

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
                jsonString=gson.toJson(squareInfo);
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String[] parts = selectedFilePath.split("/");
                final String fileName = parts[parts.length - 1];
                try {
                    URL url = new URL("http://139.196.85.93/UploadToServer.php");
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
                            + "car_ped-"+ fileName+"\"" + lineEnd);
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

                serverResponseCode = 0;
                gson=new Gson();
                jsonString="";
                jsonString=gson.toJson(curveInfo);
                try {
                    URL url = new URL("http://139.196.85.93/UploadToServer.php");
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
                            + "lane-"+ fileName+"\"" + lineEnd);
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
                mTempBitmap.recycle();
                mTempBitmap = Bitmap.createScaledBitmap(mFreespace, realImageWidth, realImageHeight, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mTempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String[] parts = selectedFilePath.split("/");
                String fileName = parts[parts.length - 1];
                parts = fileName.split("-");
                fileName=parts[0];
                try {
                    URL url = new URL("http://139.196.85.93/UploadToServer.php");
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
                            + fileName+   "\"" + lineEnd);
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
