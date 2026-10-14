package com.group58.slideshowplayer.slideshow;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.group58.slideshowplayer.MediaPlaylist;
import com.group58.slideshowplayer.PlaylistStorage;
import com.group58.slideshowplayer.User;
import com.group58.slideshowplayer.media.SlideshowMedia;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SlideshowScenes {
    private final Stage stage;
    private final User user;
    private final PlaylistStorage storage;
    private final Scene scene;
    private final BorderPane homeRoot;
    private final ListView<MediaPlaylist> playlistList;
    private final ListView<SlideshowMedia> mediaList;
    private final Label playlistTitle;
    private final TextField newPlaylistField;
    private final Spinner<Integer> secondsSpinner;
    private final Button addMediaButton;
    private final Button playButton;
    private SlideshowPlayer player;

    public SlideshowScenes(Stage stage, User user, PlaylistStorage storage) {
        this.stage = stage;
        this.user = user;
        this.storage = storage;

        playlistList = new ListView<>();
        playlistList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(MediaPlaylist item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                    ? null
                    : item.getName() + " (" + item.size() + ")");
            }
        });
        playlistList.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    user.selectPlaylist(newValue.getName());
                }
                refreshMedia();
            });

        newPlaylistField = new TextField();
        newPlaylistField.setPromptText("New playlist name");
        newPlaylistField.setOnAction(event -> createPlaylist());
        Button createButton = new Button("Create");
        createButton.setOnAction(event -> createPlaylist());
        HBox createRow = new HBox(8, newPlaylistField, createButton);
        HBox.setHgrow(newPlaylistField, Priority.ALWAYS);

        VBox.setVgrow(playlistList, Priority.ALWAYS);
        VBox left = new VBox(10, new Label("Your playlists"), playlistList, createRow);
        left.setPrefWidth(280);

        playlistTitle = new Label();
        playlistTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        mediaList = new ListView<>();
        mediaList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SlideshowMedia item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                    ? null
                    : item.getName() + "  [" + item.getClass().getSimpleName() + "]");
            }
        });
        VBox.setVgrow(mediaList, Priority.ALWAYS);

        secondsSpinner = new Spinner<>(1, 300, 5);
        secondsSpinner.setPrefWidth(80);
        addMediaButton = new Button("Upload media...");
        addMediaButton.setOnAction(event -> uploadMedia());
        playButton = new Button("Play slideshow");
        playButton.setOnAction(event -> showSlideshow());
        HBox actions = new HBox(8, new Label("Seconds per image/GIF:"), secondsSpinner,
            addMediaButton, playButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox right = new VBox(10, playlistTitle, mediaList, actions);

        Label title = new Label("Slideshow Player");
        title.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        homeRoot = new BorderPane();
        homeRoot.setPadding(new Insets(16));
        homeRoot.setTop(title);
        homeRoot.setLeft(left);
        homeRoot.setCenter(right);
        BorderPane.setMargin(title, new Insets(0, 0, 12, 0));
        BorderPane.setMargin(right, new Insets(0, 0, 0, 16));

        scene = new Scene(homeRoot, 1000, 650);
        stage.setScene(scene);
    }

    public void showHome() {
        if (player != null) {
            player.close();
            player = null;
        }
        scene.setRoot(homeRoot);
        stage.setTitle("Slideshow Player");
        refreshPlaylists();
    }

    public void showSlideshow() {
        MediaPlaylist playlist = playlistList.getSelectionModel().getSelectedItem();
        if (playlist == null || playlist.isEmpty()) {
            return;
        }
        player = new SlideshowPlayer(playlist, this::showHome);
        scene.setRoot(player.getRoot());
        stage.setTitle("Slideshow Player - " + playlist.getName());
        player.start();
    }

    public void shutdown() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    private void createPlaylist() {
        String name = newPlaylistField.getText().trim();
        MediaPlaylist playlist;
        try {
            playlist = storage.createPlaylist(user, name);
        }
        catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return;
        }
        catch (IOException e) {
            showError("Couldn't set up the folder for " + name + ": " + e.getMessage());
            playlist = user.getPlaylist(name);
        }
        newPlaylistField.clear();
        if (playlist != null) {
            user.selectPlaylist(playlist.getName());
        }
        refreshPlaylists();
    }

    private void uploadMedia() {
        MediaPlaylist playlist = playlistList.getSelectionModel().getSelectedItem();
        if (playlist == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Add media to " + playlist.getName());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
            "Images, GIFs and videos",
            "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif", "*.mp4", "*.m4v"));
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null) {
            return;
        }

        Duration duration = Duration.seconds(secondsSpinner.getValue());
        StringBuilder failures = new StringBuilder();
        for (File file : files) {
            try {
                user.addMedia(playlist.getName(),
                    storage.importFile(playlist, file, duration));
            }
            catch (IOException | IllegalArgumentException e) {
                failures.append(file.getName()).append(": ")
                    .append(e.getMessage()).append("\n");
            }
        }
        refreshPlaylists();
        if (failures.length() > 0) {
            showError("Some files couldn't be added:\n" + failures);
        }
    }

    private void refreshPlaylists() {
        MediaPlaylist selected = user.getSelectedPlaylist();
        playlistList.getItems().setAll(user.getPlaylists());
        if (selected != null) {
            playlistList.getSelectionModel().select(selected);
        }
        refreshMedia();
    }

    private void refreshMedia() {
        MediaPlaylist playlist = playlistList.getSelectionModel().getSelectedItem();
        if (playlist == null) {
            playlistTitle.setText("Create or select a playlist");
            mediaList.getItems().clear();
            addMediaButton.setDisable(true);
            playButton.setDisable(true);
            return;
        }
        playlistTitle.setText(playlist.getName() + "  -  " + playlist.getDirectory());
        mediaList.getItems().setAll(playlist.toArray());
        addMediaButton.setDisable(false);
        playButton.setDisable(playlist.isEmpty());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}
