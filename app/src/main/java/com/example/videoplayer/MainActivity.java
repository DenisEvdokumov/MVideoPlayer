package com.example.videoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    SimpleExoPlayerView playerView;
    SimpleExoPlayer exoPlayer;
    // private ComponentListener componentListener;
    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                    + " playbackState = " + playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG, "Playback ended!");
                    //Stop playback and return to start position
//                    setPlayPause(false);
                    exoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
//                    Log.i(TAG,"ExoPlayer ready! pos: "+exoPlayer.getCurrentPosition()
//                            +" max: "+stringForTime((int)exoPlayer.getDuration()));
//                    setProgress();
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG, "Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG, "ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    };

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;


    RecyclerView recyclerView;
    VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        checkUserPermission();
        ArrayList<File> video = findVideos(Environment.getExternalStorageDirectory());
        //video.addAll(findVideos(Environment.get))
        videoAdapter = new VideoAdapter(this,video);
        recyclerView.setAdapter(videoAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
        playerView = findViewById(R.id.video_view);

        videoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Button b, View view, String s, int position) {

                    Log.i(TAG,b.getText().toString());
                    recyclerView.setVisibility(View.GONE);
                    playerView.setVisibility(View.VISIBLE);
                    prepareExoPlayerFromFileUri(Uri.fromFile(new File(s)));

            }
        });
//
//        if (Util.SDK_INT > 23) {
//            prepareExoPlayerFromFileUri(Uri.fromFile(new File("/storage/emulated/0/.a/1.mp4")));
//
//        }

    }

    private ArrayList<File> findVideos(File root) {
        ArrayList<File> res = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files){
            if(singleFile.isDirectory()&& singleFile.isHidden()){
                res.addAll(findVideos(singleFile));
            }else {
                //TODO all format
                if(singleFile.getName().endsWith(".mp4")||singleFile.getName().endsWith(".3gp")){
                    res.add(singleFile);
                }
            }
        }
        return res;
    }

    private void prepareExoPlayerFromFileUri(Uri uri) {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

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
        MediaSource mediaSource =
                new ExtractorMediaSource(fileDataSource.getUri(),
                        factory, new DefaultExtractorsFactory(), null, null);
        playerView.setPlayer(exoPlayer);
        exoPlayer.prepare(mediaSource);
        exoPlayer.seekTo(1000);
        //initMediaControls();
        //Log.i(TAG,"")
        exoPlayer.setPlayWhenReady(true);
    }



//    @Override
//    public void onResume() {
//        super.onResume();
//        hideSystemUi();
//        if ((Util.SDK_INT <= 23 || exoPlayer == null)) {
//            prepareExoPlayerFromFileUri(Uri.fromFile(new File("/storage/emulated/0/.a/1.mp4")));
//
//        }
//    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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




    private void releasePlayer() {
        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.release();
            exoPlayer = null;
        }
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