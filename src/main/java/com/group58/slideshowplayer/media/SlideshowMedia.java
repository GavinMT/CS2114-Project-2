package com.group58.slideshowplayer.media;

import java.io.File;

import com.group58.slideshowplayer.LinkedChain;

import javafx.scene.Node;
import javafx.util.Duration;

public abstract class SlideshowMedia {
    private String name;
    private String date;
    private final LinkedChain<String> tags;
    private final File file;
    private final Duration duration;
    private Runnable onFinished;

    public SlideshowMedia(String name, String date, File file, Duration duration) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }
        if (duration == null || duration.lessThanOrEqualTo(Duration.ZERO)) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.name = name;
        this.date = date;
        this.file = file;
        this.duration = duration;
        this.tags = new LinkedChain<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public File getFile() {
        return file;
    }

    public Duration getDuration() {
        return duration;
    }

    private String normalizeTag(String tag) {
        return tag.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public void addTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }
        String normTag = normalizeTag(tag);
        if (normTag.isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }
        if (!hasTag(normTag)) {
            tags.add(normTag);
        }
    }

    public boolean hasTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }
        return tags.contains(normalizeTag(tag));
    }

    public boolean removeTag(String tag) {
        if (tag == null) {
            return false;
        }
        return tags.remove(normalizeTag(tag));
    }

    public LinkedChain<String> getTags() {
        return tags;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    protected void finished() {
        if (onFinished != null) {
            onFinished.run();
        }
    }

    public abstract Node getView();

    public abstract void play();

    public abstract void pause();

    public abstract void stop();

    public void dispose() {
        stop();
    }

    public void setMuted(boolean muted) {
    }

    @Override
    public String toString() {
        return name + " | " + date + " | tags: " + tags;
    }
}
