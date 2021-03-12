package com.catchebstnew.www;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.catchbest.R;
import com.catchbest.cam;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.catchbest.KSJ_PARAM.KSJ_BLUE;
import static com.catchbest.KSJ_PARAM.KSJ_GREEN;
import static com.catchbest.KSJ_PARAM.KSJ_RED;
import static com.catchbest.KSJ_SENSITIVITYMODE.KSJ_LOW;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_EXTERNAL;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE;
import static com.catchbest.KSJ_WB_MODE.KSJ_SWB_AUTO_ONCE;


/**
 * 专门测试 RK3399上 6台相机
 */
public class Rk3399_641M_Activity extends AppCompatActivity {
    private ImageView imageView01;
    private ImageView imageView02;
    private ImageView imageView03;
    private ImageView imageView04;
    private ImageView imageView05;
    private ImageView imageView06;
    private TextView textView01;
    private TextView textView02;
    private TextView textView03;
    private TextView textView04;
    private TextView textView05;
    private TextView textView06;
    private TextView tvFailCount01;
    private TextView tvFailCount02;
    private TextView tvFailCount03;
    private TextView tvFailCount04;
    private TextView tvFailCount05;
    private TextView tvFailCount06;

    private ImageView[] imageViews;
    private TextView[] textViews;
    private TextView[] tvFailCounts;
    private int[] failtCount;

    private Lock lock;
    private cam ksjcam;
    private boolean isStart = false;//相机开启状态
    private long[] startTime;
    private int[] count;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime[msg.arg1] >= 3000) {
                        startTime[msg.arg1] = currentTime;
                        textViews[msg.arg1].setText("fps:" + new BigDecimal(count[msg.arg1] / 3.0).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                        count[msg.arg1] = 0;
                    } else {
                        currentTime = System.currentTimeMillis();
                    }
                    imageViews[msg.arg1].setImageBitmap((Bitmap) msg.obj);
                    count[msg.arg1]++;
                    break;
            }
        }
    };


    private Thread[] threads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rk3399_641_m_);

        imageView01 = findViewById(R.id.imageView01);
        imageView02 = findViewById(R.id.imageView02);
        imageView03 = findViewById(R.id.imageView03);
        imageView04 = findViewById(R.id.imageView04);
        imageView05 = findViewById(R.id.imageView05);
        imageView06 = findViewById(R.id.imageView06);
        textView01 = findViewById(R.id.textView01);
        textView02 = findViewById(R.id.textView02);
        textView03 = findViewById(R.id.textView03);
        textView04 = findViewById(R.id.textView04);
        textView05 = findViewById(R.id.textView05);
        textView06 = findViewById(R.id.textView06);
        tvFailCount01 = findViewById(R.id.tv_fail_count01);
        tvFailCount02 = findViewById(R.id.tv_fail_count02);
        tvFailCount03 = findViewById(R.id.tv_fail_count03);
        tvFailCount04 = findViewById(R.id.tv_fail_count04);
        tvFailCount05 = findViewById(R.id.tv_fail_count05);
        tvFailCount06 = findViewById(R.id.tv_fail_count06);

        imageViews = new ImageView[]{imageView01,imageView02,imageView03,imageView04,imageView05,imageView06};
        textViews = new TextView[]{textView01,textView02,textView03,textView04,textView05,textView06};
        tvFailCounts = new TextView[]{tvFailCount01,tvFailCount02,tvFailCount03,tvFailCount04,tvFailCount05,tvFailCount06};
        count = new int[]{0,0,0,0,0,0};
        startTime = new long[]{0,0,0,0,0,0};
        failtCount = new int[]{0,0,0,0,0,0};

        threads = new Thread[]{new MyThread(0),new MyThread(1),new MyThread(2),new MyThread(3),new MyThread(4),new MyThread(5)};

        lock = new ReentrantLock();
        init();
    }

    /**
     * 初始化cam，设置相机参数，采图
     */
    private void init() {
        ksjcam = new cam(this);
        ksjcam.Init();
        ksjcam.m_devicecount = ksjcam.DeviceGetCount();

        if (ksjcam.m_devicecount <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("未连接相机设备");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            return;
        }

        //获取相机信息
        /**int[] deviceTypeArray = new int[ksjcam.m_devicecount];
         int[] serialsArray = new int[ksjcam.m_devicecount];
         int[] firmwareVersionArray = new int[ksjcam.m_devicecount];
         ksjcam.DeviceGetInformation(0, deviceTypeArray, serialsArray, firmwareVersionArray);

         String content = "相机个数：" + ksjcam.m_devicecount + "\n相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0];
         Log.e(TAG,"info:" + content);//输出相机信息
         */
        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            ksjcam.CaptureSetFieldOfView(i, 0, 0, 728, 544);//设置采集区域
            ksjcam.SetTriggerMode(i, KSJ_TRIGGER_INTERNAL.ordinal());//触发模式
            ksjcam.WhiteBalanceSet(i, KSJ_SWB_AUTO_ONCE.ordinal());//白平衡
            ksjcam.ExposureTimeSet(i, 20);//曝光
            ksjcam.SensitivitySetMode(i, KSJ_LOW.ordinal());//设置灵敏度
            int isBlackWhite =  ksjcam.QueryFunction(0,0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机
            if (isBlackWhite == 1) {
                isStart = true;
                startImageViewBlackWhite(i);
            }
        }
    }

    /**
     * 黑白相机采图
     */
    private void startImageViewBlackWhite(final int index) {
        startTime[index] = System.currentTimeMillis();
        threads[index].start();
    }

    /**
     * 采黑白图
     * @param index 相加下标，只有一个相机index=0
     * @param width 采图宽度
     * @param height 采图高度
     * @return
     */
    public int captureAndShow_BlackWhite(final int index, int width, int height){
        byte[] byteArray = ksjcam.CaptureRAWdataArray(index, width, height);
        if (byteArray != null && byteArray.length > 0) {
            Bitmap bitmap = createBitmap_from_byte_alpha_data(width, height, byteArray);
            Message message = new Message();
            message.what = 0;
            message.arg1 = index;
            message.obj = bitmap;
            handler.sendMessage(message);
        } else {
            failtCount[index]++;
            Log.e("TAG", "第" + index + "相机采图失败次数: " + failtCount[index]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvFailCounts[index].setText("采图失败次数: " + failtCount[index]);
                }
            });
            return -1;
        }
        return 0;
    }

    /**
     * 生成bitmap
     * @param width 图宽
     * @param height 图高
     * @param buf   图片的字节数组
     * @return 生成bitmap
     */
    public Bitmap createBitmap_from_byte_alpha_data(int width, int height, byte[] buf) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(false);
        bitmap.setPixels(getIntArray(buf), 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 将字节数组转成int数组
     * @param b
     * @return
     */
    public static int[] getIntArray(byte[] b) {
        int[] intArray = new int[b.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = byteArrayToInt(b, i);
        }
        return intArray;
    }

    /**
     * byte装int
     * @param b
     * @param pos
     * @return
     */
    public static int byteArrayToInt(byte[] b, int pos) {
        int value = 0;
        value += (int) (b[pos] << 0);
        value += (int) (b[pos] << 8);
        value += (int) (b[pos] << 16);
        value += (int) (255 << 24);
        return value;

    }

    class MyThread extends Thread {
        private int index;
        public MyThread(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            super.run();
            while (isStart) {
//                    lock.lock();
                int ret = captureAndShow_BlackWhite(index, 728, 544);
                if (ret == -1) {
                    Log.e("TAG", "run: " + "一帧图片没取到");
                }
//                    lock.unlock();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        isStart = false;

    }
}
