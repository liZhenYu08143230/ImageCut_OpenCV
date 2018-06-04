package com.example.lzy01.imagecut_opencv;


import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Threshold {
    /**
     * 保留大于（原图像最大灰度的percentNUM %）的像素
     * @param image 原图像
     * @param percentNUM
     */
    public static void thresholdProcessing(Bitmap image, int percentNUM){
        int xStart=0;
        int yStart=0;
        int width = image.getWidth();
        int height = image.getHeight();
        int max=0;
        for(int x = xStart; x < width ; x ++) {
            for(int y = yStart; y < height; y++) {
                if(ImageBaseOp.getRgbArrary(image,x,y)[0]>max){
                    max= ImageBaseOp.getRgbArrary(image,x,y)[0];
                }
            }
        }
        int target=max*percentNUM/100;
        for(int x = xStart; x < width ; x ++) {
            for(int y = yStart; y < height; y++) {
                if(ImageBaseOp.getRgbArrary(image,x,y)[0]<=target){
                    image.setPixel(x,y,0);
                }
            }
        }
    }


    public static Bitmap doubleThreshold(double[][] NMSimageData, Bitmap srcBitmap){
        int xStart=0;
        int yStart=0;
        int width = NMSimageData.length;
        int height = NMSimageData[0].length;
        int Hist[]=getHist(NMSimageData);
        int threshold[];
        int[][] hightTimage=new int[width][height];
        int[][] lowTimage=new int[width][height];
        int[][] weakEdge=new int[width][height];
        int[][] outImage=new int[width][height];
        threshold=getThreshold(Hist);

        Queue <Point>pointList=new LinkedList<>();
        Stack <Point>pointStack=new Stack<>();
        boolean connected;

        int X8[]={-1,-1,-1,0,0,0,+1,+1,+1};
        int Y8[]={-1,0,+1,-1,0,+1,-1,0,+1};

        boolean isXYCheck[][]=new boolean[NMSimageData.length][NMSimageData[0].length];
        for(int x=0;x<width;x++) {
            for (int y = 0; y < height; y++) {
                isXYCheck[x][y]=false;
            }
        }
        for(int x=xStart;x<width;x++){
            for(int y=yStart;y<height;y++){
                if(NMSimageData[x][y] > threshold[1]){
                    hightTimage[x][y]=0xff;
                    outImage[x][y]=0xff;
                }else{
                    hightTimage[x][y]=0x00;
                    outImage[x][y]=0x00;
                }
                if (NMSimageData[x][y]> threshold[0]){
                    lowTimage[x][y]=0xff;
                }else {
                    lowTimage[x][y]=0x00;
                }
                weakEdge[x][y]=lowTimage[x][y]-hightTimage[x][y];
            }
        }
        for(int x=xStart;x<width;x++){
            for (int y = yStart; y < height; y++){
                connected=false;
                if(weakEdge[x][y]==0xff && !isXYCheck[x][y]){
                    isXYCheck[x][y]=true;
                    Point temp1=new Point(x,y);
                    pointStack.push(temp1);
                    pointList.offer(temp1);
                    while(!pointStack.empty()){
                        Point temp=pointStack.pop();
                        for(int i=0;i<8;i++){
                            int x1=temp.getX()+X8[i];
                            int y1=temp.getY()+Y8[i];
                            x1=(x1<xStart?xStart:x1);
                            x1=(x1>=width?(width-1):x1);
                            y1=(y1<yStart?yStart:y1);
                            y1=(y1>=height?(height-1):y1);
                            if(weakEdge[x1][y1]==0xff&&!isXYCheck[x1][y1]){
                                Point temp2=new Point(x1,y1);
                                pointStack.push(temp2);
                                pointList.offer(temp2);
                                isXYCheck[x1][y1]=true;
                            }
                            if(hightTimage[x1][y1]==0xff){
                                connected=true;
                            }
                        }
                    }
                    if(connected){
                        while (!pointList.isEmpty()){
                            Point point=pointList.poll();
                            outImage[point.getX()][point.getY()]=0xff;
                            weakEdge[point.getX()][point.getY()]=0x00;
                        }
                    } else{
                        while (!pointList.isEmpty()){
                            Point point=pointList.poll();
                            isXYCheck[point.getX()][point.getY()]=false;
                        }
                    }
                }
            }
        }
        Bitmap result=Bitmap.createBitmap(srcBitmap);
        for(int x=xStart;x<width;x++) {
            for (int y = yStart; y < height; y++) {
                int t=(int)hightTimage[x][y];
                int temp[]={t,t,t};
                ImageBaseOp.setRGB(result,x,y,temp);
            }
        }
        return result;
    }

    private static int[] getThreshold(int[] hist) {
        int [] threshold=new int[2];
        int maxGrad=getMaxGrad(hist);
        int edgeNum=getEdgeNum(hist);

        double  dRatHigh = 0.8;
        double  dRatLow = 0.5;
        int HighCount = (int)(dRatHigh * edgeNum+0.5);
        int j=0;
        int edgeNum1 = hist[0];
        while((j<(maxGrad-1)) && (edgeNum1 <  HighCount))
        {
            j++;
            edgeNum1 += hist[j];
        }
        threshold[1] = j;                                   //高阈值
        threshold[0] = (int)((threshold[1]) * dRatLow +0.5);
        System.out.println(threshold[0]+" "+threshold[1]+" "+HighCount);
        return  threshold;
    }

    private static int getEdgeNum(int[] hist) {
        int EdgeNum=hist[0];
        for(int i=0;i<hist.length;i++){
            EdgeNum+=hist[i];
        }
        return EdgeNum;
    }

    private static int getMaxGrad(int[] hist) {
        int max=0;
        for(int i=0;i<hist.length;i++){
            if(hist[i]!=0){
                max=i;
            }
        }
        return max;
    }

    private static int[] getHist(double[][] NmsImageData){
        int []hist=new int[3000];
        int xStart=0;
        int yStart=0;
        int width = NmsImageData.length;
        int height = NmsImageData[0].length;
        for(int i=0;i<hist.length;i++)
            hist[i] = 0;
        for(int x=xStart; x<width; x++) {
            for(int y=yStart; y<height; y++)
            {
                if(NmsImageData[x][y]!=0)//???????????????????????????
                    hist[(int)NmsImageData[x][y]]++;
            }
        }
        return hist;
    }
}

class Point{
    private int x;
    private int y;
    public Point(int x,int y){
        this.x=x;
        this.y=y;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
}
