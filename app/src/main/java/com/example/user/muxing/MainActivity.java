package com.example.user.muxing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aryan.dhankar.muxlibrary.Mp4ParserAudioMuxer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    boolean result;
    private Handler handler;
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        go=(Button)findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String path  = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
                String audiopath = path + "/"+"음성 013.m4a";
                String videopath = path+ "/"+"video.mp4";

                Mp4ParserAudioMuxer muxer=new Mp4ParserAudioMuxer();
                boolean isGrantStorage = grantExternalStoragePermission();
                if(isGrantStorage){
                    result=muxer.mux(videopath,audiopath,path+"/out.mp4");
                }
     if (result==true){
         Toast.makeText(getApplicationContext(), "Muxing completed", Toast.LENGTH_SHORT).show();
     }else {

         Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
     }
            }
        });
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean grantExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("HAN_","Permission is granted");
                return true;
            }else{
                Log.v("HAN_","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                return false;
            }
        }else{
            Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
            Log.d("HAN_", "External Storage Permission is Grant ");
            return true;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.v("HAN_","Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
            }
        }
    }


    @OnClick (R.id.pcm_encoder) void PcmEncoder(View v){
        Toast.makeText(this,"Transform Start",Toast.LENGTH_SHORT).show();
        String output_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/out.m4a";
        encodeSingleFile(output_path);

//        try {
//            File tmpFile = File.createTempFile("single_" + System.currentTimeMillis(), ".m4a", getExternalCacheDir());
//            encodeSingleFile(tmpFile.getAbsolutePath());
//        } catch (IOException e) {
//            Log.e("HAN_", "Exception while creating tmp file", e);
//        }
    }

    private void encodeSingleFile(final String outputPath) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(encodeTask(1, outputPath));
    }

    private void encodeMultipleFiles(final String outputPath) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(encodeTask(10, outputPath));
    }

    private Runnable encodeTask(final int numFiles, final String outputPath) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    final PCMEncoder pcmEncoder = new PCMEncoder(48000, 48000, 1);
                    pcmEncoder.setOutputPath(outputPath);
                    pcmEncoder.prepare();
                    for (int i = 0; i < numFiles; i++) {
                        //Log.d(TAG, "Encoding: " + i);
                        String path  = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
                        String audiopath = path + "/"+"audio1.pcm";

//                        InputStream inputStream = getAssets().open("test.wav");
//                        inputStream.skip(44);
//                        pcmEncoder.encode(inputStream, 16000);

                        FileInputStream fileInputStream = new FileInputStream(audiopath);
                        pcmEncoder.encode(fileInputStream, 48000);
                    }
                    pcmEncoder.stop();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Encoded file to: " + outputPath, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    Log.e("HAN_", "Cannot create FileInputStream", e);
                }
            }
        };
    }


}