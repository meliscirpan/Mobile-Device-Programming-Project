package com.example.mobileproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistActivity extends AppCompatActivity {

    private RecyclerView playlistRecyclerView;
    private List<Playlist> playlistList;
    private FloatingActionButton addPlayListButton;
    private ArrayList<Song> songsList;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        gson = new Gson();
        songsList = new ArrayList<>();
        playlistRecyclerView = findViewById(R.id.playlist_list);
        addPlayListButton = findViewById(R.id.new_playlist);
        playlistList = getSharedPreferences("playlistPreference", MODE_PRIVATE)
                .getAll()
                .values()
                .stream()
                .map(playlist -> gson.fromJson(playlist.toString(), Playlist.class))
                .collect(Collectors.toList());
        File musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        getSongs(musicFolder.getAbsolutePath() + "/");
        addPlayListButton.setOnClickListener(addPlaylist);
        setAdapter();
    }

    private final View.OnClickListener addPlaylist = v -> {
        View alertDialogText = getLayoutInflater().inflate(R.layout.alert_dialog_text, null);
        EditText playlistName = alertDialogText.findViewById(R.id.alert_dialog_edit_text);
        String[] songNames =  songsList.stream().map(Song::getSongName).toArray(String[]::new);
        Playlist playlist = new Playlist();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yeni Çalma Listesi");
        builder.setView(alertDialogText);
        builder.setMultiChoiceItems(songNames, null, (dialog, which, isChecked) -> {
            if (isChecked) {
                playlist.addSong(songsList.get(which));
            } else {
                playlist.removeSong(songsList.get(which));
            }
        });
        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            playlist.setPlaylistName(playlistName.getText().toString());
            playlistList.add(playlist);
            getSharedPreferences("playlistPreference", MODE_PRIVATE)
                    .edit()
                    .putString(playlistName.getText().toString(), gson.toJson(playlist))
                    .apply();
            setAdapter();
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
    };

    ArrayList<HashMap<String, String>> getSongs(String rootPath) {
        ArrayList<HashMap<String, String>> fileList = new ArrayList<>();
        try {
            File rootFolder = new File(rootPath);
            File[] files = rootFolder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (getSongs(file.getAbsolutePath()) != null) {
                        fileList.addAll(getSongs(file.getAbsolutePath()));
                    } else {
                        break;
                    }
                } else if (file.getName().endsWith(".mp3")) {
                    Uri uri = Uri.fromFile(file);
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(this, uri);
                    String songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    String album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    String genre = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    String track = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS);
                    String time = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    int durationOfCurrentSong = Integer.parseInt(time) / 1000;
                    int minute = durationOfCurrentSong / 60;
                    int seconds = durationOfCurrentSong % 60;
                    String secondsString = Integer.toString(seconds);
                    if (secondsString.length() < 2) {
                        secondsString = "0" + secondsString;
                    }
                    time = Integer.toString(minute) + ":" + secondsString;
                    songsList.add(new Song(songName, artist, time, uri.toString(), file.getPath()));
                }
            }
            songsList.sort(Comparator.comparing(Song::getSongName));
            return fileList;
        } catch (Exception e) {
            System.out.println("Get Song Exception " + e);
            return null;
        }
    }

    private void setAdapter() {
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(playlistList, this, getIntent(), getSharedPreferences("playlistPreference", MODE_PRIVATE));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        playlistRecyclerView.setAdapter(playlistAdapter);
        playlistRecyclerView.setLayoutManager(layoutManager);
        playlistRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}