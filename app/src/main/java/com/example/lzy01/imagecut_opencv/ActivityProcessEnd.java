package com.example.lzy01.imagecut_opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class ActivityProcessEnd extends AppCompatActivity implements View.OnClickListener {

    private ImageView showImage;
    private Button btn_save;
    private Button btn_share;
    private Button btn_back;
    private Bitmap srcBitmap;
    private  File SAVE_FILE;
    private static final String TAG = "ActivityProcessEnd";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_end);

        showImage=findViewById(R.id.showfinImage);
        btn_save=findViewById(R.id.btn_save);
        btn_share=findViewById(R.id.btn_share);
        btn_back=findViewById(R.id.btn_backFirst);

        btn_share.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        Intent intent=getIntent();
        byte[] srcByte=intent.getByteArrayExtra("image");
        srcBitmap= BitmapFactory.decodeByteArray(srcByte, 0, srcByte.length).copy(Bitmap.Config.ARGB_8888, true);
        showImage.setImageBitmap(srcBitmap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_save:
                savePhoto();
                break;
            case R.id.btn_share:
                shareImg("Share","AppTheme","this is picture",Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), srcBitmap, null,null)));
                break;
            case R.id.btn_backFirst:
                Intent intent=new Intent(ActivityProcessEnd.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    private void savePhoto(){
        String state= Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            String savePath=Environment.getExternalStorageDirectory()+ File.separator +"CUTimages";
            String saveFileName=new Date().getTime()+"lzy.jpg";
            SAVE_FILE=new File(savePath,saveFileName);
            if(!SAVE_FILE.getParentFile().exists()){//文件夹不存在
                SAVE_FILE.getParentFile().mkdirs();
            }
            Bitmap obmp = srcBitmap;
            FileOutputStream fos;
            try{
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE));
                obmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
    }
    private void shareImg(String dlgTitle, String subject, String content, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
}

