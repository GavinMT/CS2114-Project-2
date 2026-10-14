package com.group58.slideshowplayer;

import java.io.File;

import com.group58.slideshowplayer.media.Media;

import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A Media for tests. It needs no real file and counts the
 * calls made to it.
 */
public class StubMedia extends Media {
    public int playCount;
    public int pauseCount;
    public int stopCount;
    public Boolean lastMuted;
    private Node view;

    public StubMedia(String name) {
        this(name, "2026-01-01");
    }

    public StubMedia(String name, String date) {
        super(name, date, new File(name + ".png"), Duration.seconds(5));
    }

    /**
     * Acts as if the media reached its end.
     */
    public void finish() {
        finished();
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = new Rectangle(10, 10);
        }
        return view;
    }

    @Override
    public void play() {
        playCount++;
    }

    @Override
    public void pause() {
        pauseCount++;
    }

    @Override
    public void stop() {
        stopCount++;
    }

    @Override
    public void setMuted(boolean muted) {
        lastMuted = muted;
    }
}
