package com.lee.drawlayoutsample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhuchen on 2017/11/15.
 */

public class BezierCurve extends View {

    public Paint mPaint,fPaint;

    public PointF start, end, control;

    public PointF focusPoint=control;

    public String status="replace";

    public BezierCurve(Context context) {
        super(context);
        mPaint = new Paint();
        fPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setTextSize(60);

        start = new PointF(2000,1000);
        end = new PointF(800,800);
        control = new PointF(1400,900);
        focusPoint=control;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        centerX = w/2;
//        centerY = h/2;
//
//        // 初始化数据点和控制点的位置
//        start.x = centerX-200;
//        start.y = centerY;
//        end.x = centerX+200;
//        end.y = centerY;
//        control.x = centerX;
//        control.y = centerY-100;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
        // 根据触摸位置更新控制点，并提示重绘

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制数据点和控制点
        mPaint.setColor(Color.GRAY);
        fPaint.setColor(Color.YELLOW);
        mPaint.setStrokeWidth(20);
        fPaint.setStrokeWidth(20);
        canvas.drawPoint(start.x,start.y,mPaint);
        canvas.drawPoint(end.x,end.y,mPaint);
        canvas.drawPoint(control.x,control.y,mPaint);
        canvas.drawPoint(focusPoint.x,focusPoint.y,fPaint);



        // 绘制辅助线
        mPaint.setStrokeWidth(4);
        canvas.drawLine(start.x,start.y,control.x,control.y,mPaint);
        canvas.drawLine(end.x,end.y,control.x,control.y,mPaint);

        // 绘制贝塞尔曲线
        if (status=="resize"){
            mPaint.setColor(Color.RED);
        }
        else{
            mPaint.setColor(Color.BLUE);
        }
        mPaint.setStrokeWidth(8);

        Path path = new Path();

        path.moveTo(start.x,start.y);
        path.quadTo(control.x,control.y,end.x,end.y);

        canvas.drawPath(path, mPaint);
    }

    public void setFocusPoint(String pName){
        switch (pName){
            case "start":
                focusPoint=start;
                break;
            case "end":
                focusPoint=end;
                break;
            case "control":
                focusPoint=control;
                break;

        }
        invalidate();
    }
}
