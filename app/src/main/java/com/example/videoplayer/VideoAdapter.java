package com.example.videoplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoHolder> {
    private ArrayList videos;
    private Context context;

    private OnItemClickListener mOnItemClickListener;

    public VideoAdapter(Context context, ArrayList<File> videos) {
        this.context = context;
        this.videos = videos;
    }

    public interface OnItemClickListener {
        void onItemClick(Button b, View view, String s, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public VideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context).inflate(R.layout.row_video, parent,false);
        return new VideoHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoHolder holder, final int position) {
        final String s =  videos.get(position).toString();
        final File file = (File) videos.get(position);

        holder.tvArtistName.setText( videos.get(position).toString());

        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.btnAction,v, s, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class VideoHolder extends RecyclerView.ViewHolder {
        TextView tvArtistName;
        Button btnAction;
        public VideoHolder(View itemView) {
            super(itemView);
            tvArtistName = (TextView) itemView.findViewById(R.id.tvArtistName);
            btnAction = (Button) itemView.findViewById(R.id.btnPlay);

        }
    }
}
