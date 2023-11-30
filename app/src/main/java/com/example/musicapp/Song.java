package com.example.musicapp;

import android.net.Uri;

public class Song {
    String title;
    Uri uri;
    Uri artworkUri;
    int duration;
    String album;
    String singer;

    public Song(String title, Uri uri, Uri artworkUri, int duration, String album, String singer) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.duration = duration;
        this.album = album;
        this.singer = singer;
    }

    public String getTitle() {
        return title;
    }

    public Uri getUri() {
        return uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public int getDuration() {
        return duration;
    }

    public String getAlbum() {
        return album;
    }

    public String getSinger() {
        return singer;
    }

}
