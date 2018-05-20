package com.example.lzy01.imagecut_opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.StrictMath.abs;

public class ActivityGrabCut extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private Button btn_grabCut;
    private Button btn_grabPre;
    private ImageView showImage;
    private TextView info;
    private RadioGroup radioGroup;

    private Canvas canvas;
    private Paint paint;

    private Bitmap srcBitmap;
    private Bitmap grabBitmap;
    private Bitmap drawBitmap;

    private boolean drawRect;
    private boolean haveRect;
    private boolean isGrabCut;
    private boolean isFristDraw;
    private boolean isMove;

    private ConstraintLayout.LayoutParams layout;
    private  int startX=0, startY=0, endX=0, endY=0,tempX=0,tempY=0,mode;
    private int moveX=0,moveY=0;

    private static final String TAG = "ActivityGrabCut";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_cut);

        Init();
    }

    private void Init() {
        btn_grabCut=findViewById(R.id.btn_doGrabCut);
        btn_grabPre=findViewById(R.id.btn_grabPre);
        showImage=findViewById(R.id.showGrabCutImage);
        info=findViewById(R.id.info);
        radioGroup=findViewById(R.id.radioGroup);
        layout=(ConstraintLayout.LayoutParams)showImage.getLayoutParams();

        info.setGravity(Gravity.CENTER);
        btn_grabPre.setOnClickListener(this);
        btn_grabCut.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.btn_redPaint){
                    paint.setColor(Color.RED);
                    Log.d("radioGroup", "onCheckedChanged: red");
                }
                if(checkedId==R.id.btn_bluePaint) {
                    paint.setColor(Color.BLUE);
                    Log.d("radioGroup", "onCheckedChanged: blue");
                }
                if(haveRect){
                    drawRect=true;
                    ChangeView();
                }
            }
        });
        showImage.setOnTouchListener(this);
        info.setText(R.string.draw_rect_info);
        btn_grabCut.setEnabled(false);
        paint=new Paint();
        paint.setColor(Color.WHITE);
        drawRect =false;
        isGrabCut=false;
        haveRect=false;
        ChangeView();
        isFristDraw=true;
        isMove=false;

        Intent intent=getIntent();
        byte[] srcByte=intent.getByteArrayExtra("srcImage");
        srcBitmap= BitmapFactory.decodeByteArray(srcByte, 0, srcByte.length).copy(Bitmap.Config.ARGB_8888, true);
        showImage.setImageBitmap(srcBitmap);
        drawBitmap=Bitmap.createBitmap(srcBitmap);
    }

    private void ChangeView() {
        if(drawRect &&!isGrabCut){
            btn_grabCut.setEnabled(true);
            info.setText(R.string.do_grabCut_info);
        }else if(drawRect &&isGrabCut){
            btn_grabCut.setText(R.string.finish);
            info.setVisibility(View.INVISIBLE);
            radioGroup.setVisibility(View.GONE);
            layout.setMargins(8, 0,8,0);
            showImage.setLayoutParams(layout);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_doGrabCut:
                if(drawRect &&!isGrabCut){
                    //grabBitmap=DoGrabCut(srcBitmap,drawBitmap);
                    //if(grabBitmap!=null){
                        isGrabCut=true;
                        showImage.setImageBitmap(drawBitmap);
                      //  showImage.setImageBitmap(grabBitmap);
                    //}
                    ChangeView();
                }else if(!drawRect){
                    Toast.makeText(getApplicationContext(), "please draw a rect", Toast.LENGTH_SHORT).show();
                }else if(isGrabCut){
                    Intent intent=new Intent(ActivityGrabCut.this,ActivityProcessEnd.class);
                    intent.putExtra("image",MainActivity.Bitmap2Bytes(drawBitmap));
//                    intent.putExtra("CannyImage",MainActivity.Bitmap2Bytes(grabBitmap));
                    startActivity(intent);
                }
                break;
            case R.id.btn_grabPre:
                if(!drawRect&&!haveRect){
                    finish();
                }else{
                    Init();
                    radioGroup.clearCheck();
                }
                break;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try{
            if(!drawRect) {
                int tsubSX=0,tsubSY=0,tsubEX=0,tsubEY=0;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: clickDown");
                        if(isFristDraw){
                            startX = (int) event.getX();
                            startY = (int) event.getY();
                        }else {
                            tempX = (int) event.getX();
                            tempY = (int) event.getY();
                            tsubSX=abs(startX-tempX);
                            tsubSY=abs(startY-tempY);
                            tsubEX=abs(endX-tempX);
                            tsubEY=abs(endY-tempY);
                            if(tsubSX<=5||tsubSY<=5||tsubEX<=5||tsubEY<=5){
                                isMove=true;
                                Log.d(TAG, "onTouch: isMove == true");
                            }
                            mode=RectChange(tsubSX,tsubSY,tsubEX,tsubEY);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: clickUp");
                        haveRect=true;
                        if(isFristDraw){
                            endX = (int) event.getX();
                            endY = (int) event.getY();
                            drawRect(startX, startY, endX, endY);
                            System.out.println("(startX,startY)=(" + startX + "," + startY + ")");
                            System.out.println("(endX,endY)=(" + endX + "," + endY + ")");
                            isFristDraw=isFristDraw?!isFristDraw:isFristDraw;
                        }else{
                            if(isMove) {
                                moveX = (int) event.getX() - tempX;
                                moveY = (int) event.getY() - tempY;
                                int[] xy1=getXY1(moveX,moveY);
                                drawRect(xy1[0], xy1[1], xy1[2], xy1[3]);
                                startX=xy1[0];startY=xy1[1];endX=xy1[2];endY=xy1[3];
                            }
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: clickMove");
                        if(isFristDraw){
                            endX = (int) event.getX();
                            endY = (int) event.getY();
                            drawRect(startX, startY, endX, endY);
                        } else{
                            if(isMove){
                                moveX=(int)event.getX()-tempX;
                                moveY=(int)event.getY()-tempY;
                                int[] xy1=getXY1(moveX,moveY);
                                drawRect(xy1[0], xy1[1], xy1[2], xy1[3]);
                                startX=xy1[0];startY=xy1[1];endX=xy1[2];endY=xy1[3];
                                tempX = (int) event.getX();
                                tempY = (int) event.getY();
                            }
                        }
                        break;
                }
            }else{
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: clickDown");
                        startX= (int) event.getX();
                        startY= (int) event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: clickUp");
                        endX= (int) event.getX();
                        endY= (int) event.getY();
                        drawSpecialPixel(startX,startY,endX,endY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: clickMove");
                        endX= (int) event.getX();
                        endY= (int) event.getY();
                        drawSpecialPixel(startX,startY,endX,endY);
                        startX= (int) event.getX();
                        startY= (int) event.getY();
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private int[] getXY1(int moveX, int moveY) {
        int startX1=startX,startY1=startY,endX1=endX,endY1=endY;
        switch (mode){
            case 0:
                break;
            case 1:
                startX1=startX+moveX;
                startY1=startY+moveY;
                break;
            case 2:
                startY1=startY+moveY;
                break;
            case 3:
                startY1=startY+moveY;
                endX1=endX+moveX;
                break;
            case 4:
                startX1=startX+moveX;
                break;
            case 5:
                startX1=startX+moveX;
                startY1=startY+moveY;
                endX1=endX+moveX;
                endY1=endY+moveY;
                break;
            case 6:
                endX1=endX+moveX;
                break;
            case 7:
                startX1=startX+moveX;
                endY1=endY+moveY;
                break;
            case 8:
                endY1=endY+moveY;
                break;
            case 9:
                endX1=endX+moveX;
                endY1=endY+moveY;
                break;
        }
        int []xy1={startX1,startY1,endX1,endY1};
        return xy1;
    }

    private int RectChange(int tsubSX, int tsubSY, int tsubEX, int tsubEY) {
        int mode;
        if (tsubSX <= 5) {
            if (tsubSY <= 5) {
                mode = 1;
            } else if (tsubEY <= 5) {
                mode = 7;
            } else if(tsubSY+tsubEY== abs(startY-endY)){
                mode = 4;
            }else {
                mode=0;
            }
        } else if (tsubEX <= 5) {
            if (tsubSY <= 5) {
                mode = 3;
            } else if (tsubEY <= 5) {
                mode = 9;
            } else if(tsubSY+tsubEY==  abs(startY-endY)) {
                mode = 6;
            } else{
                mode =0;
            }
        } else if(tsubEX+tsubSX==abs(startX-endX)) {
            if (tsubSY <= 5) {
                mode = 2;
            } else if (tsubEY <= 5) {
                mode = 8;
            } else if(tsubSY+tsubEY==  abs(startY-endY)) {
                mode = 5;
            } else{
                mode=0;
            }
        }else {
            mode =0;
        }
        return mode;
    }

    private void drawRect(int x1,int y1,int x2,int y2){
        drawBitmap=Bitmap.createBitmap(srcBitmap);
        paint.setStrokeWidth(5.0f);
        canvas=new Canvas(drawBitmap);
        canvas.drawLine(x1,y1,x2,y1,paint);
        canvas.drawLine(x1,y1,x1,y2,paint);
        canvas.drawLine(x2,y1,x2,y2,paint);
        canvas.drawLine(x1,y2,x2,y2,paint);
        showImage.setImageBitmap(drawBitmap);
        Log.d(TAG, "drawRect: x1="+x1+" y1="+y1+" x2="+x2+" y2="+y2);
    }

    private void drawSpecialPixel(int x1, int y1, int x2, int y2) {
        canvas=new Canvas(drawBitmap);
        paint.setStrokeWidth(5.0f);
        canvas.drawLine(x1,y1,x2,y2,paint);
        showImage.setImageBitmap(drawBitmap);
    }

}