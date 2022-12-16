package com.emresaritas.basicmediaplayer;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MediaManager {

    private MediaPlayer mediaPlayer;
    private MediaType mediaType = MediaType.EMPTY;
    private MainActivity context;
    private TextView textTimeCurrent, textTimeMax, textTitle, textArtist;
    private ImageView imageView;
    private int seekerLength;
    private MaterialButton playButton, prevButton, nextButton;
    public Uri currentMediaUri;
    private Handler handler;
    private CustomSeekerView customSeekerView;
    private MediaSession session;
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private List<String> mediaToPlay = new ArrayList<>();

    public MediaManager(MainActivity context, CustomSeekerView customSeekerView){
        this.context=context;
        this.customSeekerView=customSeekerView;
        seekerLength = customSeekerView.getMax();

        textTimeCurrent = context.findViewById(R.id.textTimeCurrent);
        textTimeMax = context.findViewById(R.id.textTimeMax);
        textTitle = context.findViewById(R.id.textTitle);
        textArtist = context.findViewById(R.id.textArtist);
        imageView = context.findViewById(R.id.imageView);
        playButton = context.findViewById(R.id.playBtn);
        prevButton = context.findViewById(R.id.prewBtn);
        nextButton = context.findViewById(R.id.nextBtn);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float perc = (float)mediaPlayer.getCurrentPosition()/(float)mediaPlayer.getDuration();
                customSeekerView.setProgress((int) (seekerLength*perc));
                textTimeCurrent.setText(format.format(mediaPlayer.getCurrentPosition()));
                mediaPlayer.seekTo(0);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runNextTrack();
            }
        });

        textTimeCurrent.setText(format.format(0));
        textTimeMax.setText(format.format(0));

        handler = new Handler();
        mediaPlayer = new MediaPlayer();


        session = new MediaSession(context, "MusicService");
        session.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.i("i","Play");
                start();
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.i("i","Pause");
                start();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                Log.i("i","Seek");
                mediaPlayer.seekTo((int) pos);
            }
        });
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        customSeekerView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(currentMediaUri == null) return;
                if(fromUser){
                    float perc = (float)seekBar.getProgress()/100;
                    mediaPlayer.seekTo((int) (perc*mediaPlayer.getDuration()));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(currentMediaUri == null) return;
                mediaPlayer.pause();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(currentMediaUri == null) return;
                float perc = (float)seekBar.getProgress()/100;
                mediaPlayer.seekTo((int) (perc*mediaPlayer.getDuration()));
                start();
            }
        });
    }

    private Runnable mainRun = new Runnable() {
        @Override
        public void run() {
            float perc = (float)mediaPlayer.getCurrentPosition()/(float)mediaPlayer.getDuration();
            customSeekerView.setProgress((int) (seekerLength*perc));
            textTimeCurrent.setText(format.format(mediaPlayer.getCurrentPosition()));
            handler.postDelayed(mainRun,500);
        }
    };



    public void stop(){
        handler.removeCallbacks(mainRun);
        currentMediaUri = null;
        context.clearSurfaceHolder();
        playButton.setIconResource(R.drawable.ic_play);
        mediaPlayer.reset();
        mediaPlayer.release();
        imageView.setImageBitmap(null);
        textTitle.setText(null);
        textArtist.setText(null);
        textTimeCurrent.setText(format.format(0));
        textTimeMax.setText(format.format(0));
    }

    public void pause(){
        playButton.setIconResource(R.drawable.ic_play);
        mediaPlayer.pause();
    }

    public void start(){
        playButton.setIconResource(R.drawable.ic_pause);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("i","Bitti");
                runNextTrack();
            }
        });
        Log.i("i","Start");
    }

    public void changeMedia(Uri uri,  ArrayList<String> mediaToPlay){
        this.mediaToPlay = new ArrayList<>();
        this.mediaToPlay.addAll(mediaToPlay);
        runNextTrack(uri.getPath());
    }
    public void runNextTrack(){
        if(mediaToPlay.isEmpty()) return;
        if(currentMediaUri == null){
            runNextTrack(mediaToPlay.get(0));
        }else{
            int index = mediaToPlay.indexOf(currentMediaUri.getPath());
            if(mediaToPlay.size() <= index+1){
                runNextTrack(mediaToPlay.get(0));
                return;
            }
            runNextTrack(mediaToPlay.get(index+1));
        }
    }
    public void runNextTrack(String path){
        stop();
        if(!new File(path).isFile()){
            mediaToPlay.remove(path);
            runNextTrack();
            return;
        }
        String type = MimeTypeMap.getFileExtensionFromUrl(path.replace(" ",""));
        if(type.equalsIgnoreCase("mp3")){
            playNewAudio(Uri.fromFile(new File(path)));
        }else if(type.equalsIgnoreCase("mp4")){
            playNewVideo(Uri.fromFile(new File(path)));
        }
    }
    private void playNewVideo(Uri uri){
        this.currentMediaUri = uri;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context,uri);
        if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null){
            textTitle.setText(uri.toString().substring(uri.toString().lastIndexOf("/")+1));
        }else{
            textTitle.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        }
        textArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
        mediaPlayer = MediaPlayer.create(context,currentMediaUri);
        start();
        textTimeMax.setText(format.format(mediaPlayer.getDuration()));
        handler.postDelayed(mainRun, 500);
        Log.i("i","Çalıyor...");
    }

    private void playNewAudio(Uri uri){
        this.currentMediaUri = uri;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context,uri);
        if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null){
            textTitle.setText(uri.toString().substring(uri.toString().lastIndexOf("/")+1));
        }else{
            textTitle.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        }
        textArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(), 0, mmr.getEmbeddedPicture().length));
        mediaPlayer = MediaPlayer.create(context,currentMediaUri);
        start();
        textTimeMax.setText(format.format(mediaPlayer.getDuration()));
        handler.postDelayed(mainRun, 500);
        Log.i("i","Çalıyor...");
    }
    public boolean isPlaying(){
        if(mediaPlayer == null) return false;
        try{
            return mediaPlayer.isPlaying();
        }catch (IllegalStateException e){
            return false;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public enum MediaType{
        VIDEO,
        AUDIO,
        EMPTY
    }
}
