package com.everyonedj.server;

import com.everyonedj.common.Constants;
import com.everyonedj.common.Song;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Siddharth on 7/17/2015.
 */
public class PlaylistManager implements SongEventListener {

    private ArrayList<SongEventListener> listeners = new ArrayList<SongEventListener>();
    public Queue<Song> requestedSongs = new LinkedList<>();
    public ArrayList<Song> defaultPlaylistSongs = new ArrayList<Song>();
    private int currentSongPlaying = -1;
    public List<Song> connectedClients = new ArrayList<>();
    public int getCurrentSongPlaying() {
        return currentSongPlaying;
    }

    public void setCurrentSongPlaying(int currentSongPlaying) {
        this.currentSongPlaying = currentSongPlaying;
    }

    private static PlaylistManager instance;

    private PlaylistManager() {
    }

    public static PlaylistManager getInstance() {
        if (instance == null) {
            instance = new PlaylistManager();
        }
        return instance;
    }

    public void addRequestedSong(Song songObject) {
        requestedSongs.add(songObject);
        this.onSongAdded(songObject.getSongTitle());
    }

    public void registerListener(SongEventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onSongAdded(String songTitle) {
        for (SongEventListener listener : listeners) {
            listener.onSongAdded(songTitle);
        }
    }

    @Override
    public void onSongChanged() {
        for (SongEventListener listener : listeners) {
            listener.onSongChanged();
        }
    }

    public void addSongToDefaultPlaylist(Song song) {
        defaultPlaylistSongs.add(song);
    }

    public void removeSongFromDefaultPlaylist(int position) {
        if (defaultPlaylistSongs.size() > 0 && defaultPlaylistSongs.size() >= position + 1) {
            System.out.println("Request to delete song at index " + position);
            System.out.println( " current song index " + getCurrentSongIndex());
            defaultPlaylistSongs.remove(position);
            if(position < getCurrentSongIndex()) {
                this.currentSongPlaying--;
            }
        }
    }


    public Song getNextSong() {
        Song requestedSong = requestedSongs.poll();
        if (requestedSong == null) {
            if (currentSongPlaying == defaultPlaylistSongs.size() - 1) {
                currentSongPlaying = -1;
            }
            if (defaultPlaylistSongs != null && defaultPlaylistSongs.size() > 0) {
                currentSongPlaying++;
                requestedSong = defaultPlaylistSongs.get(this.getCurrentSongIndex()); // index error
            }
        }
        onSongChanged();
        return requestedSong;
    }

    public Song getPreviousSong() {
        Song requestedSong = null;
        if (defaultPlaylistSongs != null && defaultPlaylistSongs.size() > 0 && this.getCurrentSongIndex() > 0) {
            currentSongPlaying--;
            requestedSong = defaultPlaylistSongs.get(this.getCurrentSongIndex());
        } else if (currentSongPlaying == 0) {
            requestedSong = defaultPlaylistSongs.get(this.getCurrentSongIndex());
        }
        return requestedSong;
    }

    public int getCurrentSongIndex() {
        int songIndex = this.currentSongPlaying;
        return songIndex;
    }

}
