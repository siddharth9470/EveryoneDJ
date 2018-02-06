package com.everyonedj.common;

import android.graphics.Bitmap;

/**
 * Created by Siddharth on 7/15/2015.
 */
public class Song {

    public String songTitle;
    public String songPath;
    public Bitmap songAlbumArt;
    int songDuration;
    public String clientName;
    public String clientIP;

    public Song(String songTitle, String songPath, Bitmap songAlbumArt, int songDuration, String clientName, String clientIP) {
        this.songTitle = songTitle;
        this.songPath = songPath;
        this.songAlbumArt = songAlbumArt;
        this.clientName = clientName;
        this.clientIP = clientIP;
        this.songDuration = songDuration;
    }

    public int getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(int songDuration) {
        this.songDuration = songDuration;
    }

    public Bitmap getSongAlbumArt() {
        return songAlbumArt;
    }

    public void setSongAlbumArt(Bitmap songAlbumArt) {
        this.songAlbumArt = songAlbumArt;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Song guest = (Song) obj;
        if(this.songPath != null
                && guest.songPath != null
                && guest.songPath.equalsIgnoreCase(this.songPath)) {
            return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((songPath == null) ? 0 : songPath.hashCode());
        return result;
    }
}
