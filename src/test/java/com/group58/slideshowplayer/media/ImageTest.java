package com.group58.slideshowplayer.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.FxTestHelper;

import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class ImageTest {
    @TempDir
    Path dir;

    private File png;
    private Image media;
    private AtomicInteger finishedCount;

    @BeforeAll
    public static void startFx() {
        FxTestHelper.startToolkit();
    }

    @BeforeEach
    public void setUp() throws IOException {
        png = FxTestHelper.writePng(dir.resolve("dot.png")).toFile();
        media = new Image("dot.png", "2026-01-01", png, Duration.millis(100));
        finishedCount = new AtomicInteger();
        media.setOnFinished(finishedCount::incrementAndGet);
    }

    @Test
    public void testConstructor() {
        assertEquals("dot.png", media.getName());
        assertEquals(png, media.getFile());
        assertEquals(Duration.millis(100), media.getDuration());
        assertInstanceOf(Media.class, media);
    }

    @Test
    public void testConstructorBadInput() {
        assertThrows(IllegalArgumentException.class,
            () -> new Image("a", "d", null, Duration.ONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Image("a", "d", png, Duration.ZERO));
    }

    @Test
    public void testGetViewIsCachedImageView() {
        Node view = FxTestHelper.callOnFx(media::getView);
        ImageView imageView = assertInstanceOf(ImageView.class, view);
        assertTrue(imageView.isPreserveRatio());
        assertFalse(imageView.getImage().isError());
        assertEquals(1, imageView.getImage().getWidth());
        assertSame(view, FxTestHelper.callOnFx(media::getView));
    }

    @Test
    public void testPlayFinishesAfterDuration() {
        FxTestHelper.runOnFx(() -> {
            media.play();
            return null;
        });
        assertTrue(FxTestHelper.waitFor(() -> finishedCount.get() == 1, 3000));
    }

    @Test
    public void testPauseHoldsTimer() throws InterruptedException {
        FxTestHelper.runOnFx(() -> {
            media.play();
            media.pause();
            return null;
        });
        Thread.sleep(300);
        assertEquals(0, finishedCount.get());

        FxTestHelper.runOnFx(() -> {
            media.play();
            return null;
        });
        assertTrue(FxTestHelper.waitFor(() -> finishedCount.get() == 1, 3000));
    }

    @Test
    public void testStopCancelsTimer() throws InterruptedException {
        FxTestHelper.runOnFx(() -> {
            media.play();
            media.stop();
            return null;
        });
        Thread.sleep(300);
        assertEquals(0, finishedCount.get());
    }

    @Test
    public void testDisposeCancelsTimer() throws InterruptedException {
        FxTestHelper.runOnFx(() -> {
            media.play();
            media.dispose();
            return null;
        });
        Thread.sleep(300);
        assertEquals(0, finishedCount.get());
    }
}
