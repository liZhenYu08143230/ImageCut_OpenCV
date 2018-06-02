package com.example.lzy01.imagecut_opencv;

public class JniFunction {
    public static native void DoCanny(Object srcBitmap);
    public static native void DoGrabCut(Object srcBitmap,int [] Mask);
}
