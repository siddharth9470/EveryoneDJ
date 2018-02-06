package com.everyonedj.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.everyonedj.common.ActiveClients;
import com.everyonedj.common.Message;
import com.everyonedj.common.MessageType;
import com.everyonedj.common.Song;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Siddharth on 7/13/2015.
 */
public class ServerIncomingMessageHandler {

    private Socket socket;

    public ServerIncomingMessageHandler(Socket socket) {
        this.socket = socket;
    }
    ActiveClients activeClients1 = ActiveClients.getInstance();
    PlaylistManager playlistManager = PlaylistManager.getInstance();


    public void readingObjectFromClients() throws IOException {

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            final Message messageObject = (Message) objectInputStream.readObject();
            handleMessage(messageObject);
        } catch (IOException e) {
            e.printStackTrace();
            socket.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            socket.close();
        }

    }



    public void handleMessage(Message message) {
        int messageType = message.getMessageType();

        switch (messageType) {

            case MessageType.REGISTER:
                System.out.println("REGISTRATION REQUEST RECEIVED");
                ActiveClients activeClients = ActiveClients.getInstance();
                ClientManager clientManager = activeClients.getClientManager(socket.getInetAddress().toString());
                clientManager.setClientName(message.getMessageContent());
                int message_Type = MessageType.REGISTER_ACKNOWLEDGE;
                clientManager.sendMessageToClient(clientManager.getClientName() + " you are successfully registered", message_Type);
                ClientManager clientManager2 = activeClients1.getClientManager(socket.getInetAddress().toString());
                Song song2 = new Song(null, null, null, 0, clientManager2.getClientName(), socket.getInetAddress().toString());
                playlistManager.connectedClients.add(song2);
                break;

            case MessageType.ADD_FILE:
                System.out.println("FILE RECEIVED TO ADD");
                File sdcardFolder = new File(Environment.getExternalStorageDirectory() + "/songsFromClient");
                sdcardFolder.mkdirs();
                String songTitle = message.getMessageContent();
                final File file = new File(sdcardFolder, songTitle);
                byte[] payloadReceived = message.getPayload();
                byte[] receivedAlbumArt = message.getAlbumArt();
                Bitmap bitmap = null;
                if (receivedAlbumArt != null) {
                    bitmap = BitmapFactory.decodeByteArray(receivedAlbumArt, 0, receivedAlbumArt.length);
                }
                int songDuration = message.getSongDuration();

                try {
                    OutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(payloadReceived, 0, payloadReceived.length);
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(songTitle + "-------------------------");
                ClientManager clientManager1 = activeClients1.getClientManager(socket.getInetAddress().toString());
                Song song = new Song(songTitle, file.toString(), bitmap, songDuration, clientManager1.getClientName(), socket.getInetAddress().toString());
                PlaylistManager.getInstance().addRequestedSong(song);
                clientManager1.sendMessageToClient("Your song successfully added", MessageType.FILE_ADDED);
                break;

            default:
                System.out.println("INVALID");
                break;
        }
    }


}
