package com.example.mobileproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Playlist implements Parcelable {
    private String playlistName;
    private List<Song> songList;

    public Playlist(String playlistName, List<Song> songList) {
        this.playlistName = playlistName;
        this.songList = songList;
    }

    public Playlist() {
        this.songList = new ArrayList<>();
    }

    protected Playlist(Parcel in) {
        playlistName = in.readString();
        songList = in.createTypedArrayList(Song.CREATOR);
    }

    public void addSong(Song song) {
        songList.add(song);
    }

    public void removeSong(Song song) {
        songList.remove(song);
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(playlistName);
        parcel.writeTypedList(songList);
    }
}
