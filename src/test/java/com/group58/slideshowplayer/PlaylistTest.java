package com.group58.slideshowplayer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.media.Media;

public class PlaylistTest {
    @TempDir
    Path home;

    private String oldHome;
    private Playlist playlist;
    private StubMedia a;
    private StubMedia b;
    private StubMedia c;

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        playlist = new Playlist("Trip");
        a = new StubMedia("a");
        b = new StubMedia("b");
        c = new StubMedia("c");
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("user.home", oldHome);
    }

    private void addAll() {
        playlist.add(a);
        playlist.add(b);
        playlist.add(c);
    }

    @Test
    public void testConstructor() {
        assertEquals("Trip", playlist.getName());
        assertEquals(home.resolve("Trip"), playlist.getDirectory());
        assertTrue(playlist.isEmpty());
        assertEquals(0, playlist.size());
        assertEquals(-1, playlist.getCurrentIndex());
        assertNull(playlist.getCurrent());
    }

    @Test
    public void testConstructorBadName() {
        assertThrows(IllegalArgumentException.class, () -> new Playlist(null));
        assertThrows(IllegalArgumentException.class, () -> new Playlist(""));
        assertThrows(IllegalArgumentException.class, () -> new Playlist("   "));
    }

    @Test
    public void testCreateDirectory() throws IOException {
        assertFalse(playlist.directoryExists());
        assertTrue(playlist.createDirectory());
        assertTrue(playlist.directoryExists());
        assertTrue(Files.isDirectory(home.resolve("Trip")));
        assertFalse(playlist.createDirectory());
    }

    @Test
    public void testAddKeepsOrderAndSetsCurrent() {
        addAll();
        assertEquals(3, playlist.size());
        assertFalse(playlist.isEmpty());
        assertEquals(0, playlist.getCurrentIndex());
        assertSame(a, playlist.getCurrent());
        assertArrayEquals(new Media[] {a, b, c}, playlist.toArray());
        assertSame(b, playlist.get(1));
    }

    @Test
    public void testAddNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> playlist.add(null));
    }

    @Test
    public void testIndexOfAndContains() {
        addAll();
        assertEquals(0, playlist.indexOf(a));
        assertEquals(2, playlist.indexOf(c));
        assertEquals(-1, playlist.indexOf(new StubMedia("z")));
        assertEquals(-1, playlist.indexOf(null));
        assertTrue(playlist.contains(b));
        assertFalse(playlist.contains(new StubMedia("z")));
    }

    @Test
    public void testRemoveBeforeCurrentShiftsIndex() {
        addAll();
        playlist.setCurrentIndex(2);
        assertTrue(playlist.remove(a));
        assertEquals(1, playlist.getCurrentIndex());
        assertSame(c, playlist.getCurrent());
    }

    @Test
    public void testRemoveAfterCurrentKeepsIndex() {
        addAll();
        playlist.setCurrentIndex(0);
        assertTrue(playlist.remove(c));
        assertEquals(0, playlist.getCurrentIndex());
        assertSame(a, playlist.getCurrent());
    }

    @Test
    public void testRemoveLastWhileCurrent() {
        addAll();
        playlist.setCurrentIndex(2);
        assertTrue(playlist.remove(c));
        assertEquals(1, playlist.getCurrentIndex());
        assertSame(b, playlist.getCurrent());
    }

    @Test
    public void testRemoveOnlyItem() {
        playlist.add(a);
        assertTrue(playlist.remove(a));
        assertTrue(playlist.isEmpty());
        assertEquals(-1, playlist.getCurrentIndex());
    }

    @Test
    public void testRemoveMissing() {
        playlist.add(a);
        assertFalse(playlist.remove(b));
        assertFalse(playlist.remove(null));
        assertEquals(1, playlist.size());
    }

    @Test
    public void testClear() {
        addAll();
        playlist.clear();
        assertTrue(playlist.isEmpty());
        assertEquals(-1, playlist.getCurrentIndex());
        assertNull(playlist.getCurrent());
    }

    @Test
    public void testSetCurrentIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class,
            () -> playlist.setCurrentIndex(0));
        addAll();
        assertThrows(IndexOutOfBoundsException.class,
            () -> playlist.setCurrentIndex(-1));
        assertThrows(IndexOutOfBoundsException.class,
            () -> playlist.setCurrentIndex(3));
    }

    @Test
    public void testNextWrapsAround() {
        addAll();
        assertSame(b, playlist.next());
        assertSame(c, playlist.next());
        assertSame(a, playlist.next());
        assertEquals(0, playlist.getCurrentIndex());
    }

    @Test
    public void testPreviousWrapsAround() {
        addAll();
        assertSame(c, playlist.previous());
        assertSame(b, playlist.previous());
        assertEquals(1, playlist.getCurrentIndex());
    }

    @Test
    public void testNextAndPreviousOnEmpty() {
        assertNull(playlist.next());
        assertNull(playlist.previous());
        assertFalse(playlist.hasNext());
        assertFalse(playlist.hasPrevious());
    }

    @Test
    public void testHasNextAndHasPrevious() {
        addAll();
        assertTrue(playlist.hasNext());
        assertFalse(playlist.hasPrevious());
        playlist.setCurrentIndex(1);
        assertTrue(playlist.hasNext());
        assertTrue(playlist.hasPrevious());
        playlist.setCurrentIndex(2);
        assertFalse(playlist.hasNext());
        assertTrue(playlist.hasPrevious());
    }

    @Test
    public void testSortByNameKeepsCurrentItem() {
        playlist.add(c);
        playlist.add(a);
        playlist.add(b);
        playlist.setCurrentIndex(0); // c
        playlist.sort(Comparator.comparing(Media::getName));
        assertArrayEquals(new Media[] {a, b, c}, playlist.toArray());
        assertSame(c, playlist.getCurrent());
        assertEquals(2, playlist.getCurrentIndex());
    }

    @Test
    public void testSortIsStable() {
        StubMedia first = new StubMedia("x", "2026-01-01");
        StubMedia second = new StubMedia("y", "2026-01-01");
        StubMedia older = new StubMedia("z", "2025-01-01");
        playlist.add(first);
        playlist.add(second);
        playlist.add(older);
        playlist.sort(Comparator.comparing(Media::getDate));
        assertArrayEquals(new Media[] {older, first, second},
            playlist.toArray());
    }

    @Test
    public void testSortSmallOrNullComparator() {
        assertThrows(IllegalArgumentException.class, () -> playlist.sort(null));
        playlist.sort(Comparator.comparing(Media::getName));
        assertTrue(playlist.isEmpty());
        playlist.add(a);
        playlist.sort(Comparator.comparing(Media::getName));
        assertSame(a, playlist.getCurrent());
    }

    @Test
    public void testToString() {
        assertEquals("Trip []", playlist.toString());
        playlist.add(a);
        assertTrue(playlist.toString().startsWith("Trip [a | "));
    }
}
