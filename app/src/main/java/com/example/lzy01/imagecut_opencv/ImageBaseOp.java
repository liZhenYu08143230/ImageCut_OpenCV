package com.example.lzy01.imagecut_opencv;

import android.graphics.Bitmap;

import static java.lang.Math.abs;


public class ImageBaseOp {


    /**
     *将图像（x，y）的RGB值保存至数组中
     * @param image 需要获取的图片
     * @param x 获取图片RGB的位置的x坐标
     * @param y 获取图片RGB的位置的y坐标
     * @return 返回的RGB数组[0]-r,[1]-g,[2]-b
     */
    public static int[] getRgbArrary(Bitmap image, int x, int y) {
        int rgb[]=new int[3];
        int pixel = image.getPixel(x, y);
        rgb[0]=(pixel & 0xff0000 ) >> 16;
        rgb[1] = (pixel & 0xff00) >> 8;
        rgb[2]= pixel & 0xff;
        return rgb;
    }

    /**
     * 设置srcImage的像素，RGB的数据分别保存
     * @param Image 需要设置RGB值的对象
     * @param x 图片修改位置的x坐标
     * @param y 图片修改位置的y上坐标
     * @param  rgb 修改的rgb数组
     */
    public static void setRGB(Bitmap Image, int x, int y, int[] rgb){
        int pixel;
        for(int i=0;i<3;i++){
            if(rgb[i]>255){
                rgb[i]=rgb[i]%255;
            }else if(rgb[i]<0){
                rgb[i]=abs(rgb[i]);
            }
        }
        pixel = (rgb[0] << 16) & 0x00ff0000 | (0xff00ffff);
        pixel = (rgb[1]  << 8) & 0x0000ff00 | (pixel & 0xffff00ff);
        pixel = (rgb[2] ) & 0x000000ff | (pixel & 0xffffff00);
        Image.setPixel(x, y, pixel);
    }

    /**
     * 对图像进行二维卷积，每个像素对应的结果保存在outArrary中。
     * @param srcImageData 原图像
     * @param outArrary 卷积产生的结果，二维数组。是图像三通道里的其中之一
     * @param Convolution 二维卷积核 int[][]
     * @param modulusOfNormalization 卷积核的归一化系数
     */
    public static void  Convolution(double[][] srcImageData, double outArrary[][], int Convolution[][], int modulusOfNormalization){
        int xStart=0;
        int yStart=0;
        int width = srcImageData.length;
        int height = srcImageData[0].length;
        int border;
        border=(Convolution.length-1)/2;
        double outRGB;
        for(int x = xStart; x < width ; x ++) {
            for(int y = yStart; y < height; y++) {
                outRGB = doKenel(x,y,border,Convolution,srcImageData,xStart,width,yStart,height);
                try{
                    outRGB/=modulusOfNormalization;
                }catch (Exception e){
                    e.printStackTrace();
                }
                outArrary[x][y]=outRGB;
            }//遍历y
        }//遍历x
    }
    /**
     * 使用给定的算子对图像进行两次一维卷积
     * @param srcImageData 原图像
     * @param outImageData 卷积产生的图像
     * @param Convolution 一维卷积核 int[]
     * @param modulusOfNormalization 卷积核的归一化系数
     */
    public static void Convolution(double[][] srcImageData,double[][] outImageData,int Convolution[],int modulusOfNormalization){
        int xStart=0;
        int yStart=0;
        int width = srcImageData.length;
        int height = srcImageData[0].length;

        int border,center;
        border=center=(Convolution.length-1)/2;

        int outRGB;
        int timeXY[][]={{xStart,width},{yStart,height}};
        double [][] outImage_mid= new double[width][height];

        for(int time=0;time<2;time++){//两次卷积

            for(int firstXY=timeXY[time][0]; firstXY<timeXY[time][1]; firstXY++){
                int secondStart=timeXY[time==0?1:0][0];
                int secondEnd=timeXY[time==0?1:0][1];

                for(int secondXY=secondStart; secondXY<secondEnd; secondXY++){
                    int x=(time==0?firstXY:secondXY);
                    int y=(time==1?firstXY:secondXY);
                    outRGB=0;
                    double[][] srcImg_time=(time==0?srcImageData:outImage_mid);
                    double[][] outImg_time=(time==0?outImage_mid:outImageData);

                    for(int i=-border;i<=border;i++){
                        int xy1=secondXY+i;
                        double srcRGB;

                        /*if (xy1<secondStart){
                            xy1=secondStart;
                        }else if(xy1>=secondEnd){
                            xy1=secondEnd-1;
                        }*/
                        if(xy1>=secondStart&&xy1<secondEnd)
                            srcRGB = srcImg_time[time==0?x:xy1][time==1?y:xy1];
                        else
                            srcRGB = 0;
//                        srcRGB = getRgbArrary(srcImg_time,time==0?x:xy1,time==1?y:xy1)[0];
                        //-----------------------------------
                        outRGB+=(Convolution[center+i]*srcRGB);
                        //------------------------------------
                    }
                    /*-----------------*/
                    try{
                        outRGB/=modulusOfNormalization;

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    outImg_time[x][y]=outRGB;
                }
            }
        }
    }

    /**
     * 二维核的使用过程
     * @param x 核中心对应的图像X坐标
     * @param y 核中心对应的图像Y坐标
     * @param border 核半径
     * @param Convolution 核本体
     * @param srcImageData 原图像
     * @param xStart 原图像起始x坐标
     * @param width 原图像宽度（比x坐标的最大值大1）
     * @param yStart 原图像起始y坐标
     * @param height 原图像高度（比y坐标的最大值大1）
     * @return
     */
    private static double doKenel(int x,int y,int border, int[][] Convolution,double[][] srcImageData,int xStart,int width,int yStart,int height) {
        double srcRGB;
        double outRGB=0;
        int center = border;
        for (int i = -border; i <= border; i++) {
            int x1 = x + i;
            /*if(x1<xStart){
                x1=xStart;
            }else if(x1>=width){
                x1=width-1;
            }*/
            for (int j = -border; j <= border; j++) {
                int y1 = y + j;
                /*if(y1<yStart){
                    y1=yStart;
                }else if(y1>=height){
                    y1=height-1;
                }*/
                if(x1>=xStart&&x1<width&&y1>=yStart&&y1<height)
                    srcRGB = srcImageData[x1][y1];
                else
                    srcRGB=0;
                //-----------------------------------
                outRGB+= (Convolution[center + i][center + j] * srcRGB);
                //------------------------------------
            }
        }
        return  outRGB;
    }
}
