package com.everyonedj.client;

import android.content.Context;
import android.content.Intent;

import com.everyonedj.common.Constants;
import com.everyonedj.common.Message;
import com.everyonedj.common.MessageType;
import com.everyonedj.server.SongEventListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Siddharth on 7/14/2015.
 */
public class ClientIncomingMessageHandler {

    private Socket socket;
    private StartSongSendingInterface sListener = null;

    public ClientIncomingMessageHandler(StartSongSendingInterface listener, Socket socket) {
        this.socket = socket;
        sListener = listener;
    }

    public void readingObjectFromServer() throws IOException {

        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            Message messageObject = (Message) inputStream.readObject();
            messageHandler(messageObject);
        } catch (IOException e) {
            socket.close();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            socket.close();
            e.printStackTrace();
        }
    }

    public void messageHandler(Message message) {
        int messageType = message.getMessageType();

        switch (messageType) {
            case MessageType.REGISTER_ACKNOWLEDGE:
                System.out.println(message.getMessageContent() + " YOU ARE SUCCESSFULLY REGISTERED");
                sListener.onStartSongListener();
                break;

            case MessageType.FILE_ADDED:
                System.out.println(message.getMessageContent());
        }
    }
}