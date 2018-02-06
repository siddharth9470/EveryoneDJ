package com.everyonedj.common;

import com.everyonedj.server.ClientManager;

import java.util.HashMap;

/**
 * Created by Siddharth on 7/15/2015.
 */
public class ActiveClients {

    private static ActiveClients instance;
    private HashMap<String, ClientManager> allActiveClients;

    private ActiveClients() {
        allActiveClients = new HashMap<String, ClientManager>();
    }

    public static ActiveClients getInstance() {
        if (instance == null) {
            instance = new ActiveClients();
        }
        return instance;
    }

    public void addClient(String ip, ClientManager manager) {
        allActiveClients.put(ip, manager);
    }

    public void removeClient(String ip) {
        allActiveClients.remove(ip);
    }

    public ClientManager getClientManager(String ip) {
        return allActiveClients.get(ip);
    }
}
