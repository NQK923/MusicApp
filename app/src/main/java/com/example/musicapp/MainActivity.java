package com.example.musicapp;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    ExoPlayer player;
    ActivityResultLauncher<String> recordAudioPermissionLauncher;
    final String recordAudioPer = Manifest.permission.RECORD_AUDIO;

    ConstraintLayout playerView;
    TextView playerCloseBtn;

    TextView songNameView, skipPreBtn, skipNextBtn, playPauseBtn, repeatModeBtn, playlistBtn;
    TextView homeSongNameView, homeSkipPreBtn, homePlayPauseBtn, homeSkipNextbtn;

    ConstraintLayout homeControlWrapper, headWrapper, artworkWrapper, seekbarWrapper, controlWrapper, audioVisualizerWrapper;

    CircleImageView artworkView;

    SeekBar seekBar;
    TextView progressView, durationView;

    BarVisualizer audioVisualizer;

    BlurImageView blurImageView;

    int defaultStatusColor;

    int repeatMode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defaultStatusColor = getWindow().getStatusBarColor();

        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        recyclerView = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchSongs();
            } else {
                userReponses();
            }
        });
        storagePermissionLauncher.launch(permission);

        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted && player.isPlaying()) {
                activateAudioVisualizer();
            } else {
                userReponsesOnRecordAudioPerm();
            }
        });

        player = new ExoPlayer.Builder(this).build();
        initView();

        playerControl();
    }

    private void playerControl() {
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playlistBtn.setOnClickListener(view -> exitPlayerView());

        homeControlWrapper.setOnClickListener(view -> showPlayerView());
    }

    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColor();
    }

    private void updatePlayerColor() {
    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
    }

    private void initView() {
        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreBtn = findViewById(R.id.skipPreBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playlistBtn = findViewById(R.id.playlistBtn);


        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipNextbtn = findViewById(R.id.homeSkipNextBtn);
        homeSkipPreBtn = findViewById(R.id.homeSkipPreBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);

        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        artworkWrapper = findViewById(R.id.artwordWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        seekbarWrapper = findViewById(R.id.seekbarWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);

        artworkView =findViewById(R.id.artworkView);

        seekBar =findViewById(R.id.seekbar);
        progressView =findViewById(R.id.progressView);
        durationView =findViewById(R.id.durationView);

        audioVisualizer =findViewById(R.id.visualizer);

        blurImageView = findViewById(R.id.bluerImageView);

    }

    private void userReponsesOnRecordAudioPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((shouldShowRequestPermissionRationale(recordAudioPer))) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu cấp quyền ghi âm").setMessage("Cấp quyền để tiến hành sử dụng ứng dụng!").setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storagePermissionLauncher.launch(recordAudioPer);
                            }
                        }).setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Bạn từ chối cấp quyền!!!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        } else {
            Toast.makeText(this, "Bạn từ chối sử dụng ứng dụng!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void activateAudioVisualizer() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }

    private void userReponses() {
        if ((ContextCompat.checkSelfPermission(this, permission)) == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((shouldShowRequestPermissionRationale(permission))) {
                new AlertDialog.Builder(this)
                        .setTitle("Yêu cầu cấp quyền bộ nhớ").setMessage("Cấp quyền để tiến hành đọc bộ nhớ thiết bị!").setPositiveButton("Chấp nhận", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storagePermissionLauncher.launch(permission);
                            }
                        }).setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), "Bạn từ chối cấp quyền!!!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        } else {
            Toast.makeText(this, "Bạn từ chối sử dụng ứng dụng!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSongs() {
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String sortOrder = MediaStore.Audio.Media.TITLE;
        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int duationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int singerColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int albumidColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            while ((cursor.moveToNext())) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString((nameColumn));
                int duration = cursor.getInt((duationColumn));
                String singer = cursor.getString(singerColumn);
                String album = cursor.getString(albumColumn);
                long albumid = cursor.getLong(albumidColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumid);

                Song song = new Song(name, uri, albumArtworkUri, duration, album, singer);

                songs.add(song);
            }
            showSongs(songs);
        }
    }

    private void showSongs(List<Song> songs) {
        if (songs.size() == 0) {
            Toast.makeText(this, "Không có bài hát nào ở đây!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        allSongs.clear();
        allSongs.addAll(songs);

        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));

        songAdapter = new SongAdapter(this, songs, player);

        recyclerView.setAdapter(songAdapter);

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_btn, menu);

        MenuItem menuItem = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) menuItem.getActionView();

        SearchSong(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    private void SearchSong(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> fitteredList = new ArrayList<>();
        if ((allSongs.size()) > 0) {
            for (Song song : allSongs) {
                if ((song.getTitle().toLowerCase().contains(query))) {
                    fitteredList.add(song);
                }
            }
            if ((songAdapter != null)) {
                songAdapter.filterSongs(fitteredList);
            }
        }
    }
}