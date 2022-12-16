package com.emresaritas.basicmediaplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.emresaritas.basicmediaplayer.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

    private Button playButton;
    private Button openMediaButton;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private CustomSeekerView customSeekerView;
    private final MainActivity context = this;
    private MediaManager mediaManager;
    private final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public final int EXTERNAL_REQUEST = 98;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playBtn);
        openMediaButton = findViewById(R.id.openMediaBtn);
        surfaceView = findViewById(R.id.surfaceView);
        customSeekerView = findViewById(R.id.customSeeker);

        mediaManager = new MediaManager(this,customSeekerView);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        playButton.setOnClickListener(playButtonOnClick);
        openMediaButton.setOnClickListener(openMediaButtonOnClick);


        if(getIntent().getExtras() != null){
            if(getIntent().getExtras().getString("path") != null){
                String path = getIntent().getExtras().getString("path");
                Log.i("i","Path: "+ path);
                Uri uri = Uri.fromFile(new File(path));
                Log.i("i","Uri: "+ uri);
                String extension = MimeTypeMap.getFileExtensionFromUrl(path.replace(" ",""));
                Log.i("i","extension: "+ extension);
                mediaManager.changeMedia(uri, getIntent().getExtras().getStringArrayList("medias"));
            }
        }

    }
    private View.OnClickListener playButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mediaManager.currentMediaUri == null) return;
            if(mediaManager.isPlaying()){
                mediaManager.pause();
            }else{
                mediaManager.start();
            }
        }
    };
    private View.OnClickListener openMediaButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openMedia();
        }
    };

    public void openMedia(){
        mediaManager.stop();
        if(requestForPermission()){
            Intent intent = new Intent(MainActivity.this, FileExploreActivity.class);
            intent.putExtra("path", Environment.getExternalStorageDirectory().toString());
            startActivity(intent);
        }else{
            Toast.makeText(this,"Please give permission",Toast.LENGTH_SHORT).show();
        }
    }

    public void clearSurfaceHolder(){
        Log.i("i","Değiş");
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.setFormat(PixelFormat.OPAQUE);
        Log.i("i","Değişti");
    }

    public boolean requestForPermission() {
        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }
        return isPermissionOn;
    }
    public boolean canAccessExternalSd() {
        return (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
    }
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i("i","Create");
        mediaManager.getMediaPlayer().setDisplay(holder);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}