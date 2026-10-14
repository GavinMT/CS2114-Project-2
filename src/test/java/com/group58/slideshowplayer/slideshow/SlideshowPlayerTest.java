package com.group58.slideshowplayer.slideshow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.FxTestHelper;
import com.group58.slideshowplayer.Playlist;
import com.group58.slideshowplayer.StubMedia;
import com.group58.slideshowplayer.media.Image;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SlideshowPlayerTest {
    @TempDir
    Path home;

    private String oldHome;
    private Playlist playlist;
    private StubMedia a;
    private StubMedia b;
    private StubMedia c;
    private AtomicInteger homeCount;
    private SlideshowPlayer player;

    @BeforeAll
    public static void startFx() {
        FxTestHelper.startToolkit();
    }

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        playlist = new Playlist("Trip");
        a = new StubMedia("a");
        b = new StubMedia("b");
        c = new StubMedia("c");
        playlist.add(a);
        playlist.add(b);
        playlist.add(c);
        homeCount = new AtomicInteger();
        player = FxTestHelper.callOnFx(
            () -> new SlideshowPlayer(playlist, homeCount::incrementAndGet));
    }

    @AfterEach
    public void tearDown() {
        FxTestHelper.runOnFx(() -> {
            player.close();
            return null;
        });
        System.setProperty("user.home", oldHome);
    }

    private void fx(Runnable action) {
        FxTestHelper.runOnFx(() -> {
            action.run();
            return null;
        });
    }

    private StackPane mediaPane() {
        return (StackPane) ((StackPane) player.getRoot()).getChildren().get(0);
    }

    private HBox controls() {
        return (HBox) ((StackPane) player.getRoot()).getChildren().get(1);
    }

    private Button button(String text) {
        for (Node node : controls().getChildren()) {
            if (node instanceof Button && ((Button) node).getText().equals(text)) {
                return (Button) node;
            }
        }
        throw new AssertionError("No button " + text);
    }

    private String status() {
        HBox controls = controls();
        return ((Label) controls.getChildren().get(controls.getChildren().size() - 1))
            .getText();
    }

    @Test
    public void testConstructorBadInput() {
        assertThrows(IllegalArgumentException.class,
            () -> FxTestHelper.callOnFx(() -> new SlideshowPlayer(null, () -> { })));
        Playlist empty = new Playlist("Empty");
        assertThrows(IllegalArgumentException.class,
            () -> FxTestHelper.callOnFx(() -> new SlideshowPlayer(empty, () -> { })));
    }

    @Test
    public void testNewPlayerShowsNothing() {
        assertInstanceOf(StackPane.class, player.getRoot());
        assertTrue(mediaPane().getChildren().isEmpty());
        assertTrue(player.isMuted());
        assertEquals(0, a.playCount);
    }

    @Test
    public void testStartShowsFirstItem() {
        playlist.setCurrentIndex(2);
        fx(player::start);
        assertEquals(0, playlist.getCurrentIndex());
        assertSame(a.getView(), mediaPane().getChildren().get(0));
        assertEquals(1, a.playCount);
        assertEquals(Boolean.TRUE, a.lastMuted);
        assertEquals("1 / 3  a", status());
    }

    @Test
    public void testForwardAndBackward() {
        fx(player::start);
        fx(player::forward);
        assertEquals(1, playlist.getCurrentIndex());
        assertSame(b.getView(), mediaPane().getChildren().get(0));
        assertEquals(1, a.stopCount); // a was disposed
        assertEquals(1, b.playCount);
        assertEquals("2 / 3  b", status());

        fx(player::backward);
        fx(player::backward);
        assertEquals(2, playlist.getCurrentIndex());
        assertEquals("3 / 3  c", status());

        fx(player::forward);
        assertEquals(0, playlist.getCurrentIndex());
    }

    @Test
    public void testPauseAndPlay() {
        fx(player::start);
        fx(player::pause);
        assertEquals(1, a.pauseCount);

        // while paused, a new item is shown but not played
        fx(player::forward);
        assertEquals(0, b.playCount);

        fx(player::play);
        assertEquals(1, b.playCount);
    }

    @Test
    public void testRestart() {
        fx(player::start);
        fx(player::pause);
        fx(player::restart);
        assertEquals(1, a.stopCount);
        assertEquals(2, a.playCount);
        fx(player::forward);
        assertEquals(1, b.playCount);
    }

    @Test
    public void testControlsBeforeStartDoNothing() {
        fx(player::play);
        fx(player::pause);
        fx(player::restart);
        fx(player::toggleMute);
        assertEquals(0, a.playCount + a.pauseCount + a.stopCount);
    }

    @Test
    public void testToggleMute() {
        fx(player::start);
        assertEquals("Sound: Off", button("Sound: Off").getText());

        fx(player::toggleMute);
        assertFalse(player.isMuted());
        assertEquals(Boolean.FALSE, a.lastMuted);
        assertEquals("Sound: On", button("Sound: On").getText());

        // the mute setting carries over to the next item
        fx(player::forward);
        assertEquals(Boolean.FALSE, b.lastMuted);

        fx(player::toggleMute);
        assertTrue(player.isMuted());
        assertEquals(Boolean.TRUE, b.lastMuted);
    }

    @Test
    public void testButtons() {
        fx(player::start);
        fx(() -> button("Forward").fire());
        assertEquals(1, playlist.getCurrentIndex());
        fx(() -> button("Backward").fire());
        assertEquals(0, playlist.getCurrentIndex());
        assertEquals(2, a.playCount);
        fx(() -> button("Pause").fire());
        assertEquals(1, a.pauseCount);
        fx(() -> button("Play").fire());
        assertEquals(3, a.playCount);
        fx(() -> button("Restart").fire());
        assertEquals(4, a.playCount);
        fx(() -> button("Home").fire());
        assertEquals(1, homeCount.get());
    }

    @Test
    public void testFinishedMediaAdvances() {
        fx(player::start);
        fx(a::finish);
        assertTrue(FxTestHelper.waitFor(
            () -> FxTestHelper.callOnFx(playlist::getCurrentIndex) == 1, 3000));
    }

    @Test
    public void testOldMediaNoLongerAdvances() {
        fx(player::start);
        fx(player::forward);
        fx(a::finish); // a was released, so this must not move the slideshow
        fx(() -> { });
        assertEquals(1, playlist.getCurrentIndex());
    }

    @Test
    public void testClose() {
        fx(player::start);
        fx(player::close);
        assertEquals(1, a.stopCount);
        // closing again is safe
        fx(player::close);
        assertEquals(1, a.stopCount);
    }

    @Test
    public void testRealImageFitsScreen() throws IOException {
        Path png = FxTestHelper.writePng(home.resolve("dot.png"));
        Playlist images = new Playlist("Images");
        images.add(new Image("dot.png", "2026-01-01", png.toFile(),
            Duration.seconds(30)));
        SlideshowPlayer imagePlayer = FxTestHelper.callOnFx(
            () -> new SlideshowPlayer(images, () -> { }));
        FxTestHelper.runOnFx(() -> {
            imagePlayer.start();
            StackPane root = (StackPane) imagePlayer.getRoot();
            root.resize(400, 300);
            StackPane pane = (StackPane) root.getChildren().get(0);
            ImageView view = (ImageView) pane.getChildren().get(0);
            assertEquals(400, view.getFitWidth());
            assertEquals(300, view.getFitHeight());
            imagePlayer.close();
            return null;
        });
    }

    @Test
    public void testHomeCallbackNotRunAutomatically() {
        fx(player::start);
        fx(player::close);
        assertEquals(0, homeCount.get());
    }
}
