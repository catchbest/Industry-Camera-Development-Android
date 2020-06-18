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

import com.catchbest.KSJ_TRIGGRMODE;
import com.catchbest.R;
import com.catchebstnew.www.adapter.ArrayWheelAdapter;
import com.catchebstnew.www.callback.OnWheelChangedListener;

/**
 * Created by gmm on 2018/1/3.
 */

public class TriggerPopupwindow extends PopupWindow implements View.OnClickListener,OnWheelChangedListener {
    private LayoutInflater inflater;
    private View view;
    private Button closePop,showValue;
    private WheelView wv_wb;
    private TextView selectValue;
    private Context mContext;
    private int pickValue;
    private TriggerModeListener triggerModeListener;
    private int[] mData_value = {KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL.ordinal(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_EXTERNAL.ordinal(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE.ordinal(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_FIXFRAMERATE.ordinal(),
            };

    private String[] mData_name = {KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL.name(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_EXTERNAL.name(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE.name(),
            KSJ_TRIGGRMODE.KSJ_TRIGGER_FIXFRAMERATE.name(),};

    public TriggerPopupwindow(Activity context){
        super(context);
        mContext = context;
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
        wv_wb = (WheelView) view.findViewById(R.id.wv_wb);
        selectValue = (TextView) view.findViewById(R.id.selectValue);

        selectValue.setText("触发模式");
    }

    private void initListener(){
        closePop.setOnClickListener(this);
        showValue.setOnClickListener(this);

        wv_wb.addChangingListener(this);
    }

    private void initData(){
        wv_wb.setViewAdapter(new ArrayWheelAdapter<String>(mContext, mData_name));
        wv_wb.setVisibleItems(5);
    }

    public void setCurrentValue(int value){
        wv_wb.setCurrentItem(value);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closePop :
                this.dismiss();
                break;

            case R.id.showValue:
                triggerModeListener.selectTrigger(pickValue);
                this.dismiss();
                break;
        }
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        pickValue = mData_value[newValue];
    }

    public interface TriggerModeListener{
        void selectTrigger(int value);
    }

    public void setTriggerModeListener(TriggerModeListener triggerModeListener) {
        this.triggerModeListener = triggerModeListener;
    }
}
