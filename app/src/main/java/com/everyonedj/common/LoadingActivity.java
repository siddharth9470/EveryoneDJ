package com.everyonedj.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.everyonedj.client.ClientActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.everyonedj.R;
import com.everyonedj.server.MainActivity;
import com.everyonedj.server.ServerActivity;


public class LoadingActivity extends Activity {

    WifiManager wifiManager;
    private boolean isServerRunning = false;
    private int backpress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_loading);
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled() || !isInternetConnected()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertBuilder();
                }
            }, 1000);
        }
        new Thread(new GettingIPThread()).start();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isServerRunning == true) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_loading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    public void alertBuilder() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("To use this app your wifi should be turned on or connected to internet, do you want to turn on wifi?");
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                turnWifiOn();

            }
        });
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "Switch on your wifi and then try again", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void turnWifiOn() {
        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(new wifi(), intentFilter);

    }

    private boolean isInternetConnected() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private class wifi extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()) {
                    Intent bringToForegroundIntent = new Intent(context, LoadingActivity.class);
                    bringToForegroundIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(bringToForegroundIntent);
                }
            }
        }
    }

    /**
     * This thread is checking if Multicasting is already happening. If it is then server is already running
     * and only clients can be started now
     */


    class GettingIPThread implements Runnable {

        @Override
        public void run() {
           // WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicastLock");
            multicastLock.setReferenceCounted(true);
            multicastLock.acquire();

            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(Constants.INET_ADDR);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            byte[] bytes = new byte[1024];
            try (MulticastSocket clientSocket = new MulticastSocket(Settings.PORT)) {
                clientSocket.setSoTimeout(1000);
                clientSocket.joinGroup(inetAddress);
                boolean gettingIP = true;
                DatagramPacket msgPacket = null;
                while (gettingIP) {
                    msgPacket = new DatagramPacket(bytes, bytes.length);
                    clientSocket.receive(msgPacket);
                    System.out.println("Server multicasting receiving - ip : " + msgPacket.getAddress());
                    if (msgPacket != null) {
                        final DatagramPacket finalMsgPacket = msgPacket;
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getApplicationContext(), ClientActivity.class);
                                intent.putExtra(Constants.SERVER_IP, finalMsgPacket.getAddress());
                                startActivity(intent);
                                finish();
                            }
                        }, 1000);
                    }
                    gettingIP = false;
                }
            } catch (SocketTimeoutException ste) {
                // Socket timed out. It means server is not running. Lets start the server now.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), ServerActivity.class);
                        startActivity(intent);
                        isServerRunning = true;
                        finish();
                    }
                }, 1000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
