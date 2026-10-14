package com.group58.slideshowplayer.media;

import java.io.File;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Image extends Media {
    private final PauseTransition timer;
    private ImageView view;

    public Image(String name, String date, File file, Duration duration) {
        super(name, date, file, duration);
        timer = new PauseTransition(duration);
        timer.setOnFinished(event -> finished());
    }

    @Override
    public Node getView() {
        if (view == null) {
            view = new ImageView(new javafx.scene.image.Image(getFile().toURI().toString()));
            view.setPreserveRatio(true);
        }
        return view;
    }

    @Override
    public void play() {
        timer.play();
    }

    @Override
    public void pause() {
        timer.pause();
    }

    @Override
    public void stop() {
        timer.stop();
    }
}
