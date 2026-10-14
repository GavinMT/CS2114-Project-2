package com.group58.slideshowplayer.slideshow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.FxTestHelper;
import com.group58.slideshowplayer.Playlist;
import com.group58.slideshowplayer.PlaylistStorage;
import com.group58.slideshowplayer.StubMedia;
import com.group58.slideshowplayer.User;

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SlideshowScenesTest {
    @TempDir
    Path home;

    private String oldHome;
    private User user;
    private PlaylistStorage storage;
    private Stage stage;
    private SlideshowScenes scenes;

    @BeforeAll
    public static void startFx() {
        FxTestHelper.startToolkit();
    }

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        user = new User("sam");
        storage = new PlaylistStorage();
        stage = FxTestHelper.callOnFx(Stage::new);
    }

    @AfterEach
    public void tearDown() {
        fx(() -> {
            if (scenes != null) {
                scenes.shutdown();
            }
            stage.hide();
        });
        System.setProperty("user.home", oldHome);
    }

    private void fx(Runnable action) {
        FxTestHelper.runOnFx(() -> {
            action.run();
            return null;
        });
    }

    private void openHome() {
        fx(() -> {
            scenes = new SlideshowScenes(stage, user, storage);
            scenes.showHome();
        });
    }

    private Parent root() {
        return stage.getScene().getRoot();
    }

    private VBox leftPanel() {
        return (VBox) ((BorderPane) root()).getLeft();
    }

    private VBox rightPanel() {
        return (VBox) ((BorderPane) root()).getCenter();
    }

    @SuppressWarnings("unchecked")
    private ListView<Playlist> playlistList() {
        return (ListView<Playlist>) leftPanel().getChildren().get(1);
    }

    private ListView<?> mediaList() {
        return (ListView<?>) rightPanel().getChildren().get(1);
    }

    private Label playlistTitle() {
        return (Label) rightPanel().getChildren().get(0);
    }

    private Button actionButton(String text) {
        HBox actions = (HBox) rightPanel().getChildren().get(2);
        return (Button) actions.getChildren().stream()
            .filter(node -> node instanceof Button
                && ((Button) node).getText().equals(text))
            .findFirst()
            .orElseThrow();
    }

    private Playlist playlistWith(String name, StubMedia... items)
        throws IOException {
        Playlist playlist = storage.createPlaylist(user, name);
        for (StubMedia item : items) {
            playlist.add(item);
        }
        return playlist;
    }

    @Test
    public void testConstructorSetsScene() {
        fx(() -> scenes = new SlideshowScenes(stage, user, storage));
        assertInstanceOf(BorderPane.class, root());
        assertEquals(1000, stage.getScene().getWidth());
        assertEquals(650, stage.getScene().getHeight());
    }

    @Test
    public void testShowHomeWithNoPlaylists() {
        openHome();
        assertEquals("Slideshow Player", stage.getTitle());
        assertTrue(playlistList().getItems().isEmpty());
        assertEquals("Create or select a playlist", playlistTitle().getText());
        assertTrue(actionButton("Upload media...").isDisabled());
        assertTrue(actionButton("Play slideshow").isDisabled());
    }

    @Test
    public void testShowHomeListsPlaylists() throws IOException {
        StubMedia a = new StubMedia("a");
        Playlist trip = playlistWith("Trip", a);
        playlistWith("Pets");
        openHome();

        assertEquals(2, playlistList().getItems().size());
        assertSame(trip, playlistList().getSelectionModel().getSelectedItem());
        assertEquals(1, mediaList().getItems().size());
        assertSame(a, mediaList().getItems().get(0));
        assertTrue(playlistTitle().getText().startsWith("Trip  -  "));
        assertFalse(actionButton("Upload media...").isDisabled());
        assertFalse(actionButton("Play slideshow").isDisabled());
    }

    @Test
    public void testSelectingPlaylistUpdatesUser() throws IOException {
        playlistWith("Trip", new StubMedia("a"));
        Playlist pets = playlistWith("Pets");
        openHome();

        fx(() -> playlistList().getSelectionModel().select(pets));
        assertSame(pets, user.getSelectedPlaylist());
        assertTrue(mediaList().getItems().isEmpty());
        assertTrue(actionButton("Play slideshow").isDisabled());
    }

    @Test
    public void testShowSlideshow() throws IOException {
        StubMedia a = new StubMedia("a");
        playlistWith("Trip", a, new StubMedia("b"));
        openHome();

        fx(scenes::showSlideshow);
        assertEquals("Slideshow Player - Trip", stage.getTitle());
        assertInstanceOf(StackPane.class, root());
        assertEquals(1, a.playCount);
    }

    @Test
    public void testShowSlideshowWithEmptyPlaylist() throws IOException {
        playlistWith("Empty");
        openHome();
        Parent home = root();
        fx(scenes::showSlideshow);
        assertSame(home, root());
        assertEquals("Slideshow Player", stage.getTitle());
    }

    @Test
    public void testShowSlideshowWithNoPlaylist() {
        openHome();
        Parent home = root();
        fx(scenes::showSlideshow);
        assertSame(home, root());
    }

    @Test
    public void testShowHomeClosesPlayer() throws IOException {
        StubMedia a = new StubMedia("a");
        playlistWith("Trip", a);
        openHome();
        Parent home = root();

        fx(scenes::showSlideshow);
        fx(scenes::showHome);
        assertSame(home, root());
        assertEquals("Slideshow Player", stage.getTitle());
        assertEquals(1, a.stopCount);
    }

    @Test
    public void testShutdown() throws IOException {
        StubMedia a = new StubMedia("a");
        playlistWith("Trip", a);
        openHome();

        fx(scenes::shutdown); // no player yet
        assertEquals(0, a.stopCount);

        fx(scenes::showSlideshow);
        fx(scenes::shutdown);
        assertEquals(1, a.stopCount);
        fx(scenes::shutdown);
        assertEquals(1, a.stopCount);
    }
}
