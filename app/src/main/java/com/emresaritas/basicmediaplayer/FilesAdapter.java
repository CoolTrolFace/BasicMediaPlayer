package com.emresaritas.basicmediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private ArrayList<AudioFile> fileDatas;
    private LayoutInflater layoutInflater;
    private FileExploreActivity exploreActivity;
    private Context context;

    FilesAdapter(Context context, FileExploreActivity exploreActivity, String path){
        this.layoutInflater=LayoutInflater.from(context);
        this.context=context;
        fileDatas = new ArrayList<>();

        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            fileDatas.add(new AudioFile(files[i].getPath()));
        }
        this.exploreActivity=exploreActivity;
    }

    @Override
    public FilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_file, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FilesAdapter.ViewHolder holder, int position) {
        AudioFile newItem = fileDatas.get(position);

        if(new File(newItem.getPath()).isDirectory()){
            holder.tName.setText(newItem.getPath().substring(newItem.getPath().lastIndexOf("/")+1));
            holder.tArtist.setText(null);
            holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.image_folder));
        }else{
            String type = MimeTypeMap.getFileExtensionFromUrl(newItem.getPath().replace(" ",""));
            if(type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("mp4")){
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(newItem.getPath());
                if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) == null){
                    holder.tName.setText(newItem.getPath().substring(newItem.getPath().lastIndexOf("/")+1));
                }else{
                    holder.tName.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                }
                holder.tArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
                if(mmr.getEmbeddedPicture()!=null){
                    holder.image.setImageBitmap(BitmapFactory.decodeByteArray(mmr.getEmbeddedPicture(), 0, mmr.getEmbeddedPicture().length));
                } else
                    holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.image_file));
            }else{
                holder.tArtist.setText(null);
                holder.tName.setText(newItem.getPath().substring(newItem.getPath().lastIndexOf("/")+1));
                holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.image_file));
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new File(newItem.getPath()).isDirectory()){
                    Intent intent = new Intent(context, FileExploreActivity.class);
                    intent.putExtra("path",newItem.getPath());
                    context.startActivity(intent);
                    exploreActivity.finish();
                }else{
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("path",newItem.getPath());
                    ArrayList<String> mediasPaths = new ArrayList<>();
                    for(AudioFile file: fileDatas){
                        String type = MimeTypeMap.getFileExtensionFromUrl(file.getPath().replace(" ",""));
                        if(type.equalsIgnoreCase("mp3") || type.equalsIgnoreCase("mp4")){
                            mediasPaths.add(file.getPath());
                        }
                    }
                    intent.putStringArrayListExtra("medias",mediasPaths);
                    context.startActivity(intent);
                    exploreActivity.finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView tArtist;
        TextView tName;
        ConstraintLayout layout;
        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.constraint);
            tArtist =  itemView.findViewById(R.id.textFileArtist);
            tName = (TextView) itemView.findViewById(R.id.textFileName);
            image = (ImageView) itemView.findViewById(R.id.imageFile);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus)
                        focused();
                    else
                        unFocused();
                }
            });
        }

        public void focused(){
            this.layout.setBackgroundColor(Color.parseColor("#FF00A7D1"));
        }
        public void unFocused(){
            this.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        @Override
        public void onClick(View v) {

        }
    }
}
