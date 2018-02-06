package com.everyonedj.server;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.everyonedj.R;
import com.everyonedj.common.Constants;
import com.everyonedj.common.Song;

import java.util.ArrayList;
import java.util.List;

public class ClientSongPlaylist extends Fragment implements SongEventListener {

    List<Song> songsFromClient = new ArrayList<>();
    PlaylistManager playlistManager = PlaylistManager.getInstance();
    AudioPlayerThread player = AudioPlayerThread.getInstance();
    ListView all_songs;
    MyAdapter adapter = new MyAdapter();
    private TextView ifNoSong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistManager.registerListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_client_song_playlist, container, false);
        all_songs = (ListView) view.findViewById(R.id.songsFromClient);
        ifNoSong = (TextView) view.findViewById(R.id.ifNoSong);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSongAdded(String songTitle) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                songsFromClient = (List<Song>) playlistManager.requestedSongs;
                all_songs.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if(playlistManager.requestedSongs != null && playlistManager.requestedSongs.size() > 0) {
                    ifNoSong.setVisibility(View.INVISIBLE);
                } else {
                    ifNoSong.setVisibility(View.VISIBLE);
                }
                System.out.println("adapter set");
                if (!player.isMediaPlayerPlaying() && !Constants.isMediaPlayerPause == true) {
                    player.startPlaying();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Constants.ON_PLAY_START);
                    getActivity().sendBroadcast(sendIntent);
                }

            }
        });
    }

    @Override
    public void onSongChanged() {
        adapter.notifyDataSetChanged();
        if(playlistManager.requestedSongs.size() == 0) {
            ifNoSong.setVisibility(View.VISIBLE);
        }
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return songsFromClient.size();
        }

        @Override
        public Object getItem(int position) {
            return songsFromClient.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getActivity().getBaseContext(), R.layout.client_playlist, null);
            TextView songTitle = (TextView) view.findViewById(R.id.songTitle);
            songTitle.setText(songsFromClient.get(position).getSongTitle());
            TextView songDuration = (TextView) view.findViewById(R.id.songDuration);
            songDuration.setText(getDurationFormat(songsFromClient.get(position).getSongDuration()));
            ImageView imageView = (ImageView) view.findViewById(R.id.album_Art);
            Bitmap albumArt = songsFromClient.get(position).getSongAlbumArt();
            if (albumArt != null) {
                imageView.setImageBitmap(albumArt);
            } else {
                imageView.setImageResource(R.drawable.no_album);
            }
            TextView clientName = (TextView) view.findViewById(R.id.addedBy);
            clientName.setText(songsFromClient.get(position).getClientName());
            return view;
        }
    }

    public String getDurationFormat(int duration) {

        String format = "";
        final int HOUR = 60 * 60 * 1000;
        final int MINUTE = 60 * 1000;
        final int SECOND = 1000;

        int durationHour = duration / HOUR;
        int durationMint = (duration % HOUR) / MINUTE;
        int durationSec = (duration % MINUTE) / SECOND;

        if (durationHour > 0) {
            format = String.format("%02d:%02d:%02d",
                    durationHour, durationMint, durationSec);
            return format;
        } else {
            format = String.format("%02d:%02d",
                    durationMint, durationSec);
            return format;
        }
    }
}
