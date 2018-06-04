package com.example.lzy01.imagecut_opencv;

import android.graphics.Bitmap;


public class Canny {


    /**
     * 生成Canny图像
     * @param sigam 高斯核的sigam
     * @param guiYiMode 计算高斯核归一化的方式
     * @param dimension 高斯核的维度
     * @param srcImage
     */
    public Bitmap CannyPicture(double sigam, int guiYiMode, int dimension, Bitmap srcImage){

        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        double theta[][];
        double[][] grayImageData=new double[width][height];
        double[][] gsImage1dData=new double[width][height];
        double[][] gradBYsobleData=new double[width][height];
        double[][] NMSImageData=new double[width][height];
        Bitmap resultCanny=Bitmap.createBitmap(srcImage);

        long startTime,endTime;
        //转化为灰度图
        startTime=System.currentTimeMillis();
        Gray.toGray(srcImage,grayImageData);
        endTime=System.currentTimeMillis();
        System.out.println("Running Time Gray="+(endTime-startTime)+"ms");


        Gaussian gaussian=new Gaussian(sigam,guiYiMode,dimension,grayImageData,gsImage1dData );

        //高斯模糊 1D
        startTime=System.currentTimeMillis();
        gaussian.gaussianPicture();
        endTime=System.currentTimeMillis();
        System.out.println("Running Time Gaussian 1d="+(endTime-startTime)+"ms");

        //梯度图像
        startTime=System.currentTimeMillis();
        theta=Grad.gradPictureSobel(gsImage1dData,gradBYsobleData);
        endTime=System.currentTimeMillis();
        System.out.println("Running Time Grad="+(endTime-startTime)+"ms");

        //非最大抑制
        startTime=System.currentTimeMillis();
        NMS.NMSwithPowerWeight(gradBYsobleData,NMSImageData,theta);
        endTime=System.currentTimeMillis();
        System.out.println("Running Time NMS="+(endTime-startTime)+"ms");

        //双阙值处理
        startTime=System.currentTimeMillis();
        resultCanny=Threshold.doubleThreshold(NMSImageData,srcImage);
        endTime=System.currentTimeMillis();
        System.out.println("Running Time Threshold="+(endTime-startTime)+"ms");

        return resultCanny;
    }
}
