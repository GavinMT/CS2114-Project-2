package com.group58.slideshowplayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import com.group58.slideshowplayer.media.Media;

public class Playlist {
    private final String name;
    private final Path directory;
    private final LinkedChain<Media> media;
    private int currentIndex;

    public Playlist(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Playlist name must not be empty");
        }
        this.name = name;
        this.directory = Paths.get(System.getProperty("user.home"), name);
        this.media = new LinkedChain<>();
        this.currentIndex = -1;
    }

    public String getName() {
        return name;
    }

    public Path getDirectory() {
        return directory;
    }

    public boolean directoryExists() {
        return Files.isDirectory(directory);
    }

    public boolean createDirectory() throws IOException {
        if (directoryExists()) {
            return false;
        }
        Files.createDirectories(directory);
        return true;
    }

    public void add(Media item) {
        media.addToEnd(item);
        if (currentIndex == -1) {
            currentIndex = 0;
        }
    }

    public boolean remove(Media item) {
        int index = indexOf(item);
        if (index == -1) {
            return false;
        }
        media.remove(item);
        if (media.isEmpty()) {
            currentIndex = -1;
        } else if (index < currentIndex || currentIndex >= media.size()) {
            currentIndex--;
        }
        return true;
    }

    public void clear() {
        media.clear();
        currentIndex = -1;
    }

    public Media get(int index) {
        return media.get(index);
    }

    public int indexOf(Media item) {
        if (item == null) {
            return -1;
        }
        Media[] items = toArray();
        for (int i = 0; i < items.length; i++) {
            if (item.equals(items[i])) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Media item) {
        return media.contains(item);
    }

    public int size() {
        return media.size();
    }

    public boolean isEmpty() {
        return media.isEmpty();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        if (index < 0 || index >= media.size()) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for size " + media.size());
        }
        currentIndex = index;
    }

    public Media getCurrent() {
        if (media.isEmpty()) {
            return null;
        }
        return media.get(currentIndex);
    }

    public Media next() {
        if (media.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex + 1) % media.size();
        return media.get(currentIndex);
    }

    public Media previous() {
        if (media.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex - 1 + media.size()) % media.size();
        return media.get(currentIndex);
    }

    public boolean hasNext() {
        return !media.isEmpty() && currentIndex < media.size() - 1;
    }

    public boolean hasPrevious() {
        return !media.isEmpty() && currentIndex > 0;
    }

    public void sort(Comparator<? super Media> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        if (media.size() < 2) {
            return;
        }
        Media current = getCurrent();
        Media[] items = toArray();
        for (int i = 1; i < items.length; i++) {
            Media key = items[i];
            int j = i - 1;
            while (j >= 0 && comparator.compare(items[j], key) > 0) {
                items[j + 1] = items[j];
                j--;
            }
            items[j + 1] = key;
        }
        media.clear();
        for (int i = items.length - 1; i >= 0; i--) {
            media.add(items[i]);
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i] == current) {
                currentIndex = i;
                break;
            }
        }
    }

    public Media[] toArray() {
        return media.toArray(new Media[media.size()]);
    }

    @Override
    public String toString() {
        return name + " " + media;
    }
}
