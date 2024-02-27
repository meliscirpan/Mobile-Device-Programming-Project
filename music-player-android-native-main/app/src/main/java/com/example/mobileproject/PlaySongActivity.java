package com.example.mobileproject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Locale;

public class PlaySongActivity extends AppCompatActivity {
    TextView title;
    TextView artist;
    TextView currentTime;
    ImageButton previous;
    ImageButton stop;
    ImageButton resume;
    ImageButton next;
    ImageView songImage;
    SeekBar bar;
    Bundle extras;
    Song currentSong;
    ArrayList<Song> songsList;
    Drawable defaultIcon;
    int currentSongIndex;
    int length = 0;
    MediaPlayerService mediaPlayerService;
    boolean isBound = false;
    private final Handler mHandler = new Handler();
    private final SongChangedReceiver songChangedReceiver = new SongChangedReceiver();

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(updateTimeTask);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) iBinder;
            mediaPlayerService = binder.getService();
            playSong();
            startForegroundService(new Intent(getApplicationContext(), MediaPlayerService.class));
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mediaPlayerService = null;
            isBound = false;
        }
    };

    private void bindService() {
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_song);
        extras = getIntent().getExtras();
        currentSong = extras.getParcelable("current_song");
        songsList = extras.getParcelableArrayList("songs_list");
        currentSongIndex = extras.getInt("current_song_index");
        System.out.println(currentSong.getSongTime());
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        currentTime = findViewById(R.id.current_time);
        previous = findViewById(R.id.previous);
        stop = findViewById(R.id.stop);
        resume = findViewById(R.id.resume);
        next = findViewById(R.id.next);
        bar = findViewById(R.id.seek_bar);
        songImage = findViewById(R.id.song_icon);
        defaultIcon = songImage.getBackground();
        setSongInfo();
        bindService();

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    length = mediaPlayerService.getCurrentPosition();
                    mediaPlayerService.onResume(progress * 1000);
                }
                System.out.println("Current progress = " + Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(updateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.postDelayed(updateTimeTask, 100);
            }
        });
        previous.setOnClickListener(v -> previousSong());
        stop.setOnClickListener(v -> stopSong());
        resume.setOnClickListener(v -> resumeSong());
        next.setOnClickListener(v -> nextSong());

        mHandler.postDelayed(updateTimeTask, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("SONG_CHANGED");
        registerReceiver(songChangedReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(songChangedReceiver);
    }

    private void playSong() {
        mediaPlayerService.stopService();
        mediaPlayerService.onPlay(currentSong.getPath(), songsList, currentSongIndex);
        bar.setMax(mediaPlayerService.getDuration() / 1000);
        bar.setProgress(0);
    }

    private void previousSong() {
        length = 0;
        if (currentSongIndex - 1 < 0) {
            currentSongIndex = songsList.size() - 1;
        } else {
            currentSongIndex -= 1;
        }
        currentSong = songsList.get(currentSongIndex);
        setSongInfo();
        mediaPlayerService.onPrevious(currentSong.getPath(), currentSongIndex);
        bar.setMax(mediaPlayerService.getDuration() / 1000);
        bar.setProgress(0);
    }

    private void stopSong() {
        mediaPlayerService.onStop();
        length = mediaPlayerService.getCurrentPosition();
    }

    private void resumeSong() {

        mediaPlayerService.onResume(length);
    }

    private void nextSong() {
        length = 0;
        if (currentSongIndex + 1 == songsList.size()) {
            currentSongIndex = 0;
        } else {
            currentSongIndex += 1;
        }
        currentSong = songsList.get(currentSongIndex);
        setSongInfo();
        mediaPlayerService.onNext(currentSong.getPath(), currentSongIndex);
        bar.setMax(mediaPlayerService.getDuration() / 1000);
        bar.setProgress(0);
    }

    private void setSongInfo() {
        title.setText(currentSong.getSongName());
        artist.setText(currentSong.getOwnerName());
        currentTime.setText(currentSong.getSongTime());
        Bitmap songCover = null;
        try {
            songCover = ThumbnailUtils.createVideoThumbnail(currentSong.getFilepath(), MediaStore.Images.Thumbnails.MINI_KIND);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (songCover != null) {
            songImage.setBackground(null);
            int targetWidth = 226;
            int targetHeight = 251;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(songCover, targetWidth, targetHeight, false);
            songCover.recycle();
            songImage.setImageBitmap(scaledBitmap);
        } else {
            songImage.setBackground(defaultIcon);
        }

    }

    private final Runnable updateTimeTask = new Runnable() {
        public void run() {
            int currentPosition = mediaPlayerService.getCurrentPosition();
            currentTime.setText(formatTime(currentPosition));
            bar.setProgress(currentPosition / 1000);

            mHandler.postDelayed(this, 100);
        }
    };

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;

        return String.format(Locale.getDefault(), "%02d:%02d", minutes % 60, seconds % 60);
    }

    public class SongChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SONG_CHANGED")) {
                currentSongIndex = intent.getIntExtra("current_song_index", 0);
                currentSong = songsList.get(currentSongIndex);
                setSongInfo();
            }
        }
    }

}
