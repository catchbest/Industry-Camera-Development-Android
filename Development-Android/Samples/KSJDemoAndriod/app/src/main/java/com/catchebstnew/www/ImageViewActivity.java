package com.catchebstnew.www;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.catchbest.R;
import com.catchbest.cam;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.catchbest.KSJ_PARAM.KSJ_BLUE;
import static com.catchbest.KSJ_PARAM.KSJ_GREEN;
import static com.catchbest.KSJ_PARAM.KSJ_RED;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_SOFTWARE;
import static com.catchbest.KSJ_WB_MODE.KSJ_SWB_AUTO_ONCE;
import static com.catchbest.KSJ_SENSITIVITYMODE.KSJ_LOW;

public class ImageViewActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "ImageViewActivity";
    private final int PERMISSION_REQUEST = 0xa00;
    private ImageView imageView;
    private TextView tv_fps;
    private TextView tv_failCount;
    private Button btn_back;
    private Button btn_catch;

    private cam ksjcam;
    private boolean isStart = false;//相机开启状态
    private boolean isWorking = false;//相机工作状态
    private boolean isMonoCamera = true; //是否黑白相机
    private long startTime;
    private double fbs = 0.0;
    private int count = 0;
    private int imgWidth = 0;
    private int imgHeight = 0;
    private int imgBitCount = 0;

    private Lock lock;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= 3000) {
                        startTime = currentTime;
                        fbs = new BigDecimal(count / 3.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        tv_fps.setText("fps:" + fbs);
                        count = 0;
                    } else {
                        currentTime = System.currentTimeMillis();
                    }
                    imageView.setImageBitmap((Bitmap) msg.obj);
                    count++;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageView = findViewById(R.id.imageView);
        tv_fps = findViewById(R.id.tv_fps);
        tv_failCount = findViewById(R.id.tv_fail_count);
        btn_back = findViewById(R.id.btn_back);
        btn_catch = findViewById(R.id.btn_catch);
        btn_back.setOnClickListener(this);
        btn_catch.setOnClickListener(this);

        lock = new ReentrantLock();

    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    /**
     * 初始化cam，设置相机参数，采图
     */
    private void init() {
        ksjcam = new cam();

        int rr = ksjcam.EnableLog(0);

        String logstr = "EnableLog：" + rr;
        Log.e("TAG","info:" + logstr);//输出相机信息

        ksjcam.UnInit();
        upgradeRootPermission("chmod -R 777 /dev/bus/usb/");  //相机重新插拔的话，需要授权
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
        int[] deviceTypeArray = new int[ksjcam.m_devicecount];
         int[] serialsArray = new int[ksjcam.m_devicecount];
         int[] firmwareVersionArray = new int[ksjcam.m_devicecount];
        int rt = ksjcam.DeviceGetInformation(0, deviceTypeArray, serialsArray, firmwareVersionArray);

//        byte[] byteArrayinfo = new byte[32];
//        byteArrayinfo = ksjcam.RdEEPROMExEx(0, 261088, 32);
//        Log.e("TAG", "init: size=" +  ksjcam.RdEEPROMExEx(0, 261088, 32).length );
//         String content = "相机个数：" + ksjcam.m_devicecount + "\n相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0] + "\n SN：" + byteArrayinfo[0] + " " + byteArrayinfo[1] + " " + byteArrayinfo[2] + " " + byteArrayinfo[3] + " " + byteArrayinfo[31];
//         Log.e("TAG","info:" + content);//输出相机信息
         String content = "相机个数：" + ksjcam.m_devicecount + "\nRet=:" + rt + "\n相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0];
         Log.e("TAG","info:" + content);//输出相机信息

        int[] nxStart = new int[1];
        int[] nyStart = new int[1];
        int[] nWidth = new int[1];
        int[] nHeight = new int[1];

        //ksjcam.CaptureSetFieldOfView(0, 0, 0, 2592, 1944);//设置采集区域
        ksjcam.CaptureGetFieldOfView(0, nxStart, nyStart, nWidth, nHeight);

        if (nWidth[0] <= 0 || nHeight[0] <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("相机视场错误设备");
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

        imgWidth = nWidth[0];
        imgHeight = nHeight[0];

        ksjcam.SetTriggerMode(0, KSJ_TRIGGER_INTERNAL.ordinal());//触发模式
        ksjcam.WhiteBalanceSet(0, KSJ_SWB_AUTO_ONCE.ordinal());//白平衡
        ksjcam.ExposureTimeSet(0, 20);//曝光
//        ksjcam.SensitivitySetMode(0, KSJ_LOW.ordinal());//设置灵敏度
        int isBlackWhite = ksjcam.QueryFunction(0, 0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机
        if (isBlackWhite == 1) {
            isMonoCamera = true;
            imgBitCount = 24;
        }
        else {
            isMonoCamera = false;
            imgBitCount = 8;
        }

         isStart = true;
         startTime = System.currentTimeMillis();
         startImageView();
    }

    /**
     * 黑白相机采图
     */
    private void startImageView() {
        startTime = System.currentTimeMillis();
        Thread captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isWorking=true;
                if(isMonoCamera) {
                    while (isStart) {
                        lock.lock();
                        int ret = captureAndShow_Mono(0, imgWidth, imgHeight);
                        if (ret == -1) {
                            upgradeRootPermission("chmod -R 777 /dev/bus/usb/");  //相机重新插拔的话，需要授权
                            Log.e("TAG", "run: " + "一帧图片没取到");
                        }
                        lock.unlock();
                    }
                }
                else {
                    while (isStart) {
                        lock.lock();
                        int ret = captureAndShow_RGB(0, imgWidth, imgHeight);
                        if (ret == -1) {
                            upgradeRootPermission("chmod -R 777 /dev/bus/usb/");  //相机重新插拔的话，需要授权
                            Log.e("TAG", "run: " + "一帧图片没取到");
                        }
                        lock.unlock();
                    }
                }
                isWorking=false;
            }
        });
        captureThread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.btn_catch:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {//未授权写权限
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


    int failCount = 0;

    /**
     * 采黑白图
     *
     * @param index  相加下标，只有一个相机index=0
     * @param width  采图宽度
     * @param height 采图高度
     * @return
     */
    public int captureAndShow_Mono(final int index, int width, int height) {
        byte[] byteArray = ksjcam.CaptureRAWdataArray(index, width, height);

        if (byteArray != null && byteArray.length > 0) {
            Bitmap bitmap = createBitmap_from_byte_alpha_data(width, height, byteArray);
            Message message = new Message();
            message.what = 0;
            message.arg1 = index;
            message.obj = bitmap;
            handler.sendMessage(message);
        } else {
            failCount++;
            Log.e("TAG", "采图失败次数: " + failCount);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_failCount.setText("采图失败次数：" + failCount);
                }
            });
            return -1;
        }

        return 0;
    }

    /**
     * 采RGB图
     *
     * @param index  相加下标，只有一个相机index=0
     * @param width  采图宽度
     * @param height 采图高度
     * @return
     */
    public int captureAndShow_RGB(final int index, int width, int height) {
        byte[] byteArray = ksjcam.CaptureRGBdataArray(index, width, height);
        if (byteArray != null && byteArray.length > 0) {
            Bitmap bitmap = createBitmap_from_rgb_alpha_data(width, height, byteArray);
            Message message = new Message();
            message.what = 0;
            message.arg1 = index;
            message.obj = bitmap;
            handler.sendMessage(message);
        } else {
            failCount++;
            Log.e("TAG", "采图失败次数: " + failCount);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_failCount.setText("采图失败次数：" + failCount);
                }
            });
            return -1;
        }

        return 0;
    }

    /**
     * 生成bitmap
     *
     * @param width  图宽
     * @param height 图高
     * @param buf    图片的字节数组
     * @return 生成bitmap
     */
    public Bitmap createBitmap_from_byte_alpha_data(int width, int height, byte[] buf) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(false);
        bitmap.setPixels(getIntArrayMono(buf), 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 生成bitmap
     *
     * @param width  图宽
     * @param height 图高
     * @param buf    图片的字节数组
     * @return 生成bitmap
     */
    public Bitmap createBitmap_from_rgb_alpha_data(int width, int height, byte[] buf) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(false);
        bitmap.setPixels(getIntArrayRGB(buf), 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 将字节数组转成int数组
     *
     * @param b
     * @return
     */
    public static int[] getIntArrayMono(byte[] b) {
        int[] intArray = new int[b.length];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = byteArrayToInt(b, i);
        }
        return intArray;
    }

    /**
     * 将字节数组转成int数组
     *
     * @param b
     * @return
     */
    public static int[] getIntArrayRGB(byte[] b) {
        int[] intArray = new int[b.length/3];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = rgbArrayToInt(b, 3*i);
        }
        return intArray;
    }

    /**
     * byte装int
     *
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

    /**
     * byte装int
     *
     * @param b
     * @param pos
     * @return
     */
    public static int rgbArrayToInt(byte[] b, int pos) {
        int value = 0;
        value += (int) (b[pos]);
        value += (int) (b[pos+1] << 8);
        value += (int) (b[pos+2] << 16);
        value += (int) (255 << 24);
        return value;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
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
        if (result == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ImageViewActivity.this, "保存图片成功，" + path + "/catchbest" + time + ".bmp", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ImageViewActivity.this, "保存失败，errorCode：" + result, Toast.LENGTH_SHORT).show();
                }
            });
        }
        lock.unlock();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isStart = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

    }

    /** 彩色相机用ImageView成像
     //彩色相机采图
     private void startImageViewColor() {
     startTime = System.currentTimeMillis();
     Thread captureThread = new Thread(new Runnable() {
    @Override public void run() {
    while (isStart) {
    int ret = captureAndShow_Color(0, 728, 544);
    if (ret == -1) {
    isStart = false;
    runOnUiThread(new Runnable() {
    @Override public void run() {
    tv_fps.setText("fps:0.0");
    }
    });
    }
    }
    }
    });
     captureThread.start();
     }

     public int captureAndShow_Color(final int index, int width, int height){
     int[] dataArray = ksjcam.CaptureRGBdataIntArray(index, width, height);
     if (dataArray != null && dataArray.length > 0) {
     Bitmap bmp = CreateBitmap_from_int_rgba_data(width, height, dataArray);
     Message message = new Message();
     message.what = 0;
     message.arg1 = index;
     message.obj = bmp;
     handler.sendMessage(message);
     } else {
     return -1;
     }
     return 0;
     }

     public Bitmap CreateBitmap_from_int_rgba_data(int width, int height, int[] buf) {
     Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
     bmp.setHasAlpha(false);
     bmp.setPixels(buf, 0, width, 0, 0, width, height);
     return bmp;
     }
     */

    public String upgradeRootPermission(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());
            dos.writeBytes(cmd + "\n");//更改dev/bus/usb 权限为 777
            dos.flush();
            dos.writeBytes("setenforce 0" + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            while ((line = br.readLine()) != null) {
                Log.d(TAG, "result:" + line);
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            p.destroy();
        }

        return result;
    }
}
