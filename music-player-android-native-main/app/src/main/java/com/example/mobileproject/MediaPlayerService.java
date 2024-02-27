package com.example.mobileproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class MediaPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private List<Song> songsList;
    private int currentSongIndex;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MediaPlayerBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            String mediaUrl = intent.getStringExtra("MEDIA_URL");
            mediaPlayer = MediaPlayer.create(this, Uri.parse(mediaUrl));
            mediaPlayer.setOnPreparedListener(v -> onPrepared());
            mediaPlayer.setOnCompletionListener(v -> onCompletion());
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        NotificationChannel ntf = new NotificationChannel("1", "1", NotificationManager.IMPORTANCE_NONE);
        ntf.setLightColor(Color.BLUE);
        ntf.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(ntf);
        Notification notification = new NotificationCompat.Builder(this, "1")
                .setContentTitle("Music Player")
                .setContentText("Playing")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker("Music Player")
                .build();

        startForeground(1,notification);
        super.onCreate();
    }

    public void onCompletion() {
        if (currentSongIndex < songsList.size() - 1) {
            currentSongIndex++;
        } else {
            currentSongIndex = 0;
        }
        onNext(songsList.get(currentSongIndex).getPath(), currentSongIndex);
        notifySongChanged(currentSongIndex);
    }

    private void notifySongChanged(int currentSongIndex) {
        Intent intent = new Intent("SONG_CHANGED");
        intent.putExtra("current_song_index", currentSongIndex);
        sendBroadcast(intent);
    }

    public void onPrepared() {
        mediaPlayer.start();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    public void onPlay(String path, List<Song> songsList, int position) {
        if (path == null) {
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.currentSongIndex = position;
        this.songsList = songsList;
        mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
        mediaPlayer.setOnCompletionListener(v -> onCompletion());
        mediaPlayer.setOnPreparedListener(v -> onPrepared());
        mediaPlayer.start();
    }

    private void changeSong(String path) {
        mediaPlayer.release();
        mediaPlayer = null;
        mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
        mediaPlayer.setOnCompletionListener(v -> onCompletion());
        mediaPlayer.setOnPreparedListener(v -> onPrepared());
        mediaPlayer.start();
    }

    public void onResume(int position) {
        if (position > 0) {
            mediaPlayer.seekTo(position);
        }

        mediaPlayer.start();
    }

    public void onStop() {
        mediaPlayer.pause();
    }

    public void onNext(String path, int position) {
        this.currentSongIndex = position;
        changeSong(path);
    }

    public void onPrevious(String path, int position) {
        this.currentSongIndex = position;
        changeSong(path);
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void stopService(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
    }

    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
