package com.example.musicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    File[] filesAndFolders;

    public FileAdapter(Context context, File[] filesAndFolders) {
        this.context = context;
        this.filesAndFolders = filesAndFolders;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FileViewHolder viewHolder = (FileViewHolder) holder;
        File selectedFile = filesAndFolders[position];
        viewHolder.textView.setText(selectedFile.getName());
        if (selectedFile.isDirectory()){
            viewHolder.imageView.setImageResource((R.drawable.ic_folder));
        }else {
            viewHolder.imageView.setImageResource((R.drawable.baseline_insert_drive_file_24));
        }
    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public FileViewHolder(View itemView) {
            super((itemView));
            textView = itemView.findViewById(R.id.file_name_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }
}
