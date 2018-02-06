package com.everyonedj.client;

import android.content.AsyncTaskLoader;
import android.os.AsyncTask;

import com.everyonedj.common.Message;
import com.everyonedj.common.MessageFailedException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class will be used for sending message object to the server.
 * Created by Siddharth on 7/16/2015.
 */
public class ServerCommunicationManager {

    private static ServerCommunicationManager instance;
    private Socket socket;

    private ServerCommunicationManager() {
    }


    public static ServerCommunicationManager getInstance() {
        if(instance == null) {
            instance = new ServerCommunicationManager();
        }
        return instance;
    }

    public void setSocket(Socket sock) {
        this.socket = sock;
    }

    public void sendMessageToServer(Message message) throws MessageFailedException {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            throw new MessageFailedException();
        }
    }


}
