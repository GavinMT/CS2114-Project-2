package com.group58.slideshowplayer.slideshow;

import com.group58.slideshowplayer.Playlist;
import com.group58.slideshowplayer.media.Media;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class SlideshowPlayer {
    private static final Duration IDLE_DELAY = Duration.seconds(3);

    private final Playlist playlist;
    private final StackPane root;
    private final StackPane mediaPane;
    private final HBox controls;
    private final Label status;
    private final Button muteButton;
    private final PauseTransition idleTimer;
    private final FadeTransition fadeOut;
    private Media current;
    private boolean playing;
    private boolean muted = true;

    public SlideshowPlayer(Playlist playlist, Runnable onHome) {
        if (playlist == null || playlist.isEmpty()) {
            throw new IllegalArgumentException("Playlist must have media to play");
        }
        this.playlist = playlist;

        mediaPane = new StackPane();
        mediaPane.setMinSize(0, 0);

        Button backward = new Button("Backward");
        Button play = new Button("Play");
        Button pause = new Button("Pause");
        Button restart = new Button("Restart");
        Button forward = new Button("Forward");
        Button home = new Button("Home");
        backward.setOnAction(event -> backward());
        play.setOnAction(event -> play());
        pause.setOnAction(event -> pause());
        restart.setOnAction(event -> restart());
        forward.setOnAction(event -> forward());
        home.setOnAction(event -> onHome.run());

        muteButton = new Button();
        muteButton.setOnAction(event -> toggleMute());
        updateMuteButton();

        status = new Label();
        status.setStyle("-fx-text-fill: white;");

        controls = new HBox(10, backward, play, pause, restart, forward, muteButton,
            home, status);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));
        controls.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 8;");
        controls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        root = new StackPane(mediaPane, controls);
        root.setStyle("-fx-background-color: black;");
        StackPane.setAlignment(controls, Pos.BOTTOM_CENTER);
        StackPane.setMargin(controls, new Insets(20));

        idleTimer = new PauseTransition(IDLE_DELAY);
        idleTimer.setOnFinished(event -> hideControls());
        fadeOut = new FadeTransition(Duration.millis(400), controls);
        fadeOut.setToValue(0);

        EventHandler<Event> activity = event -> showControls();
        root.addEventFilter(MouseEvent.MOUSE_MOVED, activity);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, activity);
        root.addEventFilter(MouseEvent.MOUSE_DRAGGED, activity);
        root.addEventFilter(TouchEvent.TOUCH_PRESSED, activity);
        root.addEventFilter(TouchEvent.TOUCH_MOVED, activity);
    }

    public Parent getRoot() {
        return root;
    }

    public void start() {
        playlist.setCurrentIndex(0);
        playing = true;
        show(playlist.getCurrent());
        showControls();
    }

    public void play() {
        playing = true;
        if (current != null) {
            current.play();
        }
    }

    public void pause() {
        playing = false;
        if (current != null) {
            current.pause();
        }
    }

    public void restart() {
        playing = true;
        if (current != null) {
            current.stop();
            current.play();
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (current != null) {
            current.setMuted(muted);
        }
        updateMuteButton();
    }

    public boolean isMuted() {
        return muted;
    }

    public void forward() {
        show(playlist.next());
    }

    public void backward() {
        show(playlist.previous());
    }

    public void close() {
        idleTimer.stop();
        fadeOut.stop();
        release();
    }

    private void show(Media media) {
        release();
        current = media;
        media.setMuted(muted);
        Node view = media.getView();
        fitToScreen(view);
        mediaPane.getChildren().setAll(view);
        media.setOnFinished(() -> Platform.runLater(this::forward));
        status.setText((playlist.getCurrentIndex() + 1) + " / " + playlist.size()
            + "  " + media.getName());
        if (playing) {
            media.play();
        }
    }

    private void release() {
        if (current != null) {
            current.setOnFinished(null);
            current.dispose();
            current = null;
        }
    }

    private void updateMuteButton() {
        if (muted) {
            muteButton.setText("Sound: Off");
            muteButton.setStyle("-fx-base: #c0392b;");
        }
        else {
            muteButton.setText("Sound: On");
            muteButton.setStyle("-fx-base: #27ae60;");
        }
    }

    private void fitToScreen(Node view) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            imageView.fitWidthProperty().bind(root.widthProperty());
            imageView.fitHeightProperty().bind(root.heightProperty());
        }
        else if (view instanceof MediaView) {
            MediaView mediaView = (MediaView) view;
            mediaView.fitWidthProperty().bind(root.widthProperty());
            mediaView.fitHeightProperty().bind(root.heightProperty());
        }
    }

    private void showControls() {
        fadeOut.stop();
        controls.setOpacity(1);
        controls.setMouseTransparent(false);
        root.setCursor(Cursor.DEFAULT);
        idleTimer.playFromStart();
    }

    private void hideControls() {
        if (controls.isHover()) {
            idleTimer.playFromStart();
            return;
        }
        controls.setMouseTransparent(true);
        root.setCursor(Cursor.NONE);
        fadeOut.playFromStart();
    }
}
