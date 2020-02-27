package com.example.user.muxing;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    boolean result;
    Button go;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        go=(Button)findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String a  = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
                //String audiopath = a + "/"+"audio1.pcm";
                String audiopath = a + "/"+"음성 013.m4a";
                String videopath = a+ "/"+"video.mp4";


//                String root = Environment.getExternalStorageDirectory().getPath();
//                String audiopath = root + "/"+"Downloads/audio";
//                String videopath = root+ "/"+"Downloads/video.mp4";

                Mp4ParserAudioMuxer muxer=new Mp4ParserAudioMuxer();
                boolean isGrantStorage = grantExternalStoragePermission();
                if(isGrantStorage){
                    //result=muxer.mux(videopath,audiopath,Environment.getExternalStorageDirectory().getPath()+"/out.mp4");
                    result=muxer.mux(videopath,audiopath,a+"/out.mp4");
                }
          //boolean result=muxer.mux(videopath,audiopath,Environment.getExternalStorageDirectory().getPath()+"/out.mp4");
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


    private void convertAudio(String filename) throws IOException {

        String outputpath =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/converted.m4a";
// Set up MediaExtractor to read from the source.

        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(filename);


        int trackCount = extractor.getTrackCount();

// Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(outputpath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
// Set up the tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
        for (int i = 0; i < trackCount; i++) {
            extractor.selectTrack(i);
            MediaFormat format = extractor.getTrackFormat(i);
            format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AMR_NB);

            int dstIndex = muxer.addTrack(format);
            indexMap.put(i, dstIndex);
        }
// Copy the samples from MediaExtractor to MediaMuxer.
        boolean sawEOS = false;
        int bufferSize = 32000;
        int frameCount = 0;
        int offset = 100;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
/* if (degrees >= 0) {
    muxer.setOrientationHint(degrees);
}*/
// Test setLocation out of bound cases

        muxer.start();
        while (!sawEOS) {
            bufferInfo.offset = offset;
            bufferInfo.size = extractor.readSampleData(dstBuf, offset);
            if (bufferInfo.size < 0) {

                sawEOS = true;
                bufferInfo.size = 0;
            } else {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                bufferInfo.flags = extractor.getSampleFlags();
                int trackIndex = extractor.getSampleTrackIndex();
                muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                        bufferInfo);
                extractor.advance();
                frameCount++;

            }
        }
        muxer.stop();
        muxer.release();

        return;
    }



}