package com.tk.tcompress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class PictureActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int CAMERA = 200;
    public static final int ALBUM = 201;
    private Button btnCamera;
    private Button btnAlbum;
    private Button btnAndroid;
    private Button btnC;
    private ImageView sourcePicture;
    private TextView sourceSize;
    private ImageView compressPicture;
    private TextView compressSize;

    private File sourceFile;
    private File compressFile;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnAlbum = (Button) findViewById(R.id.btn_album);
        btnAndroid = (Button) findViewById(R.id.btn_android);
        btnC = (Button) findViewById(R.id.btn_c);
        sourcePicture = (ImageView) findViewById(R.id.source_picture);
        sourceSize = (TextView) findViewById(R.id.source_size);
        compressPicture = (ImageView) findViewById(R.id.compress_picture);
        compressSize = (TextView) findViewById(R.id.compress_size);

        btnCamera.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        btnAndroid.setOnClickListener(this);
        btnC.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        dialog.setTitle("压缩中");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                startCamera();
                break;
            case R.id.btn_album:
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, ALBUM);
                break;
            case R.id.btn_android:
                if (sourceFile == null || (!sourceFile.exists())) {
                    Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                Luban.get(this)
                        .load(sourceFile)
                        .putGear(Luban.THIRD_GEAR)
                        .setCompressListener(new OnCompressListener() {

                            @Override
                            public void onStart() {
                                dialog.show();
                            }

                            @Override
                            public void onSuccess(File file) {
                                compressFile = file;
                                Glide.with(PictureActivity.this)
                                        .load(compressFile)
                                        .into(compressPicture);
                                compressSize.setText("压缩图大小：" + MediaUtils.getFileSize(compressFile));
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                dialog.dismiss();
                            }
                        }).launch();
                break;
            case R.id.btn_c:
                dialog.show();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                     compressFile= TCompress.compressImg(PictureActivity.this,sourceFile);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                compressPicture.setImageBitmap(BitmapFactory.decodeFile(compressFile.getAbsolutePath()));
                                Glide.with(PictureActivity.this)
                                        .load(compressFile)
                                        .into(compressPicture);
                                compressSize.setText("压缩图大小：" + MediaUtils.getFileSize(compressFile));
                                dialog.dismiss();
                            }
                        });
                    }
                }).start();

                break;
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 创建临时文件，并设置系统相机拍照后的输出路径
            try {
                sourceFile = MediaUtils.createCameraTmpFile(this);
                if (sourceFile != null && sourceFile.exists()) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(sourceFile));
                    startActivityForResult(cameraIntent, CAMERA);
                } else {
                    Toast.makeText(getApplicationContext(), "创建缓存文件失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "创建缓存文件失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "您的手机不支持相机", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this)
                        .load(sourceFile)
                        .into(sourcePicture);
                sourceSize.setText("原图大小：" + MediaUtils.getFileSize(sourceFile));
            } else {
                if (sourceFile != null) {
                    if (sourceFile.exists()) {
                        sourceFile.delete();
                    }
                }
            }
        } else if (requestCode == ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                sourceFile = new File(imagePath);
                Glide.with(this)
                        .load(sourceFile)
                        .into(sourcePicture);
                sourceSize.setText("原图大小：" + MediaUtils.getFileSize(sourceFile));
            }
        }
    }
}
