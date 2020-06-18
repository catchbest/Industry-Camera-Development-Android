package com.catchebstnew.www.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.catchbest.KSJ_BAYERMODE;
import com.catchbest.R;
import com.catchebstnew.www.adapter.ArrayWheelAdapter;
import com.catchebstnew.www.callback.OnWheelChangedListener;

/**
 * Created by gmm on 2018/1/10.
 */

public class BayerModePopupwindow extends PopupWindow implements View.OnClickListener,OnWheelChangedListener {
    private LayoutInflater inflater;
    private View view;
    private Button closePop,showValue;
    private WheelView wv_bayer;
    private TextView selectValue;
    private Context mContext;
    private int pickValue;
    private BayerModeListener bayerModeListener;
    private int[] mData_value = {KSJ_BAYERMODE.KSJ_BGGR_BGR24.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR24.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR24.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR24.ordinal(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR24_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR24_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR24_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR24_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR32.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR32.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR32.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR32.ordinal(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR32_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR32_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR32_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_BGGR_GRAY8.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_GRAY8.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_GRAY8.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_GRAY8.ordinal(),
            KSJ_BAYERMODE.KSJ_BGGR_GRAY8_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GRBG_GRAY8_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_RGGB_GRAY8_FLIP.ordinal(),
            KSJ_BAYERMODE.KSJ_GBRG_GRAY8_FLIP.ordinal()
            };

    private String[] mData_name = {KSJ_BAYERMODE.KSJ_BGGR_BGR24.name(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR24.name(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR24.name(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR24.name(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR24_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR24_FLIP.name(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR24_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR24_FLIP.name(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR32.name(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR32.name(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR32.name(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR32.name(),
            KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GRBG_BGR32_FLIP.name(),
            KSJ_BAYERMODE.KSJ_RGGB_BGR32_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GBRG_BGR32_FLIP.name(),
            KSJ_BAYERMODE.KSJ_BGGR_GRAY8.name(),
            KSJ_BAYERMODE.KSJ_GRBG_GRAY8.name(),
            KSJ_BAYERMODE.KSJ_RGGB_GRAY8.name(),
            KSJ_BAYERMODE.KSJ_GBRG_GRAY8.name(),
            KSJ_BAYERMODE.KSJ_BGGR_GRAY8_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GRBG_GRAY8_FLIP.name(),
            KSJ_BAYERMODE.KSJ_RGGB_GRAY8_FLIP.name(),
            KSJ_BAYERMODE.KSJ_GBRG_GRAY8_FLIP.name(),};
    public BayerModePopupwindow(Activity context){
        this.mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.popupwindow_white_balance,null);

        this.setContentView(view);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setTouchable(true);
        this.setAnimationStyle(R.style.AnimBottom);//设置动画
        this.setOutsideTouchable(true);

        initView();
        initListener();
        initData();
    }

    public void showPopupWindow(View parent){
        if(!this.isShowing()) {
            this.showAtLocation(parent, Gravity.BOTTOM,0,0);
        } else {
            this.dismiss();
        }
    }

    private void initView(){
        closePop = (Button) view.findViewById(R.id.closePop);
        showValue = (Button) view.findViewById(R.id.showValue);
        wv_bayer = (WheelView) view.findViewById(R.id.wv_wb);

        selectValue = (TextView) view.findViewById(R.id.selectValue);

        selectValue.setText("BayerMode");
    }

    private void initListener(){
        closePop.setOnClickListener(this);
        showValue.setOnClickListener(this);

        wv_bayer.addChangingListener(this);
    }

    private void initData(){
        wv_bayer.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mData_name));
        wv_bayer.setVisibleItems(5);
    }

    public void setCurrentValue(int value){
        wv_bayer.setCurrentItem(value);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closePop :
                this.dismiss();
                break;

            case R.id.showValue:
                bayerModeListener.selectBayer(pickValue);
                this.dismiss();
                break;
        }
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        pickValue = mData_value[newValue];
    }

    public interface BayerModeListener{
        void selectBayer(int value);
    }

    public void setBayerModeListener(BayerModeListener bayerModeListener){
        this.bayerModeListener = bayerModeListener;
    }
}
