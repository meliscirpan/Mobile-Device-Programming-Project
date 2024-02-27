package com.example.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{
    private List<Song> songsList;
    private Context context;
    private Intent intent;
    public RecyclerAdapter(List<Song> songsList, Context context, Intent songlistintent){
        this.songsList = songsList;
        this.context = context;
        this.intent = songlistintent;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView songName;
        private TextView ownerName;
        private TextView songTime;
        private ImageView songImage;
        private ConstraintLayout itemLayout;
        private Button deleteButton;
        private Button shareButton;

        private RecyclerAdapter adapter;

        public MyViewHolder linkAdapter(RecyclerAdapter adapter){
            this.adapter = adapter;
            return this;
        }
        public MyViewHolder(final View view) {
            super(view);
            songName = view.findViewById(R.id.song_name);
            ownerName = view.findViewById(R.id.owner_name);
            songTime = view.findViewById(R.id.song_time);
            songImage = view.findViewById(R.id.song_image);
            itemLayout = view.findViewById(R.id.song_item);
            deleteButton = view.findViewById(R.id.delete_button);
            shareButton = view.findViewById(R.id.share_button);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_song,parent,false);
        return new MyViewHolder(itemView).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        Song song = songsList.get(position);
        String song_uri_path = song.getPath();
        String song_name = song.getSongName();
        String owner_name = song.getOwnerName();
        String song_time = song.getSongTime();
        String song_file_path = song.getFilepath();
        holder.songName.setText(song_name);
        holder.ownerName.setText(owner_name);
        holder.songTime.setText(song_time);
        holder.songImage.setImageResource(R.mipmap.songicon);
        holder.itemLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context,PlaySongActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("current_song", song);
            bundle.putParcelableArrayList("songs_list", (ArrayList<? extends Parcelable>) songsList);
            bundle.putInt("current_song_index",position);
            intent.putExtras(bundle);
            context.startActivity(intent,bundle);
        });

        holder.shareButton.setOnClickListener(v -> {
            File sharedFile = new File(song_file_path);
            System.out.println(song_file_path);
            String type =  getMimeType(sharedFile.getName());
           //if (type != null){type="*/*";}
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"İşte senin için bir şarkı!");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri path = FileProvider.getUriForFile(context,"Serdem",sharedFile);
                intent.putExtra(Intent.EXTRA_STREAM,path);
                intent.setType(type);
                context.startActivity(intent);

            }catch (Exception e){
                Toast.makeText(context,"Bir Hata Oluştu", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemLayout.findViewById(R.id.delete_button).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Şarkıyı Sil");
            builder.setMessage("Bu şarkıyı silmek istediğinizden emin misiniz?");
            builder.setPositiveButton("Evet", (dialog, which) -> {
                Toast.makeText(context,"Şarkı Silindi",Toast.LENGTH_SHORT).show();
                deleteSong(position);
            });
            builder.setNegativeButton("Hayır", (dialog, which) -> {
                Toast.makeText(context,"Şarkı Silinmedi",Toast.LENGTH_SHORT).show();
            });
            builder.create();
            builder.show();
        });
    }

    private void deleteSong(int position) {
        try{
            File fileToDelete = new File(songsList.get(position).getPath());
            fileToDelete.setWritable(true);
            fileToDelete.setReadable(true);
            if(fileToDelete.exists()){
                fileToDelete.delete();
                try {
                    fileToDelete.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                fileToDelete = new File(songsList.get(position).getFilepath());
                fileToDelete.setWritable(true);
                fileToDelete.setReadable(true);
                if(fileToDelete.exists()){
                    fileToDelete.delete();
                    try {
                        fileToDelete.getCanonicalFile().delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("Başarılı");
                }
            }
            fileToDelete.deleteOnExit();
        }catch(Exception e){
            e.printStackTrace();
        }
        this.songsList.remove(position);
        this.notifyItemRemoved(position);
    }

    public static String getMimeType(String url) {
        int dot = url.lastIndexOf('.');
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(url.substring(dot + 1));
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

}
