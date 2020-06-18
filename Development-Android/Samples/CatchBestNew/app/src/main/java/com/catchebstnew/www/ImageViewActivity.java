package com.catchebstnew.www;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.catchbest.KSJ_BAYERMODE;
import com.catchbest.KSJ_TRIGGRMODE;
import com.catchbest.KSJ_WB_MODE;
import com.catchbest.R;
import com.catchbest.cam;
import com.catchebstnew.www.adapter.DeviceInfoAdapter;
import com.catchebstnew.www.view.BayerModePopupwindow;
import com.catchebstnew.www.view.ExposurePopupwindow;
import com.catchebstnew.www.view.FieldDialog;
import com.catchebstnew.www.view.GainPopupwindow;
import com.catchebstnew.www.view.TriggerPopupwindow;
import com.catchebstnew.www.view.WhiteBalancePopupwindow;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.catchbest.KSJ_PARAM.KSJ_BLUE;
import static com.catchbest.KSJ_PARAM.KSJ_EXPOSURE;
import static com.catchbest.KSJ_PARAM.KSJ_EXPOSURE_LINES;
import static com.catchbest.KSJ_PARAM.KSJ_GREEN;
import static com.catchbest.KSJ_PARAM.KSJ_MIRROR;
import static com.catchbest.KSJ_PARAM.KSJ_RED;
import static com.catchbest.KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL;
import static com.catchbest.KSJ_WB_MODE.KSJ_HWB_PRESETTINGS;
import static com.catchbest.KSJ_WB_MODE.KSJ_SWB_AUTO_ONCE;

