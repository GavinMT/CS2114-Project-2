package com.group58.slideshowplayer.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.util.Duration;

/**
 * Tests the parts of Video that work without decoding a real video.
 * getView() and play() need a playable MP4, so they are covered by
 * running the app.
 */
public class VideoTest {
    private File file;
    private Video video;

    @BeforeEach
    public void setUp() {
        file = new File("clip.mp4");
        video = new Video("clip.mp4", "2026-01-01", file);
    }

    @Test
    public void testDefaultConstructor() {
        assertEquals("clip.mp4", video.getName());
        assertEquals("2026-01-01", video.getDate());
        assertEquals(file, video.getFile());
        assertTrue(video.getDuration().isIndefinite());
    }

    @Test
    public void testConstructorWithDuration() {
        Video clip = new Video("c", "d", file, Duration.seconds(8));
        assertEquals(Duration.seconds(8), clip.getDuration());
    }

    @Test
    public void testConstructorBadInput() {
        assertThrows(IllegalArgumentException.class,
            () -> new Video("c", "d", null));
        assertThrows(IllegalArgumentException.class,
            () -> new Video("c", "d", file, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
            () -> new Video("c", "d", file, null));
    }

    @Test
    public void testMutedByDefault() {
        assertTrue(video.isMuted());
    }

    @Test
    public void testSetMutedBeforePlayerExists() {
        video.setMuted(false);
        assertFalse(video.isMuted());
        video.setMuted(true);
        assertTrue(video.isMuted());
    }

    @Test
    public void testControlsBeforePlayerExistDoNothing() {
        video.pause();
        video.stop();
        video.dispose();
        video.dispose();
        assertTrue(video.isMuted());
    }
}
