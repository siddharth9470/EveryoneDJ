package com.everyonedj.server;


/**
 * Created by Siddharth on 7/17/2015.
 */
public interface SongEventListener {
    void onSongAdded(String songTitle);
    void onSongChanged();
}
