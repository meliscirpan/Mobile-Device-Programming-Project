package com.example.mobileproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MenuActivity extends Activity {
    private Button songListButton;
    private Button playListButton;
    private Button exitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_screen);
        songListButton = findViewById(R.id.songs);
        playListButton = findViewById(R.id.playlists);
        exitButton = findViewById(R.id.exit);

        songListButton.setOnClickListener(v -> toSongs());
        playListButton.setOnClickListener(v -> toPlaylists());
        exitButton.setOnClickListener(v -> exit());

    }

    private void toSongs(){
        Intent intent = new Intent(MenuActivity.this,SongListActivity.class);
        MenuActivity.this.startActivity(intent);
    }

    private void toPlaylists(){
        Intent intent = new Intent(MenuActivity.this,PlaylistActivity.class);
        MenuActivity.this.startActivity(intent);
    }

    private void exit(){
        getSharedPreferences(PreferenceManager.getDefaultSharedPreferencesName(getApplicationContext()),MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(MenuActivity.this,MainActivity.class);
        MenuActivity.this.startActivity(intent);
    }
}
