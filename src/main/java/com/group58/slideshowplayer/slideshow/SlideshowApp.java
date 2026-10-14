package com.group58.slideshowplayer.slideshow;

import com.group58.slideshowplayer.PlaylistStorage;
import com.group58.slideshowplayer.User;

import javafx.application.Application;
import javafx.stage.Stage;

public class SlideshowApp extends Application {
    private SlideshowScenes scenes;

    @Override
    public void start(Stage stage) {
        User user = new User(System.getProperty("user.name"));
        PlaylistStorage storage = new PlaylistStorage();
        storage.loadInto(user);
        scenes = new SlideshowScenes(stage, user, storage);
        scenes.showHome();
        stage.show();
    }

    @Override
    public void stop() {
        if (scenes != null) {
            scenes.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
