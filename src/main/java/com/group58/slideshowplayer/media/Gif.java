package com.group58.slideshowplayer.media;

import java.io.File;

import javafx.util.Duration;

public class Gif extends Image {

    public Gif(String name, String date, File file, Duration duration) {
        super(name, date, file, duration);
        if (!file.getName().toLowerCase().endsWith(".gif")) {
            throw new IllegalArgumentException("Not a .gif file: " + file);
        }
    }
}
