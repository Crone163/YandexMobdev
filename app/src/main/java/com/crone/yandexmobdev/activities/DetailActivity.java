package com.crone.yandexmobdev.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crone.yandexmobdev.R;
import com.crone.yandexmobdev.utils.ConstantManager;
import com.crone.yandexmobdev.utils.ConvertNumbers;

import com.crone.yandexmobdev.utils.ImageTransformation;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;

import com.squareup.picasso.Picasso;


/**
 * Created by CRN_soft on 04.04.2016.
 */
public class DetailActivity extends AppCompatActivity {

    private String mBigIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String title = intent.getStringExtra(ConstantManager.NAME_DETAIL);
        String genres = intent.getStringExtra(ConstantManager.GENRES_DETAIL);

        TextView genresText = (TextView) findViewById(R.id.genresText);
        if (genresText != null)
            genresText.setText(genres);

        ConvertNumbers convert = new ConvertNumbers();
        // конвертация склонения песен
        String songs = convert.convertSongs(intent.getIntExtra(ConstantManager.TRACKS_DETAIL, 0));
        // конвертация склонения альбомов
        String albums = convert.convertAlbums(intent.getIntExtra(ConstantManager.ALBUMS_DETAIL, 0));

        TextView tracksAndAlbums = (TextView) findViewById(R.id.tracksAText);
        tracksAndAlbums.setText(albums + "  •  " + songs);

        TextView desc = (TextView)findViewById(R.id.biographyContent);
        if(desc != null)
        desc.setText(intent.getStringExtra(ConstantManager.DESC_DETAIL));


        mBigIcon = intent.getStringExtra(ConstantManager.BIGICON_DETAIL);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

    }

    // для трансформации bitmap, нужно определить размеры ImageView, их можно найти в этом классе, после прорисовки
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ImageView bigIcon = (ImageView) findViewById(R.id.bigImage);
        // при повороте экрана Picasso хранит картинку в кэше и поэтому возникала сложность с отрисовкой,
        // пришлось убрать картинку из памяти memoryPolicy(MemoryPolicy.NO_CACHE)
        Picasso.with(this)
                .load(mBigIcon)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .transform(ImageTransformation.getTransformation(bigIcon))
                .into(bigIcon, new Callback() {
                    @Override
                    public void onSuccess() {
                        setInvisibleLoading();
                    }

                    @Override
                    public void onError() {
                        setInvisibleLoading();
                    }
                });
    }

    private void setInvisibleLoading() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadingImageDetail);
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

}
