package com.catchbest;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.catchebstnew.www.mvCameraCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by terry on 17-9-14.
 */



public class cam {

    class OpenedDev {
        private UsbDevice m_Device;
        private UsbDeviceConnection m_Connection;

        public OpenedDev(UsbDevice device) {
            m_Device = device;
        }

        public boolean Open() {
            UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            m_Connection = manager.openDevice(m_Device);
            return m_Connection != null;
        }

        public void Close() {
            if (m_Connection != null) {
                m_Connection.close();
                m_Connection = null;
            }
            m_Device = null;
        }

        public short GetVID() { return (short)m_Device.getVendorId(); }
        public short GetPID() { return (short)m_Device.getProductId(); }
        public int GetFd() {
            return m_Connection.getFileDescriptor();
        }
        public String GetPath() { return m_Device.getDeviceName(); }
    };

    private Context mContext;
    private mvCameraCallback mEnumerateCallback;
    private List<UsbDevice> mDeviceList;
    private List<OpenedDev> mOpenedDevList;
    private final String ACTION_USB_PERMISSION = "com.catchbest.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e("TAG", "====== 0005 ====== : " + action + "\n" );//输出相机信息

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    boolean ok = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

                    if (mDeviceList.contains(device)) {
                        mDeviceList.remove(device);

                        if (ok) {
                            OpenedDev dev = new OpenedDev(device);
                            if (dev.Open()) {
                                mOpenedDevList.add(dev);
                            }
                        }

                        if (!mDeviceList.isEmpty()) {
                            Log.e("TAG", "====== 0001 ====== :\n" );//输出相机信息
                            RequestUsbPermission(mDeviceList.get(0));
                        } else {
                            Log.e("TAG", "====== 0002 ====== :\n" );//输出相机信息
                            EnumerateDeviceDone();
                        }
                    }
                }
            }
        }
    };

    // 无需root
    public boolean CameraEnumerateDeviceEx2(mvCameraCallback EnumerateCallback)
    {
        if (mEnumerateCallback != null)
            return false;
        mEnumerateCallback = EnumerateCallback;

        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        assert (mOpenedDevList == null);
        mOpenedDevList = new ArrayList<OpenedDev>();
        mDeviceList = new ArrayList<UsbDevice>();

        Log.e("TAG", "====== USB设备设备信息 +++++++ ====== :\n" );//输出相机信息

        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            int vid = device.getVendorId();
            int pid = device.getProductId();

            String content = "vid: " + vid + "  pid: " + pid;
            Log.e("TAG", content);//输出相机信息

            if (vid == 0x0816) {
                //if (!manager.hasPermission(device) ) {
                Log.e("TAG", "hasPermission failed, add to mDeviceList");//输出相机信息
                mDeviceList.add(device);
                //}
                //else {
                //    Log.e("TAG", "hasPermission is successed, open it" );//输出相机信息
                //    OpenedDev dev = new OpenedDev(device);
                //    if (dev.Open() ) {
                //        Log.e("TAG", "hasPermission is successed, add to mOpenedDevList" );//输出相机信息
                //        mOpenedDevList.add(dev);
                //    }
                //}
            }
        }

        if (!mDeviceList.isEmpty()) {
            RequestUsbPermission(mDeviceList.get(0));
        } else {
            EnumerateDeviceDone();
        }

        return true;
    }

    public boolean RealseAllDevices() {
        if (mOpenedDevList != null) {
            for (OpenedDev dev : mOpenedDevList) {
                dev.Close();
            }
        }

        mOpenedDevList = null;

        UnInit();
        ClearAllUsbHandle();

        return true;
    }

    private void EnumerateDeviceDone()
    {
        assert (mDeviceList.isEmpty());
        assert (mEnumerateCallback != null);

        int nDev = mOpenedDevList.size();

        if (nDev > 0) {
            int[] fds = new int[nDev];
            int[] pids = new int[nDev];
            String[] paths = new String[nDev];

            int i = 0;
            for (OpenedDev dev: mOpenedDevList) {
                fds[i] = dev.GetFd();
                pids[i] = (dev.GetVID() << 16) | dev.GetPID();
                paths[i] = dev.GetPath();

                Log.e("TAG", "====== USB >>>>>>>>>>>> ====== >>>>>>>>>>>>>>>>>> : " + fds[i] + "\n");//输出相机信息

                PreInit(fds[i]);

                Init();

                int count = DeviceGetCount();

                //获取相机信息
                int[] deviceTypeArray = new int[1];
                int[] serialsArray = new int[1];
                int[] firmwareVersionArray = new int[1];
                int nn = DeviceGetInformation(i, deviceTypeArray, serialsArray, firmwareVersionArray);
                String content = "相机型号：" + deviceTypeArray[0] + "\n序列号：" + serialsArray[0] + "\n版本：" + firmwareVersionArray[0];
                int isBlackWhite = QueryFunction(i, 0);//判断黑白、彩色相机 返回值1：黑白相机 0：彩色相机
                if (isBlackWhite == 1) {
                    Log.e("TAG", "====== 相机信息 ====== :\n" + content + "\n黑白相机");//输出相机信息
                } else {
                    Log.e("TAG", "====== 相机信息 ====== :\n" + content + "\n彩色相机");//输出相机信息
                }

                Log.e("TAG", "====== USB设备设备信息 +++++++ ====== :\n" );//输出相机信息

                ++i;
            }
        }

        mEnumerateCallback.onEnumerateDeviceCompleted(nDev);
        mEnumerateCallback = null;
    }

    private boolean RequestUsbPermission(UsbDevice device) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        mContext.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        manager.requestPermission(device, mPermissionIntent);
        return true;
    }

    public int m_devicecount;

    public cam(Context context)
    {
        mContext = context;
        m_devicecount = 1;
    }

    public native int EnableLog(int bEnable);

    public native int Init();

    public native int CaptureRawData(int index, Object buffer);

    public native int CaptureRGBData(int index, Object buffer);

    public native int DeviceGetCount();

    public native int CaptureGetSize(int index, int[] width, int[] height);

    public native int DeviceGetInformation(int index, int[] DeviceType, int[] Serials,int [] FirmwareVersion);

    public native int CaptureSetFieldOfView(int nIndex, int nxStart, int nyStart, int nWidth, int nHeight);

    public native int GetTriggerMode(int index);

    public native int SetTriggerMode(int index, int mode);

    public native int SetBayerMode(int index, int mode);

    public native int SoftStartCapture(int index);

    public native int ReadRawData(int index, Object buffer);

    public native int ReadRGBData(int index, Object buffer);

    public native byte[] CaptureRGBdataArray(int index, int width, int height);

    public native byte[] CaptureRAWdataArray(int index, int width, int height);
    public native int[] CaptureRAWImageArray(int index, int width, int height);


    public native int[] CaptureRGBdataIntArray(int index, int width, int height);


    public native int[] CaptureRGBdataIntArrayAfterStart(int index, int width, int height);


    public native int ExposureTimeSet(int index, int time);

    public native int WhiteBalanceSet(int index, int mode);

    public native int CaptureBySurface(int index, Object surface, int save);

    public native int  CaptureBitmap(int index, String fullpath);

    public native int SetParam(int index,int param,int value);
    public native int GetParam(int index,int param,int[] value);

    public native int PreInit(int fd);

    public native int PreInitAddUsbHandle(int fd);
    public native int DeleteUsbHandle(int fd);
    public native int ClearAllUsbHandle();

    public native int CaptureGetFieldOfView(int index,int[] nxStart, int[] nyStart ,int[] nWidth, int[] nHeight);

    public native int GetParamRange(int index,int param,int[] min, int[] max);

    public native int CaptureBySurfaceSave(int index, Object surface, int save, String fullpath);

    public native int CaptureSetRecover(int index,int value);

    public native int CaptureGetSizeEx(int index,int[] nWidth, int[] nHeight, int[] nBitscount);

    public native int LutSetEnable(int index,int value);
    public native int LutGetEnable(int index,int enable[]);

    public native int SensitivitySetMode(int index,int value);
    public native int SensitivityGetMode(int index,int value[]);

    public native int QueryFunction(int index,int function);

    public native int AESetExposureTimeRange(int index, int nMinExpMs, int nMaxExpMs);

    public native int AEStart(int index,int nStart, int nMaxCount,int nTarget);

    public native int WrEEPROM(int index, int address, int value);
    public native int RdEEPROM(int index, int address, int[] value);

    public native int WrEEPROMEx(int index, int address, int value);
    public native int RdEEPROMEx(int index, int address, int[] value);

    public native int WrEEPROMExEx(int index, int address, int bytes, Object buffer);
    public native byte[] RdEEPROMExEx(int index, int address, int bytes);

    public native int  UnInit();


    static {
        System.loadLibrary("usb1.0");
        System.loadLibrary("ksjapijni");
    }

}
