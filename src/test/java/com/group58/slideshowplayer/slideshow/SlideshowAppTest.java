package com.group58.slideshowplayer.slideshow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.FxTestHelper;
import com.group58.slideshowplayer.Playlist;
import com.group58.slideshowplayer.PlaylistStorage;

import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SlideshowAppTest {
    @TempDir
    Path home;

    private String oldHome;
    private SlideshowApp app;
    private Stage stage;

    @BeforeAll
    public static void startFx() {
        FxTestHelper.startToolkit();
    }

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        app = new SlideshowApp();
        stage = FxTestHelper.callOnFx(Stage::new);
    }

    @AfterEach
    public void tearDown() {
        FxTestHelper.runOnFx(() -> {
            app.stop();
            stage.hide();
            return null;
        });
        System.setProperty("user.home", oldHome);
    }

    @Test
    public void testStartShowsHomeScreen() {
        FxTestHelper.runOnFx(() -> {
            app.start(stage);
            return null;
        });
        assertTrue(stage.isShowing());
        assertEquals("Slideshow Player", stage.getTitle());
        assertInstanceOf(BorderPane.class, stage.getScene().getRoot());
    }

    @Test
    public void testStartLoadsSavedPlaylists() throws IOException {
        Path folder = Files.createDirectories(home.resolve("Trip"));
        FxTestHelper.writePng(folder.resolve("dot.png"));
        Files.writeString(folder.resolve(PlaylistStorage.MANIFEST),
            "5.0\t2026-01-01\tdot.png\n");

        FxTestHelper.runOnFx(() -> {
            app.start(stage);
            return null;
        });
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        @SuppressWarnings("unchecked")
        ListView<Playlist> playlists =
            (ListView<Playlist>) ((VBox) root.getLeft()).getChildren().get(1);
        assertEquals(1, playlists.getItems().size());
        Playlist trip = playlists.getItems().get(0);
        assertEquals("Trip", trip.getName());
        assertEquals(1, trip.size());
    }

    @Test
    public void testStopBeforeStart() {
        FxTestHelper.runOnFx(() -> {
            app.stop();
            return null;
        });
    }

    @Test
    public void testStopAfterStart() {
        FxTestHelper.runOnFx(() -> {
            app.start(stage);
            app.stop();
            app.stop();
            return null;
        });
        assertTrue(stage.isShowing());
    }
}
