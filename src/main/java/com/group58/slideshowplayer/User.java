package com.group58.slideshowplayer;

import com.group58.slideshowplayer.media.Media;

public class User {
    private final String name;
    private final LinkedChain<Playlist> playlists;
    private Playlist selectedPlaylist;

    public User(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name must not be empty");
        }
        this.name = name;
        this.playlists = new LinkedChain<>();
        this.selectedPlaylist = null;
    }

    public String getName() {
        return name;
    }

    public Playlist createPlaylist(String playlistName) {
        if (playlistName == null || playlistName.isBlank()) {
            throw new IllegalArgumentException("Playlist name must not be empty");
        }
        String trimmed = playlistName.trim();
        if (hasPlaylist(trimmed)) {
            throw new IllegalArgumentException(
                "A playlist named \"" + trimmed + "\" already exists");
        }
        Playlist playlist = new Playlist(trimmed);
        playlists.addToEnd(playlist);
        if (selectedPlaylist == null) {
            selectedPlaylist = playlist;
        }
        return playlist;
    }

    public Playlist getPlaylist(String playlistName) {
        if (playlistName == null) {
            return null;
        }
        String trimmed = playlistName.trim();
        for (Playlist playlist : getPlaylists()) {
            if (playlist.getName().equalsIgnoreCase(trimmed)) {
                return playlist;
            }
        }
        return null;
    }

    public boolean hasPlaylist(String playlistName) {
        return getPlaylist(playlistName) != null;
    }

    public Playlist[] getPlaylists() {
        return playlists.toArray(new Playlist[playlists.size()]);
    }

    public int getPlaylistCount() {
        return playlists.size();
    }

    public void addMedia(String playlistName, Media media) {
        if (media == null) {
            throw new IllegalArgumentException("Media must not be null");
        }
        Playlist playlist = getPlaylist(playlistName);
        if (playlist == null) {
            throw new IllegalArgumentException(
                "No playlist named \"" + playlistName + "\"");
        }
        playlist.add(media);
    }

    public void addMediaToSelected(Media media) {
        if (selectedPlaylist == null) {
            throw new IllegalStateException("No playlist is selected");
        }
        addMedia(selectedPlaylist.getName(), media);
    }

    public void selectPlaylist(String playlistName) {
        Playlist playlist = getPlaylist(playlistName);
        if (playlist == null) {
            throw new IllegalArgumentException(
                "No playlist named \"" + playlistName + "\"");
        }
        selectedPlaylist = playlist;
    }

    public Playlist getSelectedPlaylist() {
        return selectedPlaylist;
    }

    @Override
    public String toString() {
        return name + " " + playlists;
    }
}
