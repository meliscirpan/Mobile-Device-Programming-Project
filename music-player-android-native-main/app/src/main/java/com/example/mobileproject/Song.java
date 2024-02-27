package com.example.mobileproject;

import android.os.Parcel;
import android.os.Parcelable;

class Song implements Parcelable {
    private String songName;
    private String ownerName;
    private String songTime;
    private String uripath;
    private String filepath;

    Song(String songName, String ownerName, String songTime,String uripath,String filepath) {
        this.songName = songName;
        this.ownerName = ownerName;
        this.songTime = songTime;
        this.uripath = uripath;
        this.filepath = filepath;
    }


    protected Song(Parcel in) {
        songName = in.readString();
        ownerName = in.readString();
        songTime = in.readString();
        uripath = in.readString();
        filepath = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongTime() {
        return songTime;
    }

    public void setSongTime(String songTime) {
        this.songTime = songTime;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getPath() {
        return uripath;
    }

    public void setPath(String uripath) {
        this.uripath = uripath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songName);
        dest.writeString(ownerName);
        dest.writeString(songTime);
        dest.writeString(uripath);
        dest.writeString(filepath);
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
