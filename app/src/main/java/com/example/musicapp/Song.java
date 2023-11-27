package com.example.musicapp;

import android.net.Uri;

public class Song {
    String title;
    Uri uri;
    Uri artworkUri;
    String path;
    int duration;
    public Song(String title, Uri uri, Uri artworkUri, String path, int duration) {
        this.title = title;
        this.uri = uri;
        this.artworkUri = artworkUri;
        this.path = path;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getArtworkUri() {
        return artworkUri;
    }

    public void setArtworkUri(Uri artworkUri) {
        this.artworkUri = artworkUri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
