package com.group58.slideshowplayer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import com.group58.slideshowplayer.media.Gif;
import com.group58.slideshowplayer.media.Image;
import com.group58.slideshowplayer.media.Media;
import com.group58.slideshowplayer.media.Video;

import javafx.util.Duration;

public class PlaylistStorage {
    public static final String MANIFEST = ".slideshow-playlist";

    private static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg", "bmp"};
    private static final String[] VIDEO_EXTENSIONS = {"mp4", "m4v"};

    private final Path home;

    public PlaylistStorage() {
        this.home = Paths.get(System.getProperty("user.home"));
    }

    public int loadInto(User user) {
        int loaded = 0;
        try (Stream<Path> entries = Files.list(home)) {
            Path[] folders = entries
                .filter(path -> Files.isRegularFile(path.resolve(MANIFEST)))
                .sorted()
                .toArray(Path[]::new);
            for (Path folder : folders) {
                if (loadPlaylist(user, folder)) {
                    loaded++;
                }
            }
        }
        catch (IOException e) {
            return loaded;
        }
        return loaded;
    }

    public Playlist createPlaylist(User user, String name) throws IOException {
        if (!isValidPlaylistName(name)) {
            throw new IllegalArgumentException(
                "Playlist names can't be empty, start with a dot, "
                + "or contain / \\ : * ? \" < > |");
        }
        Playlist playlist = user.createPlaylist(name);
        playlist.createDirectory();
        Path manifest = playlist.getDirectory().resolve(MANIFEST);
        if (!Files.exists(manifest)) {
            Files.createFile(manifest);
        }
        return playlist;
    }

    public Media importFile(Playlist playlist, File source, Duration duration)
        throws IOException {
        if (!isSupported(source.getName())) {
            throw new IllegalArgumentException("unsupported file type");
        }
        playlist.createDirectory();
        Path target = uniqueTarget(playlist.getDirectory(), source.getName());
        Files.copy(source.toPath(), target);
        String date = LocalDate.now().toString();
        Media media = createMedia(target.toFile(), duration, date);
        String line = duration.toSeconds() + "\t" + date + "\t"
            + target.getFileName() + System.lineSeparator();
        Files.writeString(playlist.getDirectory().resolve(MANIFEST), line,
            StandardCharsets.UTF_8, StandardOpenOption.CREATE,
            StandardOpenOption.APPEND);
        return media;
    }

    public static boolean isValidPlaylistName(String name) {
        return name != null
            && !name.isBlank()
            && !name.startsWith(".")
            && !name.matches(".*[/\\\\:*?\"<>|].*");
    }

    public static boolean isSupported(String fileName) {
        String extension = extensionOf(fileName);
        return extension.equals("gif")
            || matches(extension, IMAGE_EXTENSIONS)
            || matches(extension, VIDEO_EXTENSIONS);
    }

    private boolean loadPlaylist(User user, Path folder) {
        String name = folder.getFileName().toString();
        if (!isValidPlaylistName(name) || user.hasPlaylist(name)) {
            return false;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(folder.resolve(MANIFEST), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            return false;
        }
        user.createPlaylist(name);
        for (String line : lines) {
            String[] parts = line.split("\t", 3);
            if (parts.length != 3) {
                continue;
            }
            File file = folder.resolve(parts[2]).toFile();
            if (!file.isFile() || !isSupported(file.getName())) {
                continue;
            }
            try {
                Duration duration = Duration.seconds(Double.parseDouble(parts[0]));
                user.addMedia(name, createMedia(file, duration, parts[1]));
            }
            catch (IllegalArgumentException e) {
                continue;
            }
        }
        return true;
    }

    private Media createMedia(File file, Duration duration, String date) {
        String name = file.getName();
        String extension = extensionOf(name);
        if (extension.equals("gif")) {
            return new Gif(name, date, file, duration);
        }
        if (matches(extension, IMAGE_EXTENSIONS)) {
            return new Image(name, date, file, duration);
        }
        if (matches(extension, VIDEO_EXTENSIONS)) {
            return new Video(name, date, file);
        }
        throw new IllegalArgumentException("unsupported file type");
    }

    private Path uniqueTarget(Path directory, String fileName) {
        Path target = directory.resolve(fileName);
        int dot = fileName.lastIndexOf('.');
        String base = dot == -1 ? fileName : fileName.substring(0, dot);
        String extension = dot == -1 ? "" : fileName.substring(dot);
        int copy = 1;
        while (Files.exists(target)) {
            target = directory.resolve(base + " (" + copy + ")" + extension);
            copy++;
        }
        return target;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot == -1 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    private static boolean matches(String extension, String[] options) {
        for (String option : options) {
            if (option.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
