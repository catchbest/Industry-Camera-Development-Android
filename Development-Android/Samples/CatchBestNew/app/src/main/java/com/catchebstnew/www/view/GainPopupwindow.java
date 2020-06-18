package com.catchebstnew.www.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.catchbest.R;


/**
 * Created by gmm on 2018/1/2.
 */

public class GainPopupwindow extends PopupWindow implements SeekBar.OnSeekBarChangeListener{

    private LayoutInflater inflater;
    private View gainView;


    private SeekBar sb_red,sb_green,sb_blue;
    private TextView tv_red_value,tv_green_value,tv_blue_value;

    private RedGainChangeListener redChangeListener;
    private GreenGainChangeListener greenChangeListener;
    private BlueGainChangeListener blueChangeListener;

    private int redMin = 0;
    private int greenMin = 0;
    private int blueMin = 0;
    public GainPopupwindow(Activity context){
        super(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gainView = inflater.inflate(R.layout.popupwindow_gain, null);
        this.setContentView(gainView);


        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setTouchable(true);
        this.setAnimationStyle(R.style.AnimTop);//设置动画
        this.setOutsideTouchable(true);

        initView();
    }

    public void showPupwindow(View parent){
        if(!this.isShowing()) {
            this.showAtLocation(parent, Gravity.TOP,0,0);
        } else {
            this.dismiss();
        }
    }

    private void initView(){
        sb_red = (SeekBar) gainView.findViewById(R.id.sb_red);
        sb_green = (SeekBar) gainView.findViewById(R.id.sb_green);
        sb_blue = (SeekBar) gainView.findViewById(R.id.sb_blue);
        tv_red_value = (TextView) gainView.findViewById(R.id.tv_red_value);
        tv_green_value = (TextView) gainView.findViewById(R.id.tv_green_value);
        tv_blue_value = (TextView) gainView.findViewById(R.id.tv_blue_value);

        sb_red.setOnSeekBarChangeListener(this);
        sb_green.setOnSeekBarChangeListener(this);
        sb_blue.setOnSeekBarChangeListener(this);

    }

    public void setData(int red,int green,int blue){
        sb_red.setProgress(red);
        sb_green.setProgress(green);
        sb_blue.setProgress(blue);

    }

    public void setRedRange(int min,int max){
        sb_red.setMax(max);
        redMin = min;
        tv_red_value.setText(sb_red.getProgress() + "");
    }

    public void setGreenRange(int min,int max){
        sb_green.setMax(max);
        greenMin = min;
        tv_green_value.setText(sb_green.getProgress() + "");
    }

    public void setBlueRange(int min,int max){
        sb_blue.setMax(max);
        blueMin = min;
        tv_blue_value.setText(sb_blue.getProgress() + "");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_red :
                tv_red_value.setText((progress+redMin) + "");
                redChangeListener.redChange((progress+redMin));
                break;

            case R.id.sb_green:
                tv_green_value.setText((progress+greenMin) + "");
                greenChangeListener.greenChange((progress+greenMin));
                break;

            case R.id.sb_blue:
                tv_blue_value.setText((progress+blueMin) + "");
                blueChangeListener.blueChange((progress+blueMin));
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface RedGainChangeListener{
        void redChange(int red);
    }

    public interface GreenGainChangeListener{
        void greenChange(int green);
    }

    public interface BlueGainChangeListener{
        void blueChange(int blue);
    }

    public void setRedChangeListener(RedGainChangeListener redChangeListener) {
        this.redChangeListener = redChangeListener;
    }

    public void setGreenChangeListener(GreenGainChangeListener greenChangeListener) {
        this.greenChangeListener = greenChangeListener;
    }

    public void setBlueChangeListener(BlueGainChangeListener blueChangeListener) {
        this.blueChangeListener = blueChangeListener;
    }
}
