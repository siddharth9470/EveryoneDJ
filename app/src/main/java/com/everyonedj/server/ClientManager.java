package com.everyonedj.server;

import com.everyonedj.common.ActiveClients;
import com.everyonedj.common.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Siddharth on 7/15/2015.
 */

public class ClientManager implements Runnable {

    private Socket clientSocket;
    private String ip;
    private String clientName;

    public ClientManager(Socket socket) {
        this.clientSocket = socket;
        ip = clientSocket.getInetAddress().toString();
    }

    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            try {
                ServerIncomingMessageHandler incomingMessageHandler = new ServerIncomingMessageHandler(clientSocket);
                incomingMessageHandler.readingObjectFromClients();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // when client disconnect remove it from map
        ActiveClients activeClients = ActiveClients.getInstance();
        activeClients.removeClient(ip);
        System.out.print("CLIENT REMOVED" + " -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");

    }
    public void sendMessageToClient(String message, int messageType) {

        Message messageObject = new Message();
        messageObject.setMessageContent(message);
        messageObject.setMessageType(messageType);

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.writeObject(messageObject);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("---------------------------------------MSG SEND TO CLIENT");
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}
