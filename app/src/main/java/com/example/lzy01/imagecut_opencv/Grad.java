package com.example.lzy01.imagecut_opencv;

import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


public class Grad {

    private static int sobelX[][]={{-1,0,1},{-2,0,2},{-1,0,1}};
    private static int sobelY[][]={{-1,-2,-1},{0,0,0},{1,2,1}};
    private static double theta[][];
    private static double gx[][];
    private static double gy[][];
    /**
     * 使用Sobel算子产生梯度图像所需的Gx，Gy图像
     * @param srcImageData 待处理的图片
     * @param outImageData 处理完成的图片
     * @return
     */
    public static double[][] gradPictureSobel(double[][] srcImageData, double outImageData[][]){
        int width = srcImageData.length;
        int height = srcImageData[0].length;

        //soble 算子的归一化系数
        int modulusOfNormalization=1;


        gx=new double [width][height];
        gy=new double [width][height];
        theta=new double[width][height];

        ImageBaseOp.Convolution(srcImageData,gx,sobelX,modulusOfNormalization);
        ImageBaseOp.Convolution(srcImageData,gy,sobelY,modulusOfNormalization);


        gradPicture(outImageData);

        angleArrary();
        return theta;
    }

    /**
     * 利用Gx和Gy计算图像的梯度幅值
     * @param outImageData 结果图像的数据
     */
    private static void gradPicture( double[][] outImageData) {
        int width=gx.length;
        int height=gx[0].length;
        double rgb;
        for(int x=0;x<width;x++){
            for(int y=0;y<height;y++){
//                M(x,y)=sqrt(Gx^2+Gy^2)
                outImageData[x][y]= sqrt(pow(gx[x][y],2)+pow(gy[x][y],2));
//                M(x,y)=|Gx|+|Gy|
//                outImageData[x][y]=abs(gx[x][y])+abs(gy[x][y]);
            }
        }
    }

    /**
     * 梯度角度的计算
     */
    private static void angleArrary(){
        int width = gx.length;
        int height = gx[0].length;
        double temp;
        for(int x=0;x<width;x++){
            for(int y=0;y<height;y++){
                temp = (atan2(gy[x][y],gx[x][y])*(180/Math.PI));
                temp = temp>=0?temp:temp+360;
                theta[x][y]=temp;
            }
        }
    }
}
