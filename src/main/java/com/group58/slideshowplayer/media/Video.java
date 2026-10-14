package com.group58.slideshowplayer.media;

import java.io.File;

import javafx.scene.Node;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class Video extends Media {
    private MediaPlayer player;
    private MediaView view;
    private boolean muted = true;

    public Video(String name, String date, File file) {
        this(name, date, file, Duration.INDEFINITE);
    }

    public Video(String name, String date, File file, Duration duration) {
        super(name, date, file, duration);
    }

    private MediaPlayer getPlayer() {
        if (player == null) {
            player = new MediaPlayer(new javafx.scene.media.Media(getFile().toURI().toString()));
            if (!getDuration().isIndefinite()) {
                player.setStopTime(getDuration());
            }
            player.setOnEndOfMedia(this::finished);
            player.setMute(muted);
        }
        return player;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (player != null) {
            player.setMute(muted);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = new MediaView(getPlayer());
            view.setPreserveRatio(true);
        }
        return view;
    }

    @Override
    public void play() {
        getPlayer().play();
    }

    @Override
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    @Override
    public void dispose() {
        if (player != null) {
            player.dispose();
            player = null;
            view = null;
        }
    }
}
