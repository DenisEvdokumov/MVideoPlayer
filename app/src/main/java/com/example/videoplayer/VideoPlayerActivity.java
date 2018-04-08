package com.example.videoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayerActivity";
    private String VIDEO_ID = "video_id";
    private String STATE_RESUME_POSITION = "STATE_RESUME_POSITION";

    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer exoPlayer;

    private long playbackPosition = 0;
    private int currentWindow;
    private ArrayList listVideosPath;
    private String pathToVideo;
    int videoID =-1;
    DynamicConcatenatingMediaSource playList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();


        listVideosPath = b.getParcelableArrayList("listVideosPath");

        if (videoID == -1) {
            videoID = b.getInt("videoID",0);
        }

        pathToVideo = listVideosPath.get(videoID).toString();
        playList = new DynamicConcatenatingMediaSource();


        if (Util.SDK_INT > 23) {
            CreateExoPlayer();
        }

    }

    private void CreateExoPlayer() {
        for (int i = videoID; i< listVideosPath.size(); i++){
            pathToVideo = listVideosPath.get(i).toString();
            playList.addMediaSource(prepareExoPlayerFromFileUri(Uri.fromFile(new File(pathToVideo))));
        }


        checkUserPermission();
        playerView = findViewById(R.id.video_view);
        playerView.setPlayer(exoPlayer);
        exoPlayer.seekTo(playbackPosition);

        exoPlayer.prepare(playList);
        exoPlayer.setPlayWhenReady(true);
    }

    private MediaSource prepareExoPlayerFromFileUri(Uri uri) {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };

        return new ExtractorMediaSource(fileDataSource.getUri(),
                        factory, new DefaultExtractorsFactory(), null, null);

    }

    @Override
    public void onResume() {
        super.onResume();

        if ((Util.SDK_INT <= 23 || exoPlayer == null)&& pathToVideo != null) {
            CreateExoPlayer();
            hideSystemUi();

        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void releasePlayer() {
        if (exoPlayer != null&& playerView.getPlayer() != null) {
            currentWindow = playerView.getPlayer().getCurrentWindowIndex();
            playbackPosition = Math.max(0, playerView.getPlayer().getContentPosition());
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(VIDEO_ID, currentWindow+ videoID);
        outState.putLong(STATE_RESUME_POSITION, playbackPosition);
        Log.i(TAG,currentWindow + "  " + playbackPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        videoID = savedInstanceState.getInt(VIDEO_ID);
        playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
    }

    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }

}