package com.crone.yandexmobdev.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crone.yandexmobdev.R;

public class ArtistViewHolder extends RecyclerView.ViewHolder{

    public ImageView artistPreview;
    public TextView artistName;
    public TextView artistGenres;
    public TextView artistSongs;

    public ArtistViewHolder(View itemView) {
        super(itemView);
        artistPreview = (ImageView)itemView.findViewById(R.id.artistPreview);
        artistName = (TextView)itemView.findViewById(R.id.artistName);
        artistGenres = (TextView)itemView.findViewById(R.id.artistGenre);
        artistSongs = (TextView)itemView.findViewById(R.id.artistSongs);
    }
}
