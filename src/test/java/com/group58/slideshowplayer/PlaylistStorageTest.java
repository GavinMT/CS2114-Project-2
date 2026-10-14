package com.group58.slideshowplayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group58.slideshowplayer.media.Gif;
import com.group58.slideshowplayer.media.Image;
import com.group58.slideshowplayer.media.Media;
import com.group58.slideshowplayer.media.Video;

import javafx.util.Duration;

public class PlaylistStorageTest {
    @TempDir
    Path home;

    @TempDir
    Path sources;

    private String oldHome;
    private PlaylistStorage storage;
    private User user;

    @BeforeAll
    public static void startFx() {
        // Image creates a PauseTransition, which needs the toolkit
        FxTestHelper.startToolkit();
    }

    @BeforeEach
    public void setUp() {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString());
        storage = new PlaylistStorage();
        user = new User("sam");
    }

    @AfterEach
    public void tearDown() {
        System.setProperty("user.home", oldHome);
    }

    private File source(String name) throws IOException {
        return Files.writeString(sources.resolve(name), "data").toFile();
    }

    private Path manifest(String playlistName) {
        return home.resolve(playlistName).resolve(PlaylistStorage.MANIFEST);
    }

    @Test
    public void testIsValidPlaylistName() {
        assertTrue(PlaylistStorage.isValidPlaylistName("Trip"));
        assertTrue(PlaylistStorage.isValidPlaylistName("Trip 2026"));
        assertFalse(PlaylistStorage.isValidPlaylistName(null));
        assertFalse(PlaylistStorage.isValidPlaylistName(""));
        assertFalse(PlaylistStorage.isValidPlaylistName("   "));
        assertFalse(PlaylistStorage.isValidPlaylistName(".hidden"));
        for (String bad : new String[] {"a/b", "a\\b", "a:b", "a*b", "a?b",
            "a\"b", "a<b", "a>b", "a|b"}) {
            assertFalse(PlaylistStorage.isValidPlaylistName(bad), bad);
        }
    }

    @Test
    public void testIsSupported() {
        for (String good : new String[] {"a.png", "a.jpg", "a.jpeg", "a.bmp",
            "a.gif", "a.mp4", "a.m4v", "A.PNG", "my.photo.JPG"}) {
            assertTrue(PlaylistStorage.isSupported(good), good);
        }
        for (String bad : new String[] {"a.txt", "a.mov", "png", "a.", "a"}) {
            assertFalse(PlaylistStorage.isSupported(bad), bad);
        }
    }

    @Test
    public void testCreatePlaylistMakesFolderAndManifest() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        assertEquals("Trip", playlist.getName());
        assertTrue(Files.isDirectory(home.resolve("Trip")));
        assertTrue(Files.isRegularFile(manifest("Trip")));
        assertSame(playlist, user.getPlaylist("Trip"));
    }

    @Test
    public void testCreatePlaylistKeepsExistingManifest() throws IOException {
        Files.createDirectories(home.resolve("Trip"));
        Files.writeString(manifest("Trip"), "old");
        storage.createPlaylist(user, "Trip");
        assertEquals("old", Files.readString(manifest("Trip")));
    }

    @Test
    public void testCreatePlaylistBadName() {
        assertThrows(IllegalArgumentException.class,
            () -> storage.createPlaylist(user, ".hidden"));
        assertThrows(IllegalArgumentException.class,
            () -> storage.createPlaylist(user, "a/b"));
        assertThrows(IllegalArgumentException.class,
            () -> storage.createPlaylist(user, ""));
        assertEquals(0, user.getPlaylistCount());
    }

    @Test
    public void testCreatePlaylistDuplicate() throws IOException {
        storage.createPlaylist(user, "Trip");
        assertThrows(IllegalArgumentException.class,
            () -> storage.createPlaylist(user, "trip"));
    }

    @Test
    public void testImportImage() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        Media media = storage.importFile(playlist, source("beach.png"),
            Duration.seconds(5));

        assertInstanceOf(Image.class, media);
        assertEquals("beach.png", media.getName());
        assertEquals(LocalDate.now().toString(), media.getDate());
        assertEquals(Duration.seconds(5), media.getDuration());
        assertEquals(home.resolve("Trip").resolve("beach.png").toFile(), media.getFile());
        assertTrue(media.getFile().isFile());

        List<String> lines = Files.readAllLines(manifest("Trip"), StandardCharsets.UTF_8);
        assertEquals(List.of("5.0\t" + LocalDate.now() + "\tbeach.png"), lines);
    }

    @Test
    public void testImportPicksMediaType() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        Duration duration = Duration.seconds(3);
        assertInstanceOf(Gif.class,
            storage.importFile(playlist, source("cat.gif"), duration));
        assertInstanceOf(Video.class,
            storage.importFile(playlist, source("clip.mp4"), duration));
        assertInstanceOf(Image.class,
            storage.importFile(playlist, source("photo.JPEG"), duration));
    }

    @Test
    public void testImportDoesNotAddToPlaylist() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        storage.importFile(playlist, source("beach.png"), Duration.seconds(5));
        assertEquals(0, playlist.size());
    }

    @Test
    public void testImportSameNameGetsNumberedCopy() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        File file = source("beach.png");
        storage.importFile(playlist, file, Duration.seconds(5));
        Media second = storage.importFile(playlist, file, Duration.seconds(5));
        Media third = storage.importFile(playlist, file, Duration.seconds(5));
        assertEquals("beach (1).png", second.getName());
        assertEquals("beach (2).png", third.getName());
        assertEquals(3, Files.readAllLines(manifest("Trip")).size());
    }

    @Test
    public void testImportCreatesMissingFolder() throws IOException {
        Playlist playlist = user.createPlaylist("Trip");
        storage.importFile(playlist, source("beach.png"), Duration.seconds(5));
        assertTrue(Files.isRegularFile(home.resolve("Trip").resolve("beach.png")));
        assertTrue(Files.isRegularFile(manifest("Trip")));
    }

    @Test
    public void testImportUnsupportedFile() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        assertThrows(IllegalArgumentException.class,
            () -> storage.importFile(playlist, source("notes.txt"), Duration.seconds(5)));
        assertEquals(0, Files.size(manifest("Trip")));
    }

    @Test
    public void testImportMissingSourceFile() throws IOException {
        Playlist playlist = storage.createPlaylist(user, "Trip");
        File missing = sources.resolve("missing.png").toFile();
        assertThrows(IOException.class,
            () -> storage.importFile(playlist, missing, Duration.seconds(5)));
    }

    @Test
    public void testLoadIntoRestoresSavedPlaylists() throws IOException {
        Playlist trip = storage.createPlaylist(user, "Trip");
        storage.importFile(trip, source("beach.png"), Duration.seconds(4));
        storage.importFile(trip, source("clip.mp4"), Duration.seconds(4));
        storage.createPlaylist(user, "Empty");

        User loaded = new User("sam");
        assertEquals(2, new PlaylistStorage().loadInto(loaded));
        assertEquals(2, loaded.getPlaylistCount());
        // folders load in sorted order
        assertEquals("Empty", loaded.getPlaylists()[0].getName());

        Playlist restored = loaded.getPlaylist("Trip");
        assertEquals(2, restored.size());
        assertInstanceOf(Image.class, restored.get(0));
        assertEquals(Duration.seconds(4), restored.get(0).getDuration());
        assertEquals(LocalDate.now().toString(), restored.get(0).getDate());
        assertInstanceOf(Video.class, restored.get(1));
        assertTrue(loaded.getPlaylist("Empty").isEmpty());
    }

    @Test
    public void testLoadIntoSkipsBadLines() throws IOException {
        Path folder = Files.createDirectories(home.resolve("Trip"));
        FxTestHelper.writePng(folder.resolve("good.png"));
        Files.writeString(folder.resolve("notes.txt"), "x");
        Files.writeString(folder.resolve(PlaylistStorage.MANIFEST), String.join("\n",
            "5.0\t2026-01-01\tgood.png",
            "not a valid line",
            "5.0\t2026-01-01\tmissing.png",
            "5.0\t2026-01-01\tnotes.txt",
            "abc\t2026-01-01\tgood.png",
            "0\t2026-01-01\tgood.png",
            ""));

        assertEquals(1, storage.loadInto(user));
        Playlist trip = user.getPlaylist("Trip");
        assertEquals(1, trip.size());
        assertEquals("good.png", trip.get(0).getName());
        assertEquals("2026-01-01", trip.get(0).getDate());
    }

    @Test
    public void testLoadIntoIgnoresFoldersWithoutManifest() throws IOException {
        Files.createDirectories(home.resolve("Documents"));
        Files.writeString(home.resolve("file.txt"), "x");
        assertEquals(0, storage.loadInto(user));
        assertEquals(0, user.getPlaylistCount());
    }

    @Test
    public void testLoadIntoSkipsExistingAndInvalidNames() throws IOException {
        storage.createPlaylist(user, "Trip");
        Path hidden = Files.createDirectories(home.resolve(".hidden"));
        Files.createFile(hidden.resolve(PlaylistStorage.MANIFEST));

        assertEquals(0, storage.loadInto(user));
        assertEquals(1, user.getPlaylistCount());
    }

    @Test
    public void testLoadIntoMissingHome() {
        System.setProperty("user.home", home.resolve("does-not-exist").toString());
        assertEquals(0, new PlaylistStorage().loadInto(user));
    }
}
