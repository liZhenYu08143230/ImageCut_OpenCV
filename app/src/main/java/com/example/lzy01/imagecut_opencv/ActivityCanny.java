package com.example.lzy01.imagecut_opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import static com.example.lzy01.imagecut_opencv.JniFunction.DoCanny;

public class ActivityCanny extends AppCompatActivity implements View.OnClickListener {

    private Button btn_canny;
    private Button btn_pre;
    private ImageView showImage;
    private Bitmap srcBitmap;
    private Bitmap cannyBitmap;
    private boolean isCannyDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__canny);
        btn_canny=findViewById(R.id.btn_doCanny);
        btn_pre=findViewById(R.id.btn_cannyPre);
        showImage=findViewById(R.id.showCannyImage);

        btn_canny.setOnClickListener(this);
        btn_pre.setOnClickListener(this);
        isCannyDone=false;
        setBtn_canny();

        Intent intent=getIntent();
        byte[] srcByte=intent.getByteArrayExtra("srcImage");
        srcBitmap= BitmapFactory.decodeByteArray(srcByte, 0, srcByte.length).copy(Bitmap.Config.ARGB_8888, true);
        showImage.setImageBitmap(srcBitmap);
    }

    private void setBtn_canny() {
        if(isCannyDone){
            btn_canny.setText(R.string.finish);
        }else{
            btn_canny.setText(R.string.canny);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_doCanny:
                if(isCannyDone){
                    Toast.makeText(getApplicationContext(), "canny have done", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(ActivityCanny.this,ActivityProcessEnd.class);
                    intent.putExtra("image",MainActivity.Bitmap2Bytes(srcBitmap));
//                    intent.putExtra("CannyImage",MainActivity.Bitmap2Bytes(cannyBitmap));
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "canny is running", Toast.LENGTH_SHORT).show();
//--------------------cannyBitmap=DoCanny(srcBitmap);
                    isCannyDone=true;
                    setBtn_canny();
//--------------------showImage.setImageBitmap(cannyBitmap);
                }
                break;
            case R.id.btn_cannyPre:
                if(isCannyDone){
                    Toast.makeText(getApplicationContext(), "Retry canny", Toast.LENGTH_SHORT).show();
                    showImage.setImageBitmap(srcBitmap);
                    isCannyDone=false;
                    setBtn_canny();
                }else {
                    Toast.makeText(getApplicationContext(), "canny undo", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
