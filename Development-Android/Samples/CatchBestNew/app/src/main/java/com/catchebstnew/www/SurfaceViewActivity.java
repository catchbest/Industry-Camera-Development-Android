package com.catchebstnew.www;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.catchbest.R;
import com.catchbest.cam;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.catchbest.KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP;
import static com.catchbest.KSJ_BAYERMODE.KSJ_GBRG_BGR32_FLIP;
import static com.catchbest.KSJ_BAYERMODE.KSJ_GRBG_BGR32_FLIP;
import static com.catchbest.KSJ_BAYERMODE.KSJ_RGGB_BGR32_FLIP;
import static com.catchbest.KSJ_PARAM.KSJ_BLUE;
import static com.catchbest.KSJ_PARAM.KSJ_GREEN;
import static com.catchbest.KSJ_PARAM.KSJ_RED;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL;
import static com.catchbest.KSJ_WB_MODE.KSJ_SWB_AUTO_ONCE;
import static com.catchbest.KSJ_SENSITIVITYMODE.KSJ_LOW;

public class SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback,View.OnClickListener {
    public static final String TAG = "SurfaceViewActivity";
    private final int PERMISSION_REQUEST = 0xa00;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView tv_fps;
    private Button btn_back;
    private Button btn_catch;

    private cam ksjcam;
    private boolean isStart = false;//相机开启状态
    private long startTime;
    private Lock lock;
    private double fbs = 0.0;
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        tv_fps = findViewById(R.id.tv_fps);
        btn_back = findViewById(R.id.btn_back);
        btn_catch = findViewById(R.id.btn_catch);
        btn_back.setOnClickListener(this);
        btn_catch.setOnClickListener(this);

        lock = new ReentrantLock();
        init();
    }

    /**
     * 初始化cam，设置相机相关参数
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
//        int[] deviceTypeArray = new int[ksjcam.m_devicecount];
//        int[] serialsArray = new int[ksjcam.m_devicecount];
//        int[] firmwareVersionArray = new int[ksjcam.m_devicecount];
//        ksjcam.DeviceGetInformation(0, deviceTypeArray, serialsArray, firmwareVersionArray);

//        String content = "相机个数：" + ksjcam.m_devicecount + "\n相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0];
//        Log.e(TAG,"info:" + content);//输出相机信息

        ksjcam.CaptureSetFieldOfView(0, 0, 0, 2592, 1944);//设置采集区域
        ksjcam.SetBayerMode(0, KSJ_GRBG_BGR32_FLIP.ordinal());//linux环境下，彩色相机可以设置KSJ_BGGR_BGR32_FLIP、KSJ_GRBG_BGR32_FLIP、KSJ_RGGB_BGR32_FLIP、KSJ_GBRG_BGR32_FLIP四种模式
        ksjcam.SetParam(0, KSJ_RED.ordinal(), 48);//增益
        ksjcam.SetParam(0, KSJ_GREEN.ordinal(), 48);//增益
        ksjcam.SetParam(0, KSJ_BLUE.ordinal(), 48);//增益
        ksjcam.SetTriggerMode(0, KSJ_TRIGGER_INTERNAL.ordinal());//触发模式
        ksjcam.WhiteBalanceSet(0, KSJ_SWB_AUTO_ONCE.ordinal());//白平衡
        ksjcam.ExposureTimeSet(0, 20);//曝光
        ksjcam.SensitivitySetMode(0, KSJ_LOW.ordinal());//设置灵敏度
        int isBlackWhite =  ksjcam.QueryFunction(0,0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机
        if (isBlackWhite == 0) {
            Toast.makeText(this, "彩色相机", Toast.LENGTH_SHORT).show();
            isStart = true;
            startSurfaceView();
        }
    }

    /**
     * 开始采图
     */
    public void startSurfaceView() {
        startTime = System.currentTimeMillis();

        Thread captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    lock.lock();
                    if ((System.currentTimeMillis()  - startTime) >= 3000) {
                        fbs = new BigDecimal(count / 3.0).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                tv_fps.setText("fps:" + fbs);
                            }
                        });
                        count = 0;
                        startTime = System.currentTimeMillis();
                    }
                    boolean b = surfaceHolder.getSurface().isValid();
                    if (b) {
                        ksjcam.CaptureBySurface(0, surfaceHolder.getSurface(), 0);//彩色相机成像函数
                        ++count;
                    }
                    lock.unlock();
                }
            }
        });
        captureThread.start();
    }

    String path = "/sdcard/CatchBest";
    File outfile = new File(path);
    String time;
    int result = -1;
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 抓取一帧图片，注意线程加锁，防止影响正常的采图
     */
    private void catchBitmap() {
        lock.lock();
        time = formatter.format(new Date());
        // 如果文件不存在，则创建一个新文件
        if (!outfile.isDirectory()) {
            try {
                outfile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        result = ksjcam.CaptureBitmap(0, path + "/catchbest" + time + ".bmp");
        if (result == 0) {//result:0表示 抓取成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SurfaceViewActivity.this, "保存图片成功，" + path + "/catchbest" + time + ".bmp", Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(path + "/catchbest" + time + ".bmp");
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            sendBroadcast(intent);//这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SurfaceViewActivity.this, "保存失败，errorCode：" + result, Toast.LENGTH_SHORT).show();
                }
            });
        }
        lock.unlock();

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        catchBitmap();
                    }
                }).start();
            } else {
                Toast.makeText(this, "未授权写入sdk权限，无法保存图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.btn_catch:
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//未授权写权限
                    requestPermission();//请求写权限
                } else {//已授权写权限
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            catchBitmap();
                        }
                    }).start();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStart = false;
        tv_fps.setText("fps:0.0");
        
    }


}
