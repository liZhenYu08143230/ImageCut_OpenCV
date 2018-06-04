package com.example.lzy01.imagecut_opencv;


import android.graphics.Bitmap;

public class NMS {
    /**
     * 不使用插值的NMS。
     * @param gradImage 梯度图像
     * @param theta 梯度角度
     * @return
     */
    public static Bitmap NMSwithoutPowerWeight(Bitmap gradImage, double[][] theta ){
        int xStart=0;
        int yStart=0;
        int width =gradImage.getWidth();
        int height=gradImage.getHeight();
        Bitmap NmsImage=Bitmap.createBitmap(gradImage);

        int rgb1=0,rgb2=0;
        for(int x=xStart;x<width;x++){
            for(int y=yStart;y<height;y++){
                //垂直边缘（theta与边缘正交）
                if(theta[x][y]>=-22.5&&theta[x][y]<22.5||theta[x][y]>=157.5&&theta[x][y]<202.5){
                    if(y-1>=yStart){
                        rgb1=gradImage.getPixel(x,y-1);
                    }else {
                        rgb1=0;
                    }
                    if(y+1<height){
                        rgb2=gradImage.getPixel(x,y+1);
                    }else {
                        rgb2=0;
                    }

                }//-45边缘
                else if(theta[x][y]>=22.5&&theta[x][y]<67.5||theta[x][y]>=202.5&&theta[x][y]<247.5){
                    if(y-1>=yStart&&x-1>=xStart){
                        rgb1=gradImage.getPixel(x-1,y-1);
                    }else {
                        rgb1=0;
                    }
                    if(y+1<height&&x+1<width){
                        rgb2=gradImage.getPixel(x+1,y+1);
                    }else {
                        rgb2=0;
                    }
                }//水平边缘
                else if(theta[x][y]>=67.5&&theta[x][y]<112.5||theta[x][y]>=247.5&&theta[x][y]<292.5){
                    if(x-1>=xStart){
                        rgb1=gradImage.getPixel(x-1,y);
                    }else {
                        rgb1=0;
                    }
                    if(x+1<width){
                        rgb2=gradImage.getPixel(x+1,y);
                    }else {
                        rgb2=0;
                    }
                }//+45边缘
                else if(theta[x][y]>=112.5&&theta[x][y]<157.5||theta[x][y]>=292.5&&theta[x][y]<337.5){
                    if(y-1>=yStart&&x+1<width){
                        rgb1=gradImage.getPixel(x+1,y-1);
                    }else {
                        rgb1=0;
                    }
                    if(y+1<height&&x-1>=xStart){
                        rgb2=gradImage.getPixel(x-1,y+1);
                    }else {
                        rgb2=0;
                    }
                }
                if(gradImage.getPixel(x,y)>rgb1&&gradImage.getPixel(x,y)>rgb2){
                    NmsImage.setPixel(x,y,gradImage.getPixel(x,y));
                }else {
                    NmsImage.setPixel(x,y,0);
                }
            }
        }
        return NmsImage;
    }

    /**
     * 插值的NMS。
     * @param gradImageData 梯度图像数据
     * @param NmsImageData NMS图像数据
     * @param theta 梯度角度数据
     */
    public static void NMSwithPowerWeight(double[][] gradImageData,double[][] NmsImageData,double[][] theta){
        int xStart=0;
        int yStart=0;
        int width =NmsImageData.length;
        int height=NmsImageData[0].length;
        double grad1,grad2,grad3,grad4,gradXY;
        double weight,gradtemp1,gradtemp2;
        double thetaXY;
        for(int x=xStart;x<width;x++){
            for(int y=yStart;y<height;y++){
                thetaXY=theta[x][y];
                gradXY=gradImageData[x][y];
                weight=0.0;
                int xSub1,xAdd1,ySub1,yAdd1;
                xSub1=(x-1)<xStart?xStart:(x-1);
                xAdd1=(x+1)>=width?(width-1):(x+1);
                ySub1=(y-1)<yStart?yStart:(y-1);
                yAdd1=(y+1)>=height?(height-1):(y+1);

                int x1,y1,x2,y2,x3,y3,x4,y4;
                x1=y1=x2=y2=x3=y3=x4=y4=0;
                /** 1
                 *      g1(x-1,y-1)  g2(x-1,y)
                 *                    C(x,y)
                 *                   g4(x+1,y)  g3(x+1,y+1)
                 */
                if(thetaXY>=90&&thetaXY<135||thetaXY>=270&&thetaXY<315){
                    x1=xSub1;
                    y1=ySub1;

                    x2=xSub1;
                    y2=y;

                    x3=xAdd1;
                    y3=y;

                    x4=xAdd1;
                    y4=yAdd1;

                    weight=1/Math.tan(thetaXY);
                }
                /** 2
                 *                      g2(x-1,y)       g1(x-1,y+1)
                 *                      C(x,y)
                 *     g3(x+1,y-1)      g4(x+1,y)
                 */
                else if(thetaXY>=45&&thetaXY<90||thetaXY>=225&&thetaXY<270){
                    x1=xSub1;
                    y1=yAdd1;

                    x2=xSub1;
                    y2=y;

                    x3=xAdd1;
                    y3=ySub1;

                    x4=xAdd1;
                    y4=y ;

                    weight=1/Math.tan(thetaXY);
                }
                /** 3
                 *      g1(x-1,y-1)
                 *      g2(x,y-1)   C(x,y)  g4(x,y+1)
                 *                          g3(x+1,y+1)
                 */
                else if(thetaXY>=135&&thetaXY<180||thetaXY>=315&&thetaXY<360){
                    x1=xSub1;
                    y1=ySub1;

                    x2=x;
                    y2=ySub1;

                    x3=x;
                    y3=yAdd1;

                    x4=xAdd1;
                    y4=yAdd1;

                    weight=Math.tan(thetaXY);
                }
                /** 4
                 *                           g3(x-1,y+1)
                 *      g2(x,y-1)   C(x,y)   g4(x,y+1)
                 *      g1(x+1,y-1)
                 */
                else if(thetaXY>=0&&thetaXY<45||thetaXY>=180&&thetaXY<225){
                    x1=xAdd1;
                    y1=ySub1;

                    x2=x ;
                    y2=ySub1;

                    x3=xSub1;
                    y3=yAdd1;

                    x4=x;
                    y4=yAdd1;

                    weight=Math.tan(thetaXY);
                }
                grad1=gradImageData[x1][y1];
                grad2=gradImageData[x2][y2];
                grad3=gradImageData[x3][y3];
                grad4=gradImageData[x4][y4];

                gradtemp1=grad1*weight+grad2*(1-weight);
                gradtemp2=grad3*weight+grad4*(1-weight);
                if(gradXY>=gradtemp1&&gradXY>=gradtemp2){
                    /*if(gradImage.getRGB(x,y)!=0){
                        NmsImage.setRGB(x,y,0xffffffff);
                    }else{
                        NmsImage.setRGB(x,y,0);
                    }*/
                    NmsImageData[x][y]=gradImageData[x][y];
                }else {
                    NmsImageData[x][y]=0;
                }
            }
        }
    }
}
