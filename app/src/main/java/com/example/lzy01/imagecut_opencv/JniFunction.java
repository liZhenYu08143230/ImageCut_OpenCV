package com.example.lzy01.imagecut_opencv;

import android.graphics.Bitmap;

public class JniFunction {
    public static native Bitmap DoCanny(Bitmap srcBitmap);
    public static native Bitmap DoGrabCut(Bitmap srcBitmap,Bitmap Mask);
}
