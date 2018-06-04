package com.example.lzy01.imagecut_opencv;

import android.graphics.Bitmap;


public class Gray {
    /**
     * 彩色转灰度
     * @param srcImage 原图像
     * @param outImage 处理后的图像
     * @return
     */
    public static void toGray(Bitmap srcImage, double[][] outImage) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        int rgb[];
        try {
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width ; x ++) {
                    rgb= ImageBaseOp.getRgbArrary(srcImage,x,y);
                    //加权法的核心,加权法是用图片的亮度作为灰度值的
                    double grayValue = (rgb[0]*0.3 + rgb[1]*0.59 + rgb[2]*0.11);
                    outImage[x][y]=grayValue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}