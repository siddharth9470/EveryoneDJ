package com.everyonedj.server;

import com.everyonedj.common.Song;

import java.util.HashMap;

/**
 * Created by Siddharth on 7/30/2015.
 */
public class AllSongHashMap {

    private static AllSongHashMap instance;
    public HashMap<String, Song> allSongs;

    private AllSongHashMap() {
        allSongs = new HashMap<String, Song>();
    }

    public static AllSongHashMap getInstance() {
        if (instance == null) {
            instance = new AllSongHashMap();
        }
        return instance;
    }

    public void addSongToServerPlaylist(String songTitle, Song song) {
        allSongs.put(songTitle, song);
    }

    public HashMap<String, Song> getAllSongs() {
        return allSongs;
    }
}
