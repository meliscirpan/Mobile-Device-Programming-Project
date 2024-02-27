package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<Playlist> playlistList;
    private Context context;
    private Intent intent;
    private SharedPreferences sharedPreferences;

    public PlaylistAdapter(List<Playlist> playlistList, Context context, Intent playlistintent, SharedPreferences sharedPreferences){
        this.playlistList = playlistList;
        this.context = context;
        this.intent = playlistintent;
        this.sharedPreferences = sharedPreferences;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_song,parent,false);
        return new PlaylistViewHolder(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.playlistName.setText(playlist.getPlaylistName());
        String text = playlist.getSongList().size() + " Şarkı";
        holder.size.setText(text);
        holder.deleteButton.setOnClickListener(v -> {
            playlistList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,playlistList.size());
            sharedPreferences.edit().remove(playlist.getPlaylistName()).apply();
        });
        holder.itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, SongListActivity.class);
            intent.putExtra("playlist",playlist);
            context.startActivity(intent);
        });
        holder.songImage.setImageResource(R.mipmap.songicon);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private PlaylistAdapter adapter;
        private TextView playlistName;
        private TextView size;
        private Button shareButton;
        private Button deleteButton;
        private ConstraintLayout itemLayout;
        private ImageView songImage;
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.song_name);
            size = itemView.findViewById(R.id.owner_name);
            shareButton = itemView.findViewById(R.id.share_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            shareButton.setVisibility(View.INVISIBLE);
            itemLayout = itemView.findViewById(R.id.song_item);
            songImage = itemView.findViewById(R.id.song_image);
            itemView.findViewById(R.id.song_time).setVisibility(View.INVISIBLE);
        }

        public PlaylistViewHolder linkAdapter(PlaylistAdapter adapter){
            this.adapter = adapter;
            return this;
        }

    }

}
