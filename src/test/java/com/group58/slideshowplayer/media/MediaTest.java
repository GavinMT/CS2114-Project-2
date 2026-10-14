package com.group58.slideshowplayer.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group58.slideshowplayer.StubMedia;

import javafx.scene.Node;
import javafx.util.Duration;

public class MediaTest {
    private StubMedia media;

    @BeforeEach
    public void setUp() {
        media = new StubMedia("beach", "2026-05-01");
    }

    /**
     * Calls the Media constructor directly.
     */
    private static Media make(File file, Duration duration) {
        return new Media("x", "2026-01-01", file, duration) {
            @Override
            public Node getView() {
                return null;
            }

            @Override
            public void play() {
            }

            @Override
            public void pause() {
            }

            @Override
            public void stop() {
            }
        };
    }

    @Test
    public void testConstructorAndGetters() {
        File file = new File("a.png");
        Media item = make(file, Duration.seconds(2));
        assertEquals("x", item.getName());
        assertEquals("2026-01-01", item.getDate());
        assertEquals(file, item.getFile());
        assertEquals(Duration.seconds(2), item.getDuration());
        assertTrue(item.getTags().isEmpty());
    }

    @Test
    public void testConstructorBadInput() {
        File file = new File("a.png");
        assertThrows(IllegalArgumentException.class, () -> make(null, Duration.ONE));
        assertThrows(IllegalArgumentException.class, () -> make(file, null));
        assertThrows(IllegalArgumentException.class, () -> make(file, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
            () -> make(file, Duration.seconds(-1)));
    }

    @Test
    public void testIndefiniteDurationAllowed() {
        assertTrue(make(new File("a.mp4"), Duration.INDEFINITE)
            .getDuration().isIndefinite());
    }

    @Test
    public void testSetters() {
        media.setName("ocean");
        media.setDate("2026-06-01");
        assertEquals("ocean", media.getName());
        assertEquals("2026-06-01", media.getDate());
    }

    @Test
    public void testAddTagNormalizes() {
        media.addTag("  Summer   Trip ");
        assertTrue(media.hasTag("summer trip"));
        assertTrue(media.hasTag("SUMMER TRIP"));
        assertTrue(media.hasTag(" summer\ttrip"));
        assertEquals("[summer trip]", media.getTags().toString());
    }

    @Test
    public void testAddTagIgnoresDuplicates() {
        media.addTag("beach");
        media.addTag("Beach");
        media.addTag(" BEACH ");
        assertEquals(1, media.getTags().size());
    }

    @Test
    public void testAddTagBadInput() {
        assertThrows(IllegalArgumentException.class, () -> media.addTag(null));
        assertThrows(IllegalArgumentException.class, () -> media.addTag(""));
        assertThrows(IllegalArgumentException.class, () -> media.addTag("   "));
        assertTrue(media.getTags().isEmpty());
    }

    @Test
    public void testHasTag() {
        assertFalse(media.hasTag("beach"));
        media.addTag("beach");
        assertTrue(media.hasTag("beach"));
        assertFalse(media.hasTag("sea"));
        assertThrows(IllegalArgumentException.class, () -> media.hasTag(null));
    }

    @Test
    public void testRemoveTag() {
        media.addTag("beach");
        media.addTag("sun");
        assertTrue(media.removeTag(" BEACH "));
        assertFalse(media.hasTag("beach"));
        assertTrue(media.hasTag("sun"));
        assertFalse(media.removeTag("beach"));
        assertFalse(media.removeTag(null));
    }

    @Test
    public void testOnFinishedCallback() {
        int[] calls = {0};
        media.finish(); // no callback yet, nothing happens
        media.setOnFinished(() -> calls[0]++);
        media.finish();
        media.finish();
        assertEquals(2, calls[0]);
        media.setOnFinished(null);
        media.finish();
        assertEquals(2, calls[0]);
    }

    @Test
    public void testDisposeStops() {
        media.dispose();
        assertEquals(1, media.stopCount);
    }

    @Test
    public void testDefaultSetMutedDoesNothing() {
        Media item = make(new File("a.png"), Duration.ONE);
        item.setMuted(true);
        item.setMuted(false);
    }

    @Test
    public void testToString() {
        assertEquals("beach | 2026-05-01 | tags: []", media.toString());
        media.addTag("sun");
        media.addTag("sea");
        assertEquals("beach | 2026-05-01 | tags: [sea, sun]", media.toString());
    }
}
