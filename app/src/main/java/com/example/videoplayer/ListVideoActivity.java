package com.example.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class ListVideoActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayerActivity";

    RecyclerView recyclerView;
    VideoAdapter videoAdapter;
    ArrayList<File> listVideosPath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);

        checkUserPermission();

        findVideos(Environment.getExternalStorageDirectory());
        findVideos(Environment.getDataDirectory());

        recyclerView = findViewById(R.id.recyclerView);
        videoAdapter = new VideoAdapter(this, listVideosPath);
        recyclerView.setAdapter(videoAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);


        videoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Button b, View view, String s, int position) {

                startActivity(new Intent(getApplicationContext(),VideoPlayerActivity.class)
                        .putExtra("videoID",position)
                        .putExtra("listVideosPath", listVideosPath));
            }
        });

        if (Util.SDK_INT > 23) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    private void findVideos(File root) {

        File[] files = root.listFiles();
        if (files != null) {

            for (File singleFile : files) {

                if (singleFile.isDirectory() || singleFile.isHidden()) {
                    findVideos(singleFile);
                } else {
                    if (singleFile.getName().endsWith(".mp4") ||
                            singleFile.getName().endsWith(".3gp") ||
                            singleFile.getName().endsWith(".m4a")) {
                        listVideosPath.add(singleFile);
                    }
                }
            }
        }

    }


    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    findVideos(Environment.getExternalStorageDirectory());
                    findVideos(Environment.getDataDirectory());
                    recyclerView.setAdapter(videoAdapter);
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
