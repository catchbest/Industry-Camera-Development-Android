package com.catchbest;


/**
 * Created by terry on 17-9-14.
 */



public class cam {

    public int m_devicecount;


    public cam() {

        m_devicecount = 0;
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


    public native int[] CaptureRGBdataIntArray(int index, int width, int height);


    public native int[] CaptureRGBdataIntArrayAfterStart(int index, int width, int height);


    public native int ExposureTimeSet(int index, int time);

    public native int WhiteBalanceSet(int index, int mode);

    public native int CaptureBySurface(int index, Object surface, int save);

    public native int  CaptureBitmap(int index, String fullpath);

    public native int SetParam(int index,int param,int value);

    public native int PreInit(int fd);

    public native int CaptureGetFieldOfView(int index,int[] nxStart, int[] nyStart ,int[] nWidth, int[] nHeight);

    public native int GetParamRange(int index,int param,int[] min, int[] max);

    public native int CaptureBySurfaceSave(int index, Object surface, int save, String fullpath);

    public native int CaptureSetRecover(int index,int value);

    public native int CaptureGetSizeEx(int index,int[] nWidth, int[] nHeight, int[] nBitscount);

    public native int LutSetEnable(int index,int value);

    public native int SensitivitySetMode(int index,int value);

    public native int QueryFunction(int index,int function);

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
