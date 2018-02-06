package com.everyonedj.client;

import com.everyonedj.common.Song;

import java.util.HashMap;

/**
 * Created by Siddharth on 8/3/2015.
 */
public class AllSongsHashMap {

    private static AllSongsHashMap instance;
    public HashMap<String, Song> AllSongs;

    private AllSongsHashMap() {
        AllSongs = new HashMap<String, Song>();
    }

    public static AllSongsHashMap getInstance() {
        if (instance == null) {
            instance = new AllSongsHashMap();
        }
        return instance;
    }

    public void addSong(String songTitle, Song song) {
        AllSongs.put(songTitle, song);
    }

    public HashMap<String, Song> getAllSongs() {
        return AllSongs;
    }
}
