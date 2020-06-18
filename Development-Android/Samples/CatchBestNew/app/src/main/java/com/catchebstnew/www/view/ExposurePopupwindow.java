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

public class ExposurePopupwindow extends PopupWindow implements SeekBar.OnSeekBarChangeListener{

    private LayoutInflater inflater;
    private View view;
    private SeekBar sb_exposure,sb_exposure2;
    private TextView tv_exposure,tv_exposure2;
    private ExposureChangeListener exposureChangeListener;
    private int max;

    private int min = 0;

    public ExposurePopupwindow(Activity context){
        super(context);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popupwindow_exposure,null);
        this.setContentView(view);

        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setTouchable(true);
        this.setAnimationStyle(R.style.AnimTop);//设置动画
        this.setOutsideTouchable(true);

        initView();
    }

    public void showPopupwindow(View parent){
        if(!this.isShowing()) {
             this.showAtLocation(parent, Gravity.TOP,0,0);
        } else {
            this.dismiss();
        }
    }

    private void initView(){
        sb_exposure = (SeekBar) view.findViewById(R.id.sb_exposure);
        sb_exposure2 = (SeekBar) view.findViewById(R.id.sb_exposure2);
        tv_exposure = (TextView) view.findViewById(R.id.tv_exposure);
        tv_exposure2 = (TextView) view.findViewById(R.id.tv_exposure2);

        sb_exposure.setOnSeekBarChangeListener(this);
        sb_exposure2.setOnSeekBarChangeListener(this);
    }

    public void setData(int progress){
        sb_exposure.setProgress(progress);
        sb_exposure2.setProgress(progress);
    }

    public void setRange(int min,int max){
        this.max = max;
        sb_exposure.setMax(max);
        this.min = min;
        tv_exposure.setText(sb_exposure.getProgress() + "/" + max);

        sb_exposure2.setMax(500);
        tv_exposure2.setText(sb_exposure2.getProgress() + "/500");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_exposure :
                tv_exposure.setText((progress+min) + "/" + max);
                exposureChangeListener.exposureChange((progress+min));
                sb_exposure2.setProgress(progress);
                break;

            case R.id.sb_exposure2:
                tv_exposure2.setText((progress+min) + "/500");
                exposureChangeListener.exposureChange((progress+min));
                sb_exposure.setProgress(progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface ExposureChangeListener{
        void exposureChange(int progress);
    }

    public void setExposureChangeListener(ExposureChangeListener exposureChangeListener) {
        this.exposureChangeListener = exposureChangeListener;
    }
}
