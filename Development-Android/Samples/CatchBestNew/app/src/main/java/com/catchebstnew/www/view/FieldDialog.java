package com.catchebstnew.www.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.catchbest.R;


/**
 * Created by gmm on 2018/1/5.
 */

public class FieldDialog extends Dialog implements View.OnClickListener{

    private EditText et_x,et_y,et_width,et_height;
    private Button btn_cancel,btn_confirm;
    private FieldViewListener fieldViewListener;
//    private LinearLayout rootView;
    private Activity mContext;
    public FieldDialog(Activity context) {
        super(context, R.style.ExceptionDialog);
        this.mContext = context;
        setDialog();
    }

    private void setDialog(){
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_field, null);
        et_x = (EditText) mView.findViewById(R.id.et_x);
        et_y = (EditText) mView.findViewById(R.id.et_y);
        et_width = (EditText) mView.findViewById(R.id.et_width);
        et_height = (EditText) mView.findViewById(R.id.et_height);
//        rootView = (LinearLayout) mView.findViewById(R.id.rootView);

        btn_cancel = (Button) mView.findViewById(R.id.btn_cancel);
        btn_confirm = (Button) mView.findViewById(R.id.btn_confirm);

        btn_cancel.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);

//        DisplayMetrics dm = new DisplayMetrics();
//        mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenWidth = dm.widthPixels;
//        int screenHeight = dm.heightPixels;
//        Log.e("TAG", "screenWidth---" +screenWidth);

        super.setContentView(mView);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootView.getLayoutParams();
//        params.width = screenWidth * (2/3);
//        rootView.setLayoutParams(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel :
                this.dismiss();
                break;

            case R.id.btn_confirm:
                fieldViewListener.setValue(Integer.parseInt(et_x.getText().toString().equals("")?"0":et_x.getText().toString()),Integer.parseInt(et_y.getText().toString().equals("")?"0":et_y.getText().toString())
                                            ,Integer.parseInt(et_width.getText().toString().equals("")?"0":et_width.getText().toString()),Integer.parseInt(et_height.getText().toString().equals("")?"0":et_height.getText().toString()));
                break;
        }
    }

    public void setData(int x,int y,int width,int height){
        et_x.setText(x + "");
        et_y.setText(y + "");
        et_width.setText(width + "");
        et_height.setText(height + "");
    }

    public interface FieldViewListener{
        void setValue(int x, int y, int width, int height);
    }

    public void setFieldViewListener(FieldViewListener fieldViewListener) {
        this.fieldViewListener = fieldViewListener;
    }

}
