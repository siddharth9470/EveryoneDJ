package com.everyonedj.server;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.everyonedj.R;
import com.everyonedj.common.Constants;
import com.everyonedj.common.Song;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerPlaylistSongs extends Fragment implements SongEventListener {

    PlaylistManager playlistManager = PlaylistManager.getInstance();
    AudioPlayerThread audioPlayerThread = AudioPlayerThread.getInstance();
    private List<Song> serverPlayList = new ArrayList<>();
    private ListView songAddedServerPlaylist;
    MyAdapter adapter = new MyAdapter();
    private TextView ifNoSong;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_server_playlist_songs, container, false);
        songAddedServerPlaylist = (ListView) view.findViewById(R.id.songsFromServer);
        ifNoSong = (TextView) view.findViewById(R.id.ifNoSong);
        playlistUpgrade();
        return view;
    }

    @Override
    public void onResume() {
        audioPlayerThread.registerListener(this);
        serverPlayList = playlistManager.defaultPlaylistSongs;

        if (serverPlayList != null && serverPlayList.size() > 0) {
            songAddedServerPlaylist.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            ifNoSong.setVisibility(View.INVISIBLE);
        }
        removeSongFromPlaylist();
        playFromPlaylist();
        playOnTouch();
        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_server_playlist_songs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongAdded(String songTitle) {

    }

    @Override
    public void onSongChanged() {
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return serverPlayList.size();
        }

        @Override
        public Object getItem(int position) {
            return serverPlayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getActivity().getBaseContext(), R.layout.row, null);
            TextView textView = (TextView) view.findViewById(R.id.song_Title);
            textView.setText(serverPlayList.get(position).getSongTitle());
            TextView songDuration = (TextView) view.findViewById(R.id.song_Duration);
            songDuration.setText(getDurationFormat(serverPlayList.get(position).getSongDuration()));
            Bitmap albumArt = serverPlayList.get(position).getSongAlbumArt();
            ImageView imageView = (ImageView) view.findViewById(R.id.album_Art);
            if (albumArt != null) {
                imageView.setImageBitmap(albumArt);
            } else {
                imageView.setImageResource(R.drawable.no_album);
            }
            return view;
        }
    }

    public void playOnTouch() {
        songAddedServerPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playlistManager.requestedSongs != null && playlistManager.requestedSongs.size() > 0) {
                    System.out.println("rqst playlist is not null");

                } else {
                    playlistManager.setCurrentSongPlaying(position - 1);
                    audioPlayerThread.startPlaying();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Constants.ON_PLAY_START);
                    getActivity().sendBroadcast(sendIntent);
                }
            }
        });
    }

    public void removeSongFromPlaylist() {
        songAddedServerPlaylist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), view);
                popupMenu.inflate(R.menu.delete_popup_menu);
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        playlistManager.removeSongFromDefaultPlaylist(position);
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                });
                return true;
            }
        });
    }

    public void playFromPlaylist() {
        if (playlistManager.requestedSongs.size() < 1 && !audioPlayerThread.isMediaPlayerPlaying()) {
            if (serverPlayList.size() > 0 && serverPlayList.size() < 2) {
                audioPlayerThread.startPlaying();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Constants.ON_PLAY_START);
                getActivity().sendBroadcast(sendIntent);
            }
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

    public void playlistUpgrade() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (playlistManager.defaultPlaylistSongs.size() == 0) {
                            ifNoSong.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }, 1000, 2000);
    }
}
