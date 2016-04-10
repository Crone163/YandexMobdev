package com.crone.yandexmobdev.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import com.crone.yandexmobdev.R;
import com.crone.yandexmobdev.models.ParseJsonModel;
import com.crone.yandexmobdev.utils.ConstantManager;
import com.crone.yandexmobdev.utils.ConvertNumbers;

import com.crone.yandexmobdev.utils.MyAnimationUtils;
import com.crone.yandexmobdev.utils.downloadCMYKImage;
import com.crone.yandexmobdev.viewholders.ArtistViewHolder;



import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.widget.ImageView;


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
    public void onBindViewHolder( final ArtistViewHolder holder,final    int position) {
        // название исполнителя
        holder.artistName.setText(mParse.get(position).name);
        // жанры
        holder.artistGenres.setText(mParse.get(position).geners);
        // загружаем картинку в ImageView
        Picasso.with(holder.artistPreview.getContext()).load(mParse.get(position).urlcover)
                .fit()
                .centerCrop()
                .into(holder.artistPreview, new Callback() {

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        // Сначало Picasso парсит картинку CMYK, спустя время он должен понять что не может декодировать картинку
                        if (Build.VERSION.SDK_INT <= 17) {
                            new downloadCMYKImage(holder.artistPreview).execute(mParse.get(position).urlcover);
                        }
                    }
                });
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
