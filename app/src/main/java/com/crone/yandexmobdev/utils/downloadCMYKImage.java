package com.crone.yandexmobdev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by CRN_soft on 07.04.2016.
 */
public class downloadCMYKImage extends AsyncTask<String, ImageView, Bitmap> {

    private ImageView mImageView;
    private Context mContext;
    private getCMYKImage get;

    public downloadCMYKImage(ImageView s) {
        this.mImageView = s;
        this.mContext = mImageView.getContext();

    }

    protected Bitmap doInBackground(String... urls) {
        get = new getCMYKImage(mContext);
        Bitmap bitmap = get.getCMYKImageFromURL(urls[0]);
        return bitmap;
    }

    // если изображение не null, то это преобразованный CMYK, копируем в кэш с префиксом,
    // потом пикассо легко подключется к конвертированной картинке
    protected void onPostExecute(Bitmap bmp) {

        File file = new File(get.getSavingPath() + ConstantManager.RGB_PREFIX);
        if (file.exists()) {
            Picasso.with(mContext).load(file).fit().centerCrop().into(mImageView);
        } else {
            if (bmp != null) {
                mImageView.setImageBitmap(bmp);
                try {
                    Bitmap bitmap = bmp;
                    FileOutputStream stream = new FileOutputStream(new File(get.getSavingPath()) + ConstantManager.RGB_PREFIX);
                    if (bitmap != null)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // избавляемся от CMYK
                File deletefile = new File(get.getSavingPath());
                deletefile.delete();
            }
        }


    }


}