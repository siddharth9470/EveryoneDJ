package com.everyonedj.server;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.everyonedj.R;
import com.everyonedj.common.BaseActivity;
import com.everyonedj.common.Song;
import com.everyonedj.common.TypeFaceSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectedClientsActivity extends BaseActivity {

    private ListView connectedClients;
    private List<Song> allClient = new ArrayList<>();
    PlaylistManager playlistManager = PlaylistManager.getInstance();
    MyAdapter adapter = new MyAdapter();
    private TextView noClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_clients);
        activateToolbar();
        connectedClients = (ListView) findViewById(R.id.allClients);
        allClient = playlistManager.connectedClients;
        notifyDataChanged();
        connectedClients.setAdapter(adapter);
        noClient = (TextView) findViewById(R.id.noClientsConnected);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connected_clients, menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        SpannableString s = new SpannableString(Html.fromHtml("<font color='#FFFFFF'>List of clients</font>"));
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allClient.size();
        }

        @Override
        public Object getItem(int position) {
            return allClient.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.connected_clients, null);
            TextView textView = (TextView) view.findViewById(R.id.activeClients);
            textView.setText(allClient.get(position).getClientName());
            return view;
        }
    }

    public void notifyDataChanged() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(playlistManager.connectedClients != null && playlistManager.connectedClients.size() > 0) {
                            noClient.setVisibility(View.INVISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }, 0, 1000);
    }



}
