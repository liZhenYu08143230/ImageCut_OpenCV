#include "com_example_lzy01_imagecut_opencv_JniFunction.h"
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

using namespace cv;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_lzy01_imagecut_1opencv_JniFunction_DoCanny(JNIEnv *env, jclass type,
                                                            jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;

    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
              info.format == ANDROID_BITMAP_FORMAT_RGB_565);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        Mat temp(info.height, info.width, CV_8UC4, pixels);
        Mat gray;
        cvtColor(temp, gray, COLOR_RGBA2GRAY);
        Canny(gray, gray, 3, 9, 3);
        cvtColor(gray, temp, COLOR_GRAY2RGBA);
    } else {
        Mat temp(info.height, info.width, CV_8UC2, pixels);
        Mat gray;
        cvtColor(temp, gray, COLOR_RGB2GRAY);
        Canny(gray, gray, 3, 9, 3);
        cvtColor(gray, temp, COLOR_GRAY2RGB);
    }
    AndroidBitmap_unlockPixels(env, bitmap);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_lzy01_imagecut_1opencv_JniFunction_DoGrabCut(JNIEnv *env, jclass type,jobject srcBitmap, jintArray Mask) {
    AndroidBitmapInfo infosrc;
    void *pixelsrc;
    jint * receivedIntArrary = env->GetIntArrayElements(Mask, 0);

    CV_Assert(AndroidBitmap_getInfo(env, srcBitmap, &infosrc) >= 0);
    CV_Assert(infosrc.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
              infosrc.format == ANDROID_BITMAP_FORMAT_RGB_565);
    CV_Assert(AndroidBitmap_lockPixels(env, srcBitmap, &pixelsrc) >= 0);
    CV_Assert(pixelsrc);

    Rect rect;
    Mat bgModel, fgModel,result;

    Mat srcC4(infosrc.height, infosrc.width, CV_8UC4, pixelsrc);
    Mat src;
    cvtColor(srcC4,src,COLOR_BGRA2BGR);
    Mat mask(infosrc.height, infosrc.width,CV_8UC1);
    for(int x=0;x<infosrc.height;x++){
        for(int y=0;y<infosrc.width;y++){
            mask.at<uchar>(x,y)=receivedIntArrary[x*infosrc.width+y];
        }
    }
    env->ReleaseIntArrayElements(Mask, receivedIntArrary, 0);

    grabCut(src,mask,rect, fgModel, bgModel,2,GC_INIT_WITH_MASK);
    compare(mask, GC_PR_FGD, mask, CMP_EQ);
    Mat foreground(src.size(),CV_8UC3, Scalar::all(255));
    src.copyTo(foreground,mask);

    cvtColor(foreground,srcC4,COLOR_BGR2BGRA);
    AndroidBitmap_unlockPixels(env, srcBitmap);
}