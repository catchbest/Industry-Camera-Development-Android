package com.catchebstnew.www;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.catchbest.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    private Button btnStart;
    private Button btnStart2;
    private Button btnRK3399;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        btnStart = findViewById(R.id.btn_start);
        btnStart2 = findViewById(R.id.btn_start2);
        btnRK3399 = findViewById(R.id.btn_rk3399);

        btnStart.setOnClickListener(this);
        btnStart2.setOnClickListener(this);
        btnRK3399.setOnClickListener(this);
    }

    private void init() {
        if (!checkAccess()) {//判断Android设备是否支持访问dev/bus/usb 权限
            if (checkSuFile()) {//针对手机和平板设备，判断系统是否root
                upgradeRootPermission("chmod -R 777 /dev/bus/usb/");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("系统未root，功能不能使用");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        } else {
            upgradeRootPermission("chmod -R 777 /dev/bus/usb/");
        }
    }

    /**
     * 更改 /dev/bus/usb/ 权限为777
     *
     * @param cmd
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

    /**
     * 检查是否是存在su文件，存在说明已经root
     * 这种方法存在误测和漏测的情况，一种情况是手机没有root但是存在su文件，这种情况一般出现在手机曾经被root过，但是又进行了系统还原操作
     *
     * @return
     */
    private static boolean checkSuFile() {
        Process process = null;
        try {
            //   /system/xbin/which 或者  /system/bin/which
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    /**
     * 判断Android设备否可以访问adb ls dev/bus/usb
     * @return
     */
    private boolean checkAccess() {
        Runtime mRuntime = Runtime.getRuntime();
        try {
            Process mProcess = mRuntime.exec("ls /dev/bus/usb/ -al ");
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            Log.e(TAG, "msg:" + mRespBuff.toString());
//            Toast.makeText(MainActivity.this, "msg:" + mRespBuff.toString(), Toast.LENGTH_SHORT).show();
            if(mRespBuff != null) {
                if(mRespBuff.toString().contains("daemon not running")) {
                    Log.e(TAG,"没有访问dev/bus/usb/ 的权限");
                    return false;
                } else {
                    Log.e(TAG,"有访问dev/bus/usb/ 的权限");
                    return true;
                }
            } else {
                Log.e(TAG,"没有访问dev/bus/usb/ 的权限");
                return false;
            }
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startActivity(new Intent(this, SurfaceViewActivity.class));
                break;

            case R.id.btn_start2:
                startActivity(new Intent(this, ImageViewActivity.class));
                break;

            case R.id.btn_rk3399:
                startActivity(new Intent(this,Rk3399_641M_Activity.class));
                break;
        }
    }
}
