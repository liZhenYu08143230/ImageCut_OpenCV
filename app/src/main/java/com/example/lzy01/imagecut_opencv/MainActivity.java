package com.example.lzy01.imagecut_opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private File PICTURE_FILE ;
    private Uri imageUri;
    private static final int PHOTO_REQUEST=1;
    private static final int FILE_REQUEST=2;
    private Bitmap srcBitmap;

    private ImageButton openPhoto_btn;
    private ImageButton openGallery_btn;
    private Button canny_btn;
    private Button grabCut_btn;
    private Button back_btn;
    private ImageView showImage;

    private boolean haveChooseImage;
    private static final String TAG = "MainActivity";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
    }

    private void Init() {
        openGallery_btn=findViewById(R.id.btn_galley);
        openGallery_btn.setOnClickListener(this);

        openPhoto_btn=findViewById(R.id.btn_camera);
        openPhoto_btn.setOnClickListener(this);

        canny_btn=findViewById(R.id.btn_canny);
        canny_btn.setOnClickListener(this);

        grabCut_btn=findViewById(R.id.btn_GrabCut);
        grabCut_btn.setOnClickListener(this);

        back_btn=findViewById(R.id.btn_back);
        back_btn.setOnClickListener(this);

        showImage=findViewById(R.id.showImage);
        haveChooseImage=false;
        ChangeView();
    }

    private void ChangeView() {
        if(haveChooseImage){
            openPhoto_btn.setVisibility(View.INVISIBLE);
            openGallery_btn.setVisibility(View.INVISIBLE);
            canny_btn.setVisibility(View.VISIBLE);
            grabCut_btn.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.VISIBLE);
            showImage.setVisibility(View.VISIBLE);
        }else{
            openPhoto_btn.setVisibility(View.VISIBLE);
            openGallery_btn.setVisibility(View.VISIBLE);
            canny_btn.setVisibility(View.INVISIBLE);
            grabCut_btn.setVisibility(View.INVISIBLE);
            back_btn.setVisibility(View.INVISIBLE);
            showImage.setVisibility(View.INVISIBLE);
        }
    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_camera:
                ToCamera();
                break;
            case R.id.btn_galley:
                ToGallery();
                break;
            case R.id.btn_canny:
                ToCanny();
                finish();
                break;
            case R.id.btn_GrabCut:
                ToGrabCut();
                finish();
                break;
            case R.id.btn_back:
                backFirstView();
        }
    }

    private void ToCamera() {
        String path = Environment.getExternalStorageDirectory() + File.separator +"images"; //获取路径
        String fileName = new Date().getTime()+".jpg";//定义文件名
        PICTURE_FILE = new File(path,fileName);
        if(!PICTURE_FILE.getParentFile().exists()){//文件夹不存在
            PICTURE_FILE.getParentFile().mkdirs();
        }
        imageUri = Uri.fromFile(PICTURE_FILE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);//Photo_Request是自己定义的一个请求码
        Log.d(TAG, "ToCamera: call camera with file path");
    }

    private void ToGallery() {
        Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
        //选择图片格式
        intent2.setType("image/*");
        intent2.putExtra("return-data",true);
        startActivityForResult(intent2,FILE_REQUEST);
    }

    private void ToCanny() {
        Intent cannyIntent=new Intent(MainActivity.this,ActivityCanny.class);
        cannyIntent.putExtra("srcImage",Bitmap2Bytes(srcBitmap));
        startActivity(cannyIntent);
        finish();
        Log.d(TAG, "cutImage: canny");
    }

    private void ToGrabCut() {
        Intent grabCutIntent=new Intent(MainActivity.this,ActivityGrabCut.class);
        grabCutIntent.putExtra("srcImage",Bitmap2Bytes(srcBitmap));
        startActivity(grabCutIntent);
        Log.d(TAG, "cutImage: grabcut");
        finish();
    }

    private void backFirstView() {
        haveChooseImage=false;
        ChangeView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST:
                    srcBitmap=loadBitmap(PICTURE_FILE.getPath());
                    showImage.setImageBitmap(srcBitmap);
                    haveChooseImage=true;
                    ChangeView();
                    Log.d(TAG, "onActivityResult: getPictureFromcameraSuccessful");
                    break;
                case FILE_REQUEST:
                    Uri uri = data.getData();
                    imageUri=uri;
                    //通过uri的方式返回，部分手机uri可能为空
                    if (uri != null) {
                        try {
                            //通过uri获取到bitmap对象
                            srcBitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            showImage.setImageBitmap(srcBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //部分手机可能直接存放在bundle中
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            srcBitmap=bundleExtras.getParcelable("data");
                            showImage.setImageBitmap(srcBitmap);
                        }
                    }
                    haveChooseImage=true;
                    ChangeView();
                    break;
            }
        }else{
            showImage.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "图片获取失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 从文件中载入图像
     * @param imgpath 文件路径
     * @return
     */
    private Bitmap loadBitmap(String imgpath) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // 减少内存使用量，有效防止OOM
        {
            options.inJustDecodeBounds = true;

            //InSampleSize这个参数可以调节你在decode原图时所需要的内存，有点像采样率，会丢掉一些像素，值是大于1的数，为2的幂时更利于运算。
            //举个例子：当 inSampleSize == 4 时会返回一个尺寸(长和宽)是原始尺寸1/4，像素是原来1/16的图片，由此来减少内存使用
            options.inSampleSize = 1;//手动控制此数值,决定显示时照片的大小

            options.inJustDecodeBounds = false;
        }

        // 加载图片,并返回
        return BitmapFactory.decodeFile(imgpath, options);
    }

    /**
     * bitmap to byte[]
     * @param bm bitmap
     * @return
     */
    public static byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
