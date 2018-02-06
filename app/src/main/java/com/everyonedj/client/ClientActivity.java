package com.everyonedj.client;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.everyonedj.R;
import com.everyonedj.common.Constants;
import com.everyonedj.common.Message;
import com.everyonedj.common.MessageFailedException;
import com.everyonedj.common.MessageType;
import com.everyonedj.common.Settings;
import com.everyonedj.common.Song;
import com.everyonedj.server.SongEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ClientActivity extends Activity implements StartSongSendingInterface {

    private InetAddress mInetAddress;
    String serverIp = "";
    private Socket socket;
    private EditText userName;
    private Button submitButton;
    AllSongsHashMap allSongsHashMap = AllSongsHashMap.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        setTitle("");
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/FuturaLT-Book.ttf");
        userName = (EditText) findViewById(R.id.userName);
        submitButton = (Button) findViewById(R.id.submitButton);
        userName.setTypeface(face);
        submitButton.setTypeface(face);
        mInetAddress = (InetAddress) getIntent().getSerializableExtra(Constants.SERVER_IP);
        serverIp = mInetAddress.toString();
        allSongFromDevice();
        StringTokenizer tokenizer = new StringTokenizer(serverIp, "/");
        while (tokenizer.hasMoreTokens()) {
            serverIp = tokenizer.nextToken();
            System.out.println(serverIp);
        }
        new Thread(new ConnectThread()).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    class ConnectThread implements Runnable {

        @Override
        public void run() {
            try {
                InetAddress inetAddress = InetAddress.getByName(serverIp);
                System.out.println("connecting status : " + "Connecting");
                socket = new Socket(inetAddress, Settings.PORT);
                System.out.println("connecting status : " + "Connected");

                final ServerCommunicationManager serverCommunicationManager = ServerCommunicationManager.getInstance();
                serverCommunicationManager.setSocket(socket);

                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String user_name = userName.getText().toString();

                       if(user_name != null && !user_name.equals("")) {
                           Message message = new Message();
                           message.setMessageType(MessageType.REGISTER);
                           message.setMessageContent(user_name);
                           try {
                               serverCommunicationManager.sendMessageToServer(message);
                           } catch (MessageFailedException mfe) {
                               //Handle failed message here
                           }
                       } else {
                           Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_LONG).show();
                       }

                    }
                });
                while (!socket.isClosed()) {
                    ClientIncomingMessageHandler incomingMessageHandler = new ClientIncomingMessageHandler(ClientActivity.this, socket);
                    incomingMessageHandler.readingObjectFromServer();
                }
                System.out.println("Connection lost");
                serverClosed();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartSongListener() {
        Intent intent = new Intent(ClientActivity.this, SongSendingActivity.class);
        startActivity(intent);
        finish();
    }

    public void serverClosed() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Constants.ON_SERVER_CLOSED);
        sendBroadcast(sendIntent);
    }

    private void allSongFromDevice() {

        List<Song> songDataList = new ArrayList<>();
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
           /* String artist = cursor.getString(cursor
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
            songDataList.add(song);
            allSongsHashMap.addSong(track, song);
        }

    }
}