public class ImageViewActivity extends AppCompatActivity implements
         ExposurePopupwindow.ExposureChangeListener
        , WhiteBalancePopupwindow.WhiteBalanceSelectListener
        , TriggerPopupwindow.TriggerModeListener
        , BayerModePopupwindow.BayerModeListener
        , GainPopupwindow.RedGainChangeListener
        , GainPopupwindow.GreenGainChangeListener
        , GainPopupwindow.BlueGainChangeListener {
    private final int PERMISSION_REQUEST = 0xa00;
    private LinearLayout container;
    private LinearLayout imageViewContainer;
    private ImageView imageView;
    private TextView tv_fps;
    private TextView tv_failCount;
    private TextView tv_device_info;
    private RecyclerView listView;

    private GainPopupwindow gainPopup;
    private ExposurePopupwindow exposurePopup;
    private WhiteBalancePopupwindow whiteBalancePopupwindow;
    private TriggerPopupwindow triggerPopupwindow;
    private BayerModePopupwindow bayerModePopupwindow;
    private FieldDialog fieldDialog;
    private int currentWB = KSJ_WB_MODE.KSJ_HWB_PRESETTINGS.ordinal();
    private int currentTrigger = KSJ_TRIGGRMODE.KSJ_TRIGGER_INTERNAL.ordinal();
    private int currentBayer = KSJ_BAYERMODE.KSJ_BGGR_BGR32_FLIP.ordinal();

    private cam ksjcam;
    private boolean isStart = false;//相机开启状态
    private boolean isWorking = false;//相机工作状态
    private long startTime;
    private double fbs = 0.0;
    private int count = 0;
    private Lock lock;

    private boolean isMirror = false;
    private boolean isLut = true;
    private int exposureLine = 200;
    private int redGain = 48;
    private int greenGain = 48;
    private int blueGain = 48;

    private List<String> deviceArray =  new ArrayList<>();
    private int curSelectCamera = 0;
    private int nextSelectCamera = 0;

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

        container =  findViewById(R.id.container);
        imageViewContainer = findViewById(R.id.imageViewContainer);
//        imageView = findViewById(R.id.imageView);
        imageView = new ImageView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageViewContainer.addView(imageView);
        tv_fps = findViewById(R.id.tv_fps);
        tv_failCount = findViewById(R.id.tv_fail_count);
        tv_device_info = findViewById(R.id.tv_device);
        listView = findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        gainPopup = new GainPopupwindow(this);
        exposurePopup = new ExposurePopupwindow(this);
        whiteBalancePopupwindow = new WhiteBalancePopupwindow(this);
        triggerPopupwindow = new TriggerPopupwindow(this);
        bayerModePopupwindow = new BayerModePopupwindow(this);
        gainPopup.setRedChangeListener(this);
        gainPopup.setGreenChangeListener(this);
        gainPopup.setBlueChangeListener(this);
        exposurePopup.setExposureChangeListener(this);
        whiteBalancePopupwindow.setWhiteBalanceSelectListener(this);
        triggerPopupwindow.setTriggerModeListener(this);
        bayerModePopupwindow.setBayerModeListener(this);

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

        for (int i = 0; i < ksjcam.m_devicecount; i++) {
            //获取相机信息
            int[] deviceTypeArray = new int[1];
            int[] serialsArray = new int[1];
            int[] firmwareVersionArray = new int[1];
            ksjcam.DeviceGetInformation(i, deviceTypeArray, serialsArray, firmwareVersionArray);
            String content = "相机 " + (i + 1) + "/" + ksjcam.m_devicecount + "\n相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0];
            int isBlackWhite = ksjcam.QueryFunction(i, 0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机
            if (isBlackWhite == 1) {
                Log.e("TAG", "====== 相机信息 ====== :\n" + content + "\n黑白相机");//输出相机信息
            } else {
                Log.e("TAG", "====== 相机信息 ====== :\n" + content + "\n彩色相机");//输出相机信息
            }

            deviceArray.add(DeviceTypes[deviceTypeArray[0]] + "_" + serialsArray[0]);

            ksjcam.SetTriggerMode(i, KSJ_TRIGGER_INTERNAL.ordinal());//触发模式
            ksjcam.WhiteBalanceSet(i, KSJ_HWB_PRESETTINGS.ordinal());//白平衡
            ksjcam.SetParam(i, KSJ_EXPOSURE_LINES.ordinal(), exposureLine);//曝光
            //ksjcam.SetParam(i, KSJ_RED.ordinal(), 16);
            //ksjcam.SetParam(i, KSJ_GREEN.ordinal(), 16);
            //ksjcam.SetParam(i, KSJ_BLUE.ordinal(), 16);
            //ksjcam.SensitivitySetMode(0, KSJ_LOW.ordinal());//设置灵敏度
        }

        tv_device_info.setText(deviceArray.get(0));

        DeviceInfoAdapter adapter = new DeviceInfoAdapter(this,deviceArray);
        listView.setAdapter(adapter);
        adapter.setOnItemClickListener(new DeviceInfoAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                isStart = false;
                imageViewContainer.removeView(imageView);
                imageView = null;
                for (int i = 0; i < 20; ++i) { // 等待2秒钟，确认线程退出
                    if (!isWorking) break;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                nextSelectCamera = position;
                listView.setVisibility(View.GONE);

                imageView = new ImageView(ImageViewActivity.this);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);
                imageViewContainer.addView(imageView);
                isStart = true;
                tv_device_info.setText(deviceArray.get(position));
                startImageView();
            }
        });

        nextSelectCamera = 0;

        isStart = true;
        startTime = System.currentTimeMillis();
        startImageView();
    }
    /**
     * 黑白相机采图
     */
    private void startImageView() {
        final int camIndex = nextSelectCamera;
        curSelectCamera = nextSelectCamera;

        int[] nxStart = new int[1];
        int[] nyStart = new int[1];
        int[] nWidth = new int[1];
        int[] nHeight = new int[1];

        ksjcam.CaptureGetFieldOfView(camIndex, nxStart, nyStart, nWidth, nHeight);

        if (nWidth[0] <= 0 || nHeight[0] <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("相机视场错误");
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

        final int imgWidth = nWidth[0];
        final int imgHeight = nHeight[0];

        final int isBlackWhite = ksjcam.QueryFunction(camIndex, 0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机

        startTime = System.currentTimeMillis();
        Thread captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isWorking=true;
                if(isBlackWhite != 0) {
                    while (isStart) {
                        if (camIndex != nextSelectCamera) break;
                        lock.lock();
                        int ret = captureAndShow_Mono(camIndex, imgWidth, imgHeight);
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
                        int ret = captureAndShow_RGB(camIndex, imgWidth, imgHeight);
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
            //runOnUiThread(new Runnable() {
            //    @Override
            //    public void run() {
            //        tv_failCount.setText("采图失败次数：" + failCount);
            //    }
            //});
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
            //runOnUiThread(new Runnable() {
            //    @Override
            //    public void run() {
            //        tv_failCount.setText("采图失败次数：" + failCount);
            //    }
            //});
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
        bitmap.setPixels(getIntArrayMono(buf, width, height), 0, width, 0, 0, width, height);
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
        bitmap.setPixels(getIntArrayRGB(buf, width, height), 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 将字节数组转成int数组
     *
     * @param b
     * @return
     */
    public static int[] getIntArrayMono(byte[] b, int width, int height) {
        int[] intArray = new int[b.length];
        for (int j = 0; j < height; j++) {
            for (int i=0; i<width; ++i) {
                intArray[((height-j-1)*width)+i] = byteArrayToInt(b, j*width+i);
            }
        }
        return intArray;
    }

    /**
     * 将字节数组转成int数组
     *
     * @param b
     * @return
     */
    public static int[] getIntArrayRGB(byte[] b, int width, int height) {
        int[] intArray = new int[b.length/3];
        for (int j = 0; j < height; j++) {
            for (int i=0; i<width; ++i) {
                intArray[((height-j-1)*width)+i] = rgbArrayToInt(b, 3 * (j*width+i));
            }
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
        result = ksjcam.CaptureBitmap(curSelectCamera, path + "/catchbest" + time + ".bmp");
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
                Log.d("TAG", "result:" + line);
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

    public int m_nwidth;
    public int m_nheight;
    public int m_x;
    public int m_y;
    int min[] = new int[1];
    int max[] = new int[1];

    @Override
    public void redChange(int red) {
        ksjcam.SetParam(curSelectCamera, KSJ_RED.ordinal(), red);
    }

    @Override
    public void greenChange(int green) {
        ksjcam.SetParam(curSelectCamera, KSJ_GREEN.ordinal(), green);
    }

    @Override
    public void blueChange(int blue) {
        ksjcam.SetParam(curSelectCamera, KSJ_BLUE.ordinal(), blue);
    }

    @Override
    public void exposureChange(int progress) {
        exposureLine =  progress;
        Log.e("TAG", "====== exposureLine ====== : " + exposureLine);
        ksjcam.SetParam(curSelectCamera, KSJ_EXPOSURE_LINES.ordinal(), exposureLine);
    }

    @Override
    public void selectWB(int value) {
        currentWB = value;
        ksjcam.WhiteBalanceSet(curSelectCamera, value);

    }

    @Override
    public void selectTrigger(int value) {
        currentTrigger = value;
        ksjcam.SetTriggerMode(curSelectCamera, value);

    }

    @Override
    public void selectBayer(int value) {
        currentBayer = value;
        ksjcam.SetBayerMode(curSelectCamera, value);
    }


    public void back(View view) {
        finish();
    }

    public void catchBitmap(View view) {
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
    }

    public void setWhiteBalance(View view) {
        whiteBalancePopupwindow.showPopupWindow(container);
        whiteBalancePopupwindow.setCurrentValue(currentWB);
    }

    public void setRbgGain(View view) {
        final int[] redGain = new int[1];
        final int[] greenGain = new int[1];
        final int[] blueGain = new int[1];
        ksjcam.GetParam(curSelectCamera, KSJ_RED.ordinal(), redGain);
        ksjcam.GetParam(curSelectCamera, KSJ_GREEN.ordinal(), greenGain);
        ksjcam.GetParam(curSelectCamera, KSJ_BLUE.ordinal(), blueGain);
        gainPopup.setData(redGain[0], greenGain[0], blueGain[0]);
        ksjcam.GetParamRange(curSelectCamera, KSJ_RED.ordinal(), min, max);
        gainPopup.setRedRange(min[0], max[0]);
        ksjcam.GetParamRange(curSelectCamera, KSJ_GREEN.ordinal(), min, max);
        gainPopup.setGreenRange(min[0], max[0]);
        ksjcam.GetParamRange(curSelectCamera, KSJ_BLUE.ordinal(), min, max);
        gainPopup.setBlueRange(min[0], max[0]);
        gainPopup.showPupwindow(container);
    }

    public void setBayerMode(View view) {
        bayerModePopupwindow.showPopupWindow(container);
        bayerModePopupwindow.setCurrentValue(currentBayer);
    }

    public void setExposure(View view) {
        exposurePopup.setData(exposureLine);
        ksjcam.GetParamRange(curSelectCamera, KSJ_EXPOSURE_LINES.ordinal(), min, max);
        if (max[0] > 999999) max[0]=65536;
        exposurePopup.setRange(min[0], max[0]);
        exposurePopup.showPopupwindow(container);
    }

    public void setTriggerModel(View view) {
        triggerPopupwindow.showPopupWindow(container);
        triggerPopupwindow.setCurrentValue(currentTrigger);
    }

    public void setMirror(View view) {
        if (!isMirror) {
            ksjcam.SetParam(curSelectCamera, KSJ_MIRROR.ordinal(), 1);//镜像  默认0
            isMirror = true;
        } else {
            ksjcam.SetParam(curSelectCamera, KSJ_MIRROR.ordinal(), 0);//镜像  默认0
            isMirror = false;
        }
    }

    public void setField(View view) {
        int[] widtharray = new int[1];
        int[] heightarray = new int[1];
        int[] xarray = new int[1];
        int[] yarray = new int[1];
        ksjcam.CaptureGetFieldOfView(curSelectCamera, xarray, yarray, widtharray, heightarray);
        m_nwidth = widtharray[0];
        m_nheight = heightarray[0];
        m_x = xarray[0];
        m_y = yarray[0];
        fieldDialog = new FieldDialog(this);
        fieldDialog.setData(m_x, m_y, m_nwidth, m_nheight);
        fieldDialog.show();
        fieldDialog.setFieldViewListener(new FieldDialog.FieldViewListener() {
            @Override
            public void setValue(int x, int y, int width, int height) {
                fieldDialog.dismiss();

                isStart = false;
                imageViewContainer.removeView(imageView);
                imageView = null;
                for (int i = 0; i < 20; ++i) { // 等待2秒钟，确认线程退出
                    if (!isWorking) break;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                imageView = new ImageView(ImageViewActivity.this);
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);
                imageViewContainer.addView(imageView);
                ksjcam.CaptureSetFieldOfView(curSelectCamera, x, y, width, height);

                isStart = true;
                startImageView();
            }
        });
    }

    public void setLut(View view) {
        if (isLut) {
            ksjcam.LutSetEnable(curSelectCamera, 0);//LUT 默认 1
            isLut = false;
        } else {
            ksjcam.LutSetEnable(curSelectCamera, 1);//LUT 默认 1
            isLut = true;
        }
    }

    public void setSensitivity(View view) {
        int sensitivity = 0;
        if (sensitivity == 4) {
            sensitivity = 0;
        } else {
            sensitivity++;
        }
        ksjcam.SensitivitySetMode(curSelectCamera, sensitivity);
    }

    public void selectDevice(View view) {
        if (ksjcam.m_devicecount == 0)
            return;
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
        }
    }

    public void dismissDeviceListView(View view) {
        if (ksjcam.m_devicecount == 0)
            return;
        if (listView.getVisibility() == View.VISIBLE) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
        }
    }

    String[] DeviceTypes =
            {
                    "Gauss2 UC130C(MRNN)",
                    "Gauss2 UC130M(MRNN)",
                    "xxxxxxxxxxxxxxxxxxx",
                    "Gauss2 UC320C(MRNN)",
                    "Gauss2 UC130C(MRYN)",
                    "Gauss2 UC130M(MRYN)",
                    "xxxxxxxxxxxxxxxxxxx",
                    "Gauss2 UC320C(MRYN)",
                    "Gauss2 UC500C(MRNN)",
                    "Gauss2 UC500M(MRNN)",
                    "Gauss2 UC500C(MRYN)",
                    "Gauss2 UC500M(MRYN)",
                    "xxxxxxx UC320C(OCR)",
                    "xxxxxxx UC900C(MRNN)",
                    "xxxxxxx UC1000C(MRNN)",
                    "xxxxxxx UC900C(MRYN)",
                    "xxxxxxx UC1000C(MRYN)",
                    "Elanus2 UC130C(MRYY)",
                    "Elanus2 UC130M(MRYY)",
                    "xxxxxxx UD140C(SRNN)",
                    "xxxxxxx UD140M(SRNN)",
                    "xxxxxxx UC36C(MGNN)",
                    "xxxxxxx UC36M(MGNN)",
                    "xxxxxxx UC36C(MGYN)",
                    "xxxxxxx UC36M(MGYN)",
                    "Elanus2 UC900C(MRYY)",
                    "Elanus2 UC1000C(MRYY)",
                    "Elanus2 UC1400C(MRYY)",
                    "Elanus2 UC36C(MGYY)",
                    "Elanus2 UC36M(MGYY)",
                    "Elanus2 UC320C(MRYY)",
                    "Elanus2 UC500C(MRYY)",
                    "Elanus2 UC500M(MRYY)",
                    "CatchBEST MUC130C(MRYN)",
                    "CatchBEST MUC130M(MRYN)",
                    "CatchBEST MUC320C(MRYN)",
                    "Jelly2 MUC36C(MGYYO)",
                    "Jelly2 MUC36M(MGYYO)",
                    "xxxxxx MUC130C(MRYY)",
                    "Jelly2 MUC130M(MRYY)",
                    "Jelly2 MUC320C(MRYY)",
                    "Jelly2 MUC500C(MRYYO)",
                    "Jelly2 MUC500M(MRYYO)",
                    "xxxxxx MUC900C(MRYY)",
                    "xxxxxx MUC1000C(MRYY)",
                    "xxxxxx MUC1400C(MRYY)",
                    "Elanus2 UD205C(SGYY)",
                    "Elanus2 UD205M(SGYY)",
                    "Elanus2 UD274C(SGYY)",
                    "Elanus2 UD274M(SGYY)",
                    "Elanus2 UD285C(SGYY)",
                    "Elanus2 UD285M(SGYY)",
                    "Jelly3 MU3C500C(MRYYO)",
                    "Jelly3 MU3C500M(MRYYO)",
                    "Jelly3 MU3C1000C(MRYYO)",
                    "Jelly3 MU3C1400C(MRYYO)",
                    "Jelly3 MU3V130C(CGYYO)",
                    "Jelly3 MU3V130M(CGYYO)",
                    "Jelly3 MU3E130C(EGYYO)",
                    "Jelly3 MU3E130M(EGYYO)",
                    "Jelly1 MUC36C(MGYFO)",
                    "Jelly1 MUC36M(MGYFO)",
                    "Jelly3 MU3C120C(MGYYO)",
                    "Jelly3 MU3C120M(MGYYO)",
                    "Jelly3 MU3E200C(EGYYO)",
                    "Jelly3 MU3E200M(EGYYO)",
                    "Jelly1 MUC130C(MRYNO)",
                    "Jelly1 MUC130M(MRYNO)",
                    "Jelly1 MUC320C(MRYNO)",
                    "Gauss3 U3C130C(MRYNO)",
                    "Gauss3 U3C130M(MRYNO)",
                    "Gauss3 U3C320C(MRYNO)",
                    "Gauss3 U3C500C(MRYNO)",
                    "Gauss3 U3C500M(MRYNO)",
                    "xxxxxx MU3C1401C(MRYYO)",
                    "xxxxxx MU3C1001C(MRYYO)",
                    "xxxxxx MUC131M(MRYN)",
                    "xxxxxx MU3C501C(MRYYO)",
                    "xxxxxx MU3C501M(MRYYO)",
                    "Jelly2 MUC120C(MGYYO)",
                    "Jelly2 MUC120M(MGYYO)",
                    "xxxxxx MU3E131C(EGYY)",
                    "xxxxxx MU3E131M(EGYY)",
                    "xxxxxx MU3E201C(EGYYO)",
                    "xxxxxx MU3E201M(EGYYO)",
                    "Missing Device",
                    "Jelly3 MU3S230C(SGYYO)",
                    "Jelly3 MU3S230M(SGYYO)",
                    "Jelly3 MU3S640C(SRYYO)",
                    "Jelly3 MU3S640M(SRYYO)",
                    "Cooler CUD285C(SGYYO)",
                    "Cooler CUD285M(SGYYO)",
                    "Jelly3 MU3S231C(SGYYO)",
                    "Jelly3 MU3S231M(SGYYO)",
                    "Jelly3 MU3S500C(SGYYO)",
                    "Jelly3 MU3S500M(SGYYO)",
                    "Jelly3 MU3S1200C(SRYYO)",
                    "Jelly3 MU3S1200M(SRYYO)",
                    "Jelly4 MU3L2K7C(AGYYO)",
                    "Jelly4 MU3L2K7M(AGYYO)",
                    "Jelly4 MU3L4K3C(AGYYO)",
                    "Jelly4 MU3L4K3M(AGYYO)",
                    "Jelly6 MU3HS2000C(SRYYO)",
                    "Jelly6 MU3HS2000M(SRYYO)",
                    "Jelly6 MU3HS500C(SGYYO)",
                    "Jelly6 MU3HS500M(SGYYO)",
                    "Jelly6 MU3HS230C(SGYYO)",
                    "Jelly6 MU3HS230M(SGYYO)",
                    "Jelly6 MU3HI400C(IGYYO)",
                    "Jelly6 MU3HI400M(IGYYO)",
                    "TEST CAMERA",
                    "Jelly6 MU3HI401C(IGYYO)",
                    "Jelly6 MU3HI401M(IGYYO)",
                    "Jelly6 MU3S40C(SGYYO)",
                    "Jelly6 MU3S40M(SGYYO)",
                    "Jelly6 MU3S210C(SRYYO)",
                    "Jelly6 MU3S210M(SRYYO)",
                    "Jelly6 MU3I130C(IGYYO)",
                    "Jelly6 MU3I130M(IGYYO)",
                    "Jelly6 MU3S321C(SGYYO)",
                    "Jelly6 MU3S321M(SGYYO)",
                    "Jelly3 MU3S641M(SRYYO)",
                    "Jelly3 MU3S1201M(SRYYO)",
                    "Jelly6 MU3HS2001M(SRYYO)",
                    "Jelly3 MU3S211M(SRYYO)",
                    "Jelly3 MU3C36C(MGYY)",
                    "Jelly3 MU3C36M(MGYY)",
                    "Jelly6 MU3HS1200C(SRYYO)",
                    "Jelly6 MU3HS1200M(SRYYO)",
                    "UNKOWN TYPE",
                    "UNKOWN TYPE",
                    "UNKOWN TYPE",
                    "UNKOWN TYPE",
                    "UNKOWN TYPE",
                    "UNKOWN TYPE",
            };
}
