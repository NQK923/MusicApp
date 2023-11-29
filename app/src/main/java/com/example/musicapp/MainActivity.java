package com.example.musicapp;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chibde.visualizer.LineBarVisualizer;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
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

    LineBarVisualizer audioVisualizer;

    BlurImageView blurImageView;

    int defaultStatusColor;

    int bodyTextColor;

    int rgb;

    int repeatMode = 1;

    boolean isBond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defaultStatusColor = getWindow().getStatusBarColor();

        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

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

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);

                songNameView.setText(Objects.requireNonNull(mediaItem).mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText((getReadableTime((int) player.getCurrentPosition())));
                seekBar.setProgress((int) player.getCurrentPosition());
                seekBar.setMax((int) player.getDuration());
                durationView.setText(getReadableTime((int) player.getDuration()));
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_pause, 0, 0, 0);
                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);

                showCurrentArtWork();

                updatePlayerPositionProgress();

                artworkView.setAnimation(loadRotation());

                activateAudioVisualizer();

                updatePlayerColor();

                if (!player.isPlaying()) {
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY) {
                    songNameView.setText((Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title));
                    homeSongNameView.setText((player.getCurrentMediaItem().mediaMetadata.title));
                    progressView.setText((getReadableTime((int) player.getCurrentPosition())));
                    durationView.setText((getReadableTime((int) player.getDuration())));
                    seekBar.setMax((int) player.getDuration());
                    seekBar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_pause, 0, 0, 0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);

                    showCurrentArtWork();

                    updatePlayerPositionProgress();

                    artworkView.setAnimation(loadRotation());

                    activateAudioVisualizer();

                    updatePlayerColor();
                } else {
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_outline, 0, 0, 0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_, 0, 0, 0);
                }
            }
        });

        skipNextBtn.setOnClickListener(view -> skipNextSong());
        homeSkipNextbtn.setOnClickListener(view -> skipNextSong());

        skipPreBtn.setOnClickListener(view -> skipPreSong());
        homeSkipPreBtn.setOnClickListener(view -> skipPreSong());

        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player.getPlaybackState() == ExoPlayer.STATE_READY) {
                    seekBar.setProgress((progressValue));
                    progressView.setText((getReadableTime(progressValue)));
                    player.seekTo(progressValue);
                }
            }
        });

        repeatModeBtn.setOnClickListener(view -> {
            if ((repeatMode == 1)) {
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one, 0, 0, 0);
            } else if (repeatMode == 2) {
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle, 0, 0, 0);
            } else if (repeatMode == 3) {
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat, 0, 0, 0);
            }
        });
        updatePlayerColor();
    }

    private void playOrPausePlayer() {
        if (player.isPlaying()) {
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle_outline, 0, 0, 0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_, 0, 0, 0);
            artworkView.clearAnimation();
        } else {
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_pause, 0, 0, 0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause, 0, 0, 0);
            artworkView.startAnimation(loadRotation());
        }
        updatePlayerColor();

    }

    private void skipPreSong() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious();
        } else {
            player.seekTo(player.getMediaItemCount() - 1, 0);
        }
    }

    private void skipNextSong() {
        if (player.hasNextMediaItem()) {
            player.seekToNext();
        } else {
            player.seekTo(0, 0);
        }
    }

    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    progressView.setText((getReadableTime((int) player.getCurrentPosition())));
                    seekBar.setProgress((int) player.getCurrentPosition());
                }
                updatePlayerPositionProgress();
            }
        }, 1000);
    }

    private void showCurrentArtWork() {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem().mediaMetadata.artworkUri));

        if (artworkView.getDrawable() == null) {
            artworkView.setImageResource(R.drawable.default_artwork);
        }
    }

    private String getReadableTime(int currentPosition) {
        String totalDurationText;
        int sec = currentPosition / 1000;
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

    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColor();
    }

    private void updatePlayerColor() {
        if (playerView.getVisibility() == View.GONE) {
            return;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) artworkView.getDrawable();
        if (bitmapDrawable == null) {
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.default_artwork);
        }
        assert bitmapDrawable != null;
        Bitmap bitmap = bitmapDrawable.getBitmap();

        blurImageView.setImageBitmap(bitmap);
        blurImageView.setBlur(4);

        Palette.from(bitmap).generate(palette -> {
            if (palette != null) {
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                if (swatch == null) {
                    swatch = palette.getMutedSwatch();
                    if (swatch == null) {
                        swatch = palette.getDominantSwatch();
                    }
                }

                int titleTextColor = swatch.getTitleTextColor();
                bodyTextColor = swatch.getBodyTextColor();
                rgb = swatch.getRgb();

                getWindow().setStatusBarColor(rgb);
                getWindow().setNavigationBarColor(rgb);


                songNameView.setTextColor(titleTextColor);
                playerCloseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
                progressView.setTextColor(bodyTextColor);
                durationView.setTextColor(bodyTextColor);

                repeatModeBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                skipPreBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                skipNextBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                playPauseBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                playlistBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
            }
        });

    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor, 199));
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

        artworkView = findViewById(R.id.artworkView);

        seekBar = findViewById(R.id.seekbar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);

        audioVisualizer = findViewById(R.id.visualizer);

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
        if ((ContextCompat.checkSelfPermission(this, recordAudioPer)) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioVisualizer.setColor(bodyTextColor);
        audioVisualizer.setPlayer(player.getAudioSessionId());
        audioVisualizer.setDensity(144);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();

    }

    @Override
    public void onBackPressed() {
        if (playerView.getVisibility() == View.VISIBLE) {
            exitPlayerView();
        } else {
            super.onBackPressed();
        }
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
        String path = null;
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getStringExtra("path");
        }
        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;
        String newPath = path.replaceFirst("/storage/emulated/0", "");

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
        String selection = MediaStore.Audio.Media.DATA + " like ? ";
        String[] selectionArgs = new String[]{"%" + newPath + "/%"};
        Log.i("Test", path);
        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, selection, selectionArgs, sortOrder)) {
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

        String title = getResources().getString(R.string.app_name);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager((layoutManager));

        songAdapter = new SongAdapter(this, songs, player, playerView);

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