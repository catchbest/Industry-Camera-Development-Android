package com.catchebstnew.www.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.catchbest.R;


/**
 * Created by gmm on 2018/1/4.
 */

public class SwitchButton extends View {
    private Paint mPaint;

    int centerX,centerY;
    boolean isOpen = true;
    private int resultWidth;
    private int resultHeight;

    public SwitchButton(Context context) {
        super(context);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPaint();
        // TypedArray是存放资源的array,1.通过上下文得到这个数组,attrs是构造函数传进来的,对应attrs.xml
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        isOpen = a.getBoolean(R.styleable.SwitchButton_current_status,true);
        this.setFocusable(true);
        this.setClickable(true);
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w/2;
        centerY = h/2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerX,centerY);
        canvas.drawColor(0x33000000);
        if(isOpen) {
            mPaint.setColor(0xff00ff00);
        } else {
            mPaint.setColor(0xffff0000);
        }

        RectF rectF = new RectF(-30,-30,30,30);
        canvas.drawArc(rectF,-45,270,false,mPaint);
        canvas.drawLine(0,-35,0,-5,mPaint);
    }

    private int measureWidth(int measureSpec) {
        resultWidth = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode == MeasureSpec.EXACTLY) {
            resultWidth = size;
        } else {
            resultWidth = size;
        }
//        Log.e("TAG", "width-------" + resultWidth);
        return resultWidth;
    }

    private int measureHeight(int measureSpec) {
        resultHeight = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode == MeasureSpec.EXACTLY) {
            resultHeight = size;
        } else {
            resultHeight = size;
        }
//        Log.e("TAG", "height-----" + resultHeight);
        return resultHeight;
    }

    public boolean getStatus(){
        return isOpen;
    }

    public void setStatus(boolean status){
        isOpen = status;
        invalidate();
    }
}
