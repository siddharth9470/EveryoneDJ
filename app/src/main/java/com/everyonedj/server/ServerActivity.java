package com.everyonedj.server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.everyonedj.common.ActiveClients;
import com.everyonedj.common.BaseActivity;
import com.everyonedj.common.Settings;

import com.everyonedj.R;
import com.everyonedj.common.Song;

public class ServerActivity extends Activity {

    private Socket socket;
    private ServerSocket serverSocket = null;
    private Button yesButton;
    AllSongHashMap allSongHashMap = AllSongHashMap.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        yesButton = (Button) findViewById(R.id.yesButton);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/FuturaLT-Book.ttf");
        yesButton.setTypeface(face);
        allSongFromDevice();
        alertMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void alertMessage() {
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new MulticastThread()).start();
                new Thread(new ConnectThread()).start();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void allSongFromDevice() {

        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final Cursor cursor = getApplicationContext().getContentResolver().query(uri,
                cursor_cols, where, null, null);

        while (cursor.moveToNext()) {
            /*String artist = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));*/
            String track = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String data = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            Long albumId = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

            int duration = cursor.getInt(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                        getApplicationContext().getContentResolver(), albumArtUri);
                bitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }

            Song song = new Song(track, data, bitmap, duration, null, null);
            allSongHashMap.addSongToServerPlaylist(track, song);

        }

    }

    class MulticastThread implements Runnable {

        @Override
        public void run() {

            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicastLock");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();

            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(Settings.INET_ADDR);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }


            try (DatagramSocket serverSocket = new DatagramSocket()) {
                while (true) {
                    String msg = "Clients";
                    DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                            msg.getBytes().length, inetAddress, Settings.PORT);
                    serverSocket.send(msgPacket);
                    System.out.println("Server Multicasting to : " + msg);
                    Thread.sleep(100);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectThread implements Runnable {

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(Settings.PORT);
            } catch (Exception ee) {
                ee.printStackTrace();
            }

            while (true) {
                try {
                    System.out.println("Waiting for Client to connect");
                    if (serverSocket != null) {
                        socket = serverSocket.accept();
                        System.out.println("Client connected");
                        ClientManager clientManager = new ClientManager(socket);
                        new Thread(clientManager).start();
                        ActiveClients activeClients = ActiveClients.getInstance();
                        activeClients.addClient(socket.getInetAddress().toString(), clientManager);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
