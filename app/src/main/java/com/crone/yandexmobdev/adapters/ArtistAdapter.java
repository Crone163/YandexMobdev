package com.crone.yandexmobdev.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import com.crone.yandexmobdev.R;
import com.crone.yandexmobdev.models.ParseJsonModel;
import com.crone.yandexmobdev.utils.ConvertNumbers;
import com.crone.yandexmobdev.utils.MyAnimationUtils;
import com.crone.yandexmobdev.viewholders.ArtistViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistViewHolder> {

    private ArrayList<ParseJsonModel> mParse;
    private int mPreviousPosition = 0;

    public ArtistAdapter(ArrayList<ParseJsonModel> parseJsonModels) {
        this.mParse = parseJsonModels;
    }


    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_artists_elements, parent, false);
        ArtistViewHolder viewHolder = new ArtistViewHolder(view);
        // для того чтобы проверить сохранилась ли картинка в кеше, используем параметр setIndicatorsEnabled,
        // синий треугольник на картинке показывает, что она была загруженна только что, зеленый - взята из кэша.
        //Picasso.with(view.getContext()).setIndicatorsEnabled(true);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        // название исполнителя
        holder.artistName.setText(mParse.get(position).name);
        // жанры
        holder.artistGenres.setText(mParse.get(position).geners);
        //преобразовываем ссылку на cover в URI
        Uri uri = Uri.parse(mParse.get(position).urlcover);
        // получаем контекст для Picasso
        Context context = holder.artistPreview.getContext();
        // загружаем картинку в ImageView
        Picasso.with(context).load(uri).into(holder.artistPreview);
        // альбомы + артисты
        ConvertNumbers convert = new ConvertNumbers();
        // конвертация склонения песен
        String songs = convert.convertSongs(mParse.get(position).tracks);
        // конвертация склонения альбомов
        String albums = convert.convertAlbums(mParse.get(position).albums);
        holder.artistSongs.setText(albums + ", " + songs);
        // анимация при скролинге
        if (position > mPreviousPosition) {
            MyAnimationUtils.animateSunblind(holder, true);
        } else {
            MyAnimationUtils.animateSunblind(holder, false);
        }
        mPreviousPosition = position;

    }


    @Override
    public int getItemCount() {
        if (mParse != null) {
            return mParse.size();
        }
        return 0;
    }
}
