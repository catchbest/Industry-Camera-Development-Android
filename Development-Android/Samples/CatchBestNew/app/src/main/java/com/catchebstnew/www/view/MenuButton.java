package com.catchebstnew.www.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by gmm on 2018/1/4.
 */

public class MenuButton extends View {
    private Paint mPaint;
    private int centerX,centerY;
    public MenuButton(Context context) {
        super(context);
    }

    public MenuButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.setFocusable(true);
        this.setClickable(true);

        initPaint();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(0xffffffff);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w/2;
        centerY = h/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerX,centerY);
        canvas.drawColor(0x33000000);
        canvas.drawLine(-30,-10,30,-10,mPaint);
        canvas.drawLine(-30,0,30,0,mPaint);
        canvas.drawLine(-30,10,30,10,mPaint);
    }
}
