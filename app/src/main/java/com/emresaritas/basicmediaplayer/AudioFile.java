package com.emresaritas.basicmediaplayer;

import android.net.Uri;

public class AudioFile {
    private String path;

    public AudioFile(String path) {
        this.path = path;
    }


    public String getPath() {
        return path;
    }
}
