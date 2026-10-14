package com.group58.slideshowplayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserTest {
    @TempDir
    Path home;

    private String oldHome;
    private User user;

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        user = new User("sam");
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("user.home", oldHome);
    }

    @Test
    public void testConstructor() {
        assertEquals("sam", user.getName());
        assertEquals(0, user.getPlaylistCount());
        assertEquals(0, user.getPlaylists().length);
        assertNull(user.getSelectedPlaylist());
    }

    @Test
    public void testConstructorBadName() {
        assertThrows(IllegalArgumentException.class, () -> new User(null));
        assertThrows(IllegalArgumentException.class, () -> new User(""));
        assertThrows(IllegalArgumentException.class, () -> new User("  "));
    }

    @Test
    public void testCreatePlaylist() {
        Playlist trip = user.createPlaylist("  Trip  ");
        Playlist pets = user.createPlaylist("Pets");
        assertEquals("Trip", trip.getName());
        assertEquals(2, user.getPlaylistCount());
        assertSame(trip, user.getPlaylists()[0]);
        assertSame(pets, user.getPlaylists()[1]);
        // the first playlist becomes the selected one
        assertSame(trip, user.getSelectedPlaylist());
    }

    @Test
    public void testCreatePlaylistBadName() {
        assertThrows(IllegalArgumentException.class, () -> user.createPlaylist(null));
        assertThrows(IllegalArgumentException.class, () -> user.createPlaylist(" "));
        assertEquals(0, user.getPlaylistCount());
    }

    @Test
    public void testCreatePlaylistDuplicateIgnoresCase() {
        user.createPlaylist("Trip");
        assertThrows(IllegalArgumentException.class, () -> user.createPlaylist("trip"));
        assertThrows(IllegalArgumentException.class, () -> user.createPlaylist(" TRIP "));
        assertEquals(1, user.getPlaylistCount());
    }

    @Test
    public void testGetPlaylist() {
        Playlist trip = user.createPlaylist("Trip");
        assertSame(trip, user.getPlaylist("Trip"));
        assertSame(trip, user.getPlaylist(" trip "));
        assertNull(user.getPlaylist("Other"));
        assertNull(user.getPlaylist(null));
        assertTrue(user.hasPlaylist("TRIP"));
        assertFalse(user.hasPlaylist("Other"));
        assertFalse(user.hasPlaylist(null));
    }

    @Test
    public void testGetPlaylistsReturnsCopy() {
        user.createPlaylist("Trip");
        user.getPlaylists()[0] = null;
        assertEquals("Trip", user.getPlaylists()[0].getName());
    }

    @Test
    public void testAddMedia() {
        Playlist trip = user.createPlaylist("Trip");
        StubMedia media = new StubMedia("a");
        user.addMedia("trip", media);
        assertEquals(1, trip.size());
        assertSame(media, trip.get(0));
    }

    @Test
    public void testAddMediaBadInput() {
        user.createPlaylist("Trip");
        assertThrows(IllegalArgumentException.class, () -> user.addMedia("Trip", null));
        assertThrows(IllegalArgumentException.class,
            () -> user.addMedia("Missing", new StubMedia("a")));
        assertThrows(IllegalArgumentException.class,
            () -> user.addMedia(null, new StubMedia("a")));
    }

    @Test
    public void testSelectPlaylist() {
        user.createPlaylist("Trip");
        Playlist pets = user.createPlaylist("Pets");
        user.selectPlaylist("pets");
        assertSame(pets, user.getSelectedPlaylist());
        assertThrows(IllegalArgumentException.class, () -> user.selectPlaylist("Missing"));
        assertSame(pets, user.getSelectedPlaylist());
    }

    @Test
    public void testAddMediaToSelected() {
        user.createPlaylist("Trip");
        Playlist pets = user.createPlaylist("Pets");
        user.selectPlaylist("Pets");
        StubMedia media = new StubMedia("dog");
        user.addMediaToSelected(media);
        assertSame(media, pets.get(0));
    }

    @Test
    public void testAddMediaToSelectedWithNoPlaylist() {
        assertThrows(IllegalStateException.class,
            () -> user.addMediaToSelected(new StubMedia("a")));
    }

    @Test
    public void testToString() {
        assertEquals("sam []", user.toString());
        user.createPlaylist("Trip");
        assertEquals("sam [Trip []]", user.toString());
    }
}
