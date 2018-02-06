package com.everyonedj.server;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.everyonedj.R;
import com.everyonedj.common.BaseActivity;
import com.everyonedj.common.Constants;
import com.everyonedj.common.TypeFaceSpan;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.Tracker;

import java.util.concurrent.TimeUnit;


public class MainActivity extends BaseActivity implements SongEventListener {

    AudioPlayerThread player = AudioPlayerThread.getInstance();
    PlaylistManager playlistManager = PlaylistManager.getInstance();
    SeekBar mSeekBar;
    ImageView backwardButton, forwardButton, nextButton, previousButton;
    private ToggleButton play_pause;
    int finalTime = 0;
    int startTime = 0;
    Handler mHandler = new Handler();
    public static int oneTimeOnly = 0;
    TextView startSongTime, finalSongTime, playing, currentSongPLaying;
    private BroadcastReceiver onPlayStartBroadcastReceiver;
    private EasyTracker easyTracker = null;
    private PagerTabStrip pagerTabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activateToolbar();
        ViewPager viewPager = (ViewPager) findViewById(R.id.pagerView);
        viewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);
        setTypeFaceToPagerTab();
        playlistManager.registerListener(this);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        backwardButton = (ImageView) findViewById(R.id.backWard);
        play_pause = (ToggleButton) findViewById(R.id.play_pause);
        forwardButton = (ImageView) findViewById(R.id.forward);
        startSongTime = (TextView) findViewById(R.id.startTime);
        finalSongTime = (TextView) findViewById(R.id.finalTime);
        nextButton = (ImageView) findViewById(R.id.next);
        previousButton = (ImageView) findViewById(R.id.previous);
        currentSongPLaying = (TextView) findViewById(R.id.currentSongPlaying);
        playing = (TextView) findViewById(R.id.playing);
        player.registerListener(this);
        googleAnalytics();
        mSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ActionBar actionBar = getSupportActionBar();
        SpannableString s = new SpannableString(Html.fromHtml("<font color='#FFFFFF'>EveryoneDJ</font>"));
        s.setSpan(new TypeFaceSpan(this, "FuturaLT-Book.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(s);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
        Tracker tracker = EasyTracker.getInstance(this);
        tracker.set(Fields.SCREEN_NAME, "Server Running");
        tracker.send(MapBuilder.createAppView().build());
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddSongsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:

                Intent toConnectedClients = new Intent(this, ConnectedClientsActivity.class);
                startActivity(toConnectedClients);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSongAdded(String songTitle) {

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

    @Override
    protected void onResume() {
        if (player.isMediaPlayerPlaying()) {
            play();
            currentSongPLaying.setText(Constants.currentSongPlaying);
        }
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ON_PLAY_START);
        onPlayStartBroadcastReceiver = new OnPlayStartBroadcastReceiver();
        getApplicationContext().registerReceiver(onPlayStartBroadcastReceiver, intentFilter);
        super.onResume();
    }

    @Override
    public void onSongChanged() {
        play();
        currentSongPLaying.setText(Constants.currentSongPlaying);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(onPlayStartBroadcastReceiver);
    }

    private class OnPlayStartBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ((intent.getAction() != null) && Constants.ON_PLAY_START.equals(intent.getAction())) {
                play();
                currentSongPLaying.setText(Constants.currentSongPlaying);
            }
        }
    }

    public class FragmentAdapter extends FragmentPagerAdapter {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ServerPlaylistSongs();
            } else {
                return new ClientSongPlaylist();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Your Playlist";
            } else {
                return "Requested Playlist";
            }
        }
    }

    public void play() {
        playing.setVisibility(View.VISIBLE);
        play_pause.setChecked(true);
        play_pause.setClickable(true);
        finalTime = player.mediaFinalTime();
        startTime = player.mediaStartTime();
        if (oneTimeOnly == 0) {
            mSeekBar.setMax((int) finalTime);
        }
        mSeekBar.setProgress((int) startTime);
        mSeekBar.setClickable(true);

        startSongTime.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        finalSongTime.setText(String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );
        mHandler.postDelayed(updateSongTime, 100);

        backwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.backward();
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.forward();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.nextSong();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.previousSong();
            }
        });

        play_pause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    player.play();
                    Constants.isMediaPlayerPause = false;
                } else {
                    player.pause();
                    Constants.isMediaPlayerPause = true;
                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekBar(progress);
                    mSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            startTime = player.mediaStartTime();
            startSongTime.setText(String.format("%02d:%02d",

                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            mSeekBar.setProgress((int) startTime);
            mHandler.postDelayed(this, 100);
        }
    };

    private void googleAnalytics() {
        easyTracker = EasyTracker.getInstance(MainActivity.this);
        easyTracker.send(MapBuilder.createEvent("your_action",
                "envet_name", "button_name/id", null).build());
    }

    private void setTypeFaceToPagerTab() {
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/FuturaLT-Book.ttf");
        for (int i = 0; i < pagerTabStrip.getChildCount(); ++i) {
            View nextChild = pagerTabStrip.getChildAt(i);
            if (nextChild instanceof TextView) {
                TextView textViewToConvert = (TextView) nextChild;
                textViewToConvert.setTypeface(face);
            }
        }
    }
}
