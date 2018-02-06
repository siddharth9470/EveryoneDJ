package com.everyonedj.client;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everyonedj.R;
import com.everyonedj.common.BaseActivity;
import com.everyonedj.common.Constants;
import com.everyonedj.common.Message;
import com.everyonedj.common.MessageFailedException;
import com.everyonedj.common.MessageType;
import com.everyonedj.common.Song;
import com.everyonedj.common.TypeFaceSpan;
import com.everyonedj.common.Utility;
import com.google.analytics.tracking.android.EasyTracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class SongSendingActivity extends BaseActivity {

    AllSongsHashMap allSongsHashMap = AllSongsHashMap.getInstance();
    private List<Song> allSongsFromDevice = new ArrayList<>();
    private ListView all_songs;
    private EditText searchText;
    private LinearLayout transparentLayout;
    private HashMap<String, Song> allSongsFromHashMap;
    TextView sendingMsgText, sendingMsgText2, serverClosedText, serverClosedText2;
    ProgressBar mProgressBar;
    private BroadcastReceiver serverClosed;
    private EasyTracker easyTracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_sending);
        activateToolbar();
        getAllSongsFRomHashMap();
        allSongsFromHashMap = allSongsHashMap.getAllSongs();
        all_songs = (ListView) findViewById(R.id.allSongsList);
        all_songs.setAdapter(new MyAdapter());
        searchText = (EditText) findViewById(R.id.searchText);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/FuturaLT-Book.ttf");
        searchText.setTypeface(face);
        transparentLayout = (LinearLayout) findViewById(R.id.transparent_layout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        sendingMsgText = (TextView) findViewById(R.id.textView);
        sendingMsgText2 = (TextView) findViewById(R.id.textView2);
        serverClosedText = (TextView) findViewById(R.id.server_closed);
        serverClosedText2 = (TextView) findViewById(R.id.server_closed2);
        searchSongs();
        sendSongToServer();
        googleAnalytics();
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
        Tracker tracker = EasyTracker.getInstance(this);
        tracker.set(Fields.SCREEN_NAME, "Client Running");
        tracker.send(MapBuilder.createAppView().build());
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onResume() {
        // it calls when server close
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ON_SERVER_CLOSED);
        serverClosed = new ServerClosedBroadcasting();
        getApplicationContext().registerReceiver(serverClosed, intentFilter);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_sending, menu);

        ActionBar actionBar = getSupportActionBar();
        SpannableString s = new SpannableString(Html.fromHtml("<font color='#FFFFFF'>Add your song to queue</font>"));
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void searchSongs() {

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
                    allSongsFromHashMap.get(key).getSongTitle();

                    if (allSongsFromHashMap.get(key).getSongTitle().toLowerCase().contains(s.toString().toLowerCase()) || allSongsFromHashMap.get(key).getSongTitle().equalsIgnoreCase(s.toString())) {
                        Song song = new Song(allSongsFromHashMap.get(key).getSongTitle(), allSongsFromHashMap.get(key).getSongPath()
                                , allSongsFromHashMap.get(key).getSongAlbumArt(), allSongsFromHashMap.get(key).getSongDuration(), null, null);
                        allSongsFromDevice.add(song);
                    }
                }
                MyAdapter resultAdapter = new MyAdapter();
                all_songs.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(serverClosed);
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
            textView.setText(allSongsFromDevice.get(position).getSongTitle());
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

    public void sendSongToServer() {
        all_songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String songTitle = allSongsFromDevice.get(position).getSongTitle();
                String songPath = allSongsFromDevice.get(position).getSongPath();
                Bitmap albumArt = allSongsFromDevice.get(position).getSongAlbumArt();
                int songDuration = allSongsFromDevice.get(position).getSongDuration();
                File file = new File(songPath);
                if (file.length() < 51000000) {
                    all_songs.setEnabled(false);
                    searchText.setEnabled(false);
                    transparentLayout.setVisibility(View.VISIBLE);
                    final Message message = new Message();
                    message.setMessageContent(songTitle);
                    message.setMessageType(MessageType.ADD_FILE);

                    byte[] toSend = Utility.toByteArray(file);
                    message.setPayload(toSend);
                    if (albumArt != null) {
                        message.setAlbumArt(Utility.bitmapTooByte(albumArt));
                    }
                    message.setSongDuration(songDuration);

                    new SendFile(message).execute();
                } else {
                    Toast.makeText(SongSendingActivity.this, "Sorry you cannot send more than 50 mb song", Toast.LENGTH_LONG).show();
                }
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

    private class SendFile extends AsyncTask<Message, Void, Message> {

        Message msg;

        public SendFile(Message message) {
            this.msg = message;
        }

        @Override
        protected Message doInBackground(Message... params) {
            ServerCommunicationManager serverCommunicationManager = ServerCommunicationManager.getInstance();
            try {
                serverCommunicationManager.sendMessageToServer(msg);
            } catch (MessageFailedException mfe) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Message message) {
            transparentLayout.setVisibility(View.GONE);
            all_songs.setEnabled(true);
            searchText.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Your song is successfully added", Toast.LENGTH_SHORT).show();
            super.onPostExecute(message);
        }
    }

    private class ServerClosedBroadcasting extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((intent.getAction() != null) && Constants.ON_SERVER_CLOSED.equals(intent.getAction())) {
                all_songs.setEnabled(false);
                transparentLayout.setVisibility(View.VISIBLE);
                sendingMsgText.setVisibility(View.INVISIBLE);
                sendingMsgText2.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                serverClosedText.setVisibility(View.VISIBLE);
                serverClosedText2.setVisibility(View.VISIBLE);
                searchText.setEnabled(false);
            }
        }
    }

    private void googleAnalytics() {
        easyTracker = EasyTracker.getInstance(SongSendingActivity.this);

        easyTracker.send(MapBuilder.createEvent("your_action",
                "envet_name", "button_name/id", null).build());
    }

    private void getAllSongsFRomHashMap() {
        Iterator iterator = allSongsHashMap.getAllSongs().keySet().iterator();
        while ((iterator.hasNext())) {
            String key = (String) iterator.next();
            allSongsHashMap.getAllSongs().get(key).getSongTitle();


            Song song = new Song(allSongsHashMap.getAllSongs().get(key).getSongTitle(), allSongsHashMap.getAllSongs().get(key).getSongPath()
                    , allSongsHashMap.getAllSongs().get(key).getSongAlbumArt(), allSongsHashMap.getAllSongs().get(key).getSongDuration(), null, null);
            System.out.println(allSongsHashMap.getAllSongs().get(key).getSongTitle() + "------*-------");
            allSongsFromDevice.add(song);
        }
    }
}
