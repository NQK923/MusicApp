package com.example.musicapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Song> songs;
    ExoPlayer player;

    ConstraintLayout playerView;

    public SongAdapter(Context context, List<Song> songs, ExoPlayer player, ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player = player;
        this.playerView = playerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item, parent, false);
        return new SongViewHolder((view));
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;
        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(getDuration(song.getDuration()));
        viewHolder.singerHolder.setText(song.getSinger());
        viewHolder.albumHolder.setText(song.getAlbum());

        Uri artworkUri = song.getArtworkUri();

        if (artworkUri != null) {
            viewHolder.artworkHolder.setImageURI(artworkUri);
            if (viewHolder.artworkHolder.getDrawable() == null) {
                viewHolder.artworkHolder.setImageResource(R.drawable.default_artwork);
            }
        }

        viewHolder.itemView.setOnClickListener(view -> {
            context.startService(new Intent(context.getApplicationContext(),PlayerService.class));
            playerView.setVisibility(View.VISIBLE);
            if (!player.isPlaying()) {
                player.setMediaItems(getMediaItems(), position, 0);
            } else {
                player.pause();
                player.seekTo(position, 0);
            }
            player.prepare();
            player.play();
            Toast.makeText(context, song.getTitle(), Toast.LENGTH_SHORT).show();
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)) != PackageManager.PERMISSION_GRANTED) {
                ((MainActivity) context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
    }

    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaItem mediaItem = new MediaItem.Builder().setUri(song.getUri()).setMediaMetadata(getMetadata(song)).build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder().setTitle(song.getTitle()).setArtworkUri(song.getArtworkUri()).build();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView artworkHolder;
        TextView titleHolder, durationHolder, singerHolder, albumHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById((R.id.durationView));
            singerHolder = itemView.findViewById((R.id.singerView));
            albumHolder = itemView.findViewById(R.id.albumView);

        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Song> filtedList) {
        songs = filtedList;
        notifyDataSetChanged();
    }

    private String getDuration(int totalDuration) {
        String totalDurationText;
        int sec = totalDuration / 1000;
        int hrs = sec / 3600;
        int min = (sec % 3600) / 60;
        sec = sec % 60;
        if ((hrs < 1)) {
            totalDurationText = String.format("%02d:%02d", min, sec);
        } else {
            totalDurationText = String.format("%02d:%02d:%02d", hrs, min, sec);
        }
        return totalDurationText;
    }
}
