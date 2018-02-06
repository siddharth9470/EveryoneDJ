package com.everyonedj.server;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.everyonedj.R;
import com.everyonedj.common.BaseActivity;
import com.everyonedj.common.Song;
import com.everyonedj.common.TypeFaceSpan;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AddSongsActivity extends BaseActivity {

    AllSongHashMap allSongHashMap = AllSongHashMap.getInstance();
    PlaylistManager playlistManager = PlaylistManager.getInstance();
    private List<Song> allSongsFromDevice = new ArrayList<>();
    private ListView allSongsListView;
    MyAdapter adapter = new MyAdapter();
    private EditText searchText;
    private HashMap<String, Song> allSongsFromHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);
        activateToolbar();
        addingSongsToList();
        allSongsListView = (ListView) findViewById(R.id.allSongs);
        allSongsListView.setAdapter(adapter);
        searchText = (EditText) findViewById(R.id.search_Text);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/FuturaLT-Book.ttf");
        searchText.setTypeface(face);
        searchSongs();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_songs, menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        SpannableString s = new SpannableString(Html.fromHtml("<font color='#FFFFFF'>Choose your song</font>"));
        s.setSpan(new TypeFaceSpan(this, "FuturaLT-Book.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(s);
        return true;
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

    public void addingSongsToList() {
        Iterator iterator = allSongHashMap.getAllSongs().keySet().iterator();
        while ((iterator.hasNext())) {
            String key = (String) iterator.next();
            Song song = new Song(allSongHashMap.getAllSongs().get(key).getSongTitle(), allSongHashMap.getAllSongs().get(key).getSongPath()
                    , allSongHashMap.getAllSongs().get(key).getSongAlbumArt(), allSongHashMap.getAllSongs().get(key).getSongDuration(), null, null);
            allSongsFromDevice.add(song);
        }
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allSongsFromDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return allSongsFromDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row, null);
            TextView textView = (TextView) view.findViewById(R.id.song_Title);
            textView.setText((allSongsFromDevice.get(position).getSongTitle()).trim());
            TextView songDuration = (TextView) view.findViewById(R.id.song_Duration);
            songDuration.setText(getDurationFormat(allSongsFromDevice.get(position).getSongDuration()));
            ImageView imageView = (ImageView) view.findViewById(R.id.album_Art);
            Bitmap albumArt = allSongsFromDevice.get(position).getSongAlbumArt();
            if (albumArt != null) {
                imageView.setImageBitmap(albumArt);
            } else {
                imageView.setImageResource(R.drawable.no_album);
            }

            return view;
        }
    }

    public void searchSongs() {
        allSongsFromHashMap = allSongHashMap.getAllSongs();
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                allSongsFromDevice.clear();

                Iterator iterator = allSongsFromHashMap.keySet().iterator();
                while ((iterator.hasNext())) {
                    String key = (String) iterator.next();

                    if (allSongsFromHashMap.get(key).getSongTitle().toLowerCase().contains(s.toString().toLowerCase()) || allSongsFromHashMap.get(key).getSongTitle().equalsIgnoreCase(s.toString())) {
                        Song song = new Song(allSongsFromHashMap.get(key).getSongTitle(), allSongsFromHashMap.get(key).getSongPath()
                                , allSongsFromHashMap.get(key).getSongAlbumArt(), allSongsFromHashMap.get(key).getSongDuration(), null, null);
                        allSongsFromDevice.add(song);
                    }
                }
                MyAdapter resultAdapter = new MyAdapter();
                allSongsListView.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        allSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String song_title = allSongsFromDevice.get(position).getSongTitle();
                String song_path = allSongsFromDevice.get(position).getSongPath();
                Bitmap albumArt = allSongsFromDevice.get(position).getSongAlbumArt();
                int songDuration = allSongsFromDevice.get(position).getSongDuration();
                Song song = new Song(song_title, song_path, albumArt, songDuration, null, null);
                playlistManager.addSongToDefaultPlaylist(song);
                finish();
            }
        });

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
