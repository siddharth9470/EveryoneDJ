package com.everyonedj.server;

import android.media.MediaPlayer;

import com.everyonedj.common.Constants;
import com.everyonedj.common.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Siddharth on 7/18/2015.
 */
public class AudioPlayerThread extends Thread {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<SongEventListener> listeners = new ArrayList<SongEventListener>();
    private int finalTime = 0;
    private int startTime = 0;
    private int backwardTime = 5000;
    private int forwardTime = 5000;
    private PlaylistManager plManager = PlaylistManager.getInstance();
    private String songFilePath;


    private static AudioPlayerThread instance;

    private AudioPlayerThread() {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if ((plManager.defaultPlaylistSongs != null && plManager.defaultPlaylistSongs.size() > 0) ||
                        (plManager.requestedSongs != null && plManager.requestedSongs.size() > 0)) {

                    getNextSongAndPlay();
                    onSongPlayListener();

                }

            }
        });
    }

    public void onSongPlayListener() {
        for (SongEventListener listener : listeners) {
            listener.onSongChanged();
        }
    }

    public static AudioPlayerThread getInstance() {
        if (instance == null) {
            instance = new AudioPlayerThread();
        }
        return instance;
    }

    public void startPlaying() {
        getNextSongAndPlay();
    }

    private void getNextSongAndPlay() {
        Song song = plManager.getNextSong();
        if(song != null) {
            setSong(song.songPath);
            System.out.println("Song path --- " + song.songPath);
            play();
            Constants.currentSongPlaying = song.getSongTitle();
        }
    }

    public void setSong(String songFilePath) {
        this.mediaPlayer.stop();
        this.mediaPlayer.reset();
        this.songFilePath = songFilePath;
        try {
            this.mediaPlayer.setDataSource(songFilePath);
            mediaPlayer.prepare();
        } catch (IOException ioe) {

        }
    }

    public void play() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void nextSong() {
        getNextSongAndPlay();
        onSongPlayListener();
    }

    public void previousSong() {
        if (plManager.defaultPlaylistSongs != null && plManager.defaultPlaylistSongs.size() > 0) {
            Song song = plManager.getPreviousSong();
            if(song != null) {
                setSong(song.songPath);
                play();
                Constants.currentSongPlaying = song.getSongTitle();
                onSongPlayListener();
            }
        }
    }

    public void forward() {
        startTime = mediaPlayer.getCurrentPosition();
        finalTime = mediaPlayer.getDuration();
        int temp = (int) startTime;
        if ((temp + forwardTime) <= finalTime) {
            startTime = startTime + forwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }

    public void backward() {
        startTime = mediaPlayer.getCurrentPosition();
        int temp = (int) startTime;
        if ((temp - backwardTime) > 0) {
            startTime = startTime - backwardTime;
            mediaPlayer.seekTo((int) startTime);
        }
    }

    public boolean isMediaPlayerPlaying() {
        if(mediaPlayer.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    public void isMediaPlayerPause() {
        mediaPlayer.pause();
    }

    public void seekBar(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public int mediaFinalTime() {
        finalTime = mediaPlayer.getDuration();
        return finalTime;
    }

    public int mediaStartTime() {
        startTime = mediaPlayer.getCurrentPosition();
        return startTime;
    }

    public void registerListener(SongEventListener listener) {
        listeners.add(listener);
    }



}
