package com.group58.slideshowplayer.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.group58.slideshowplayer.FxTestHelper;

import javafx.util.Duration;

public class GifTest {
    @BeforeAll
    public static void startFx() {
        FxTestHelper.startToolkit();
    }

    @Test
    public void testConstructor() {
        File file = new File("cat.gif");
        Gif gif = new Gif("cat.gif", "2026-01-01", file, Duration.seconds(3));
        assertEquals("cat.gif", gif.getName());
        assertEquals("2026-01-01", gif.getDate());
        assertEquals(file, gif.getFile());
        assertEquals(Duration.seconds(3), gif.getDuration());
        assertInstanceOf(Image.class, gif);
    }

    @Test
    public void testUpperCaseExtension() {
        Gif gif = new Gif("CAT.GIF", "2026-01-01", new File("CAT.GIF"),
            Duration.seconds(3));
        assertEquals("CAT.GIF", gif.getName());
    }

    @Test
    public void testNotAGif() {
        assertThrows(IllegalArgumentException.class, () -> new Gif("a", "d",
            new File("cat.png"), Duration.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Gif("a", "d",
            new File("gif"), Duration.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Gif("a", "d",
            new File("cat.gif.txt"), Duration.ONE));
    }

    @Test
    public void testBadFileOrDuration() {
        assertThrows(IllegalArgumentException.class,
            () -> new Gif("a", "d", null, Duration.ONE));
        assertThrows(IllegalArgumentException.class,
            () -> new Gif("a", "d", new File("cat.gif"), Duration.ZERO));
    }
}
