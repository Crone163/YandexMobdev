package com.crone.yandexmobdev.utils;


import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import magick.ColorspaceType;
import magick.ImageInfo;
import magick.MagickImage;
import magick.util.MagickBitmap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Get images with CMYK colorspace (Images not supported by Android)
 *
 * @author Mario Velasco Casqueo "@MVelascoC"
 *         Date: 11/12/2013
 */
public class getCMYKImage {

    private Context mContext;

    public getCMYKImage(Context context) {
        this.mContext = context;
    }

    private String SAVING_PATH;

    /**
     * Get a drawable object of an image through a path to that image.
     *
     * @param path Could be an internal or external path (Cache)
     * @return drawable image to display (Bitmap)
     */
    public Bitmap getCMYKImageFromPath(String path) {
        Bitmap bitmap = null;
        try {
            ImageInfo info = new ImageInfo(path); // where the CMYK image is
            MagickImage imageCMYK = new MagickImage(info);
            boolean status = imageCMYK.transformRgbImage(ColorspaceType.CMYKColorspace);
            bitmap = MagickBitmap.ToBitmap(imageCMYK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Get a drawable object of an image through a URL
     *
     * @param urlString URL of the file to be downloaded
     * @return drawable image to display (Bitmap)
     */
    public Bitmap getCMYKImageFromURL(String urlString) {
        String name = urlString;
        name = name.substring(name.length() - 24, name.length() - 8);
        SAVING_PATH = mContext.getCacheDir() + "/" + name;
        File file = new File(SAVING_PATH);
        if (new File(SAVING_PATH + ConstantManager.RGB_PREFIX).exists()) {
            return null;
        }
        Bitmap bitmap = null;
        if (file.exists()) {
            bitmap = getCMYKImageFromPath(SAVING_PATH);
        } else {
            if (netCheckin()) {
                downloadFile(urlString, SAVING_PATH);
                bitmap = getCMYKImageFromPath(SAVING_PATH);
            }
        }

        return bitmap;
    }

    public String getSavingPath() {
        return SAVING_PATH;
    }

    /**
     * Download file from URL and save it in local
     *
     * @param urlString  URL of the file to be downloaded
     * @param savingPath where the file will be saved, internal or external path
     */
    private void downloadFile(String urlString, String savingPath) {
        try {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(savingPath); // save file here
            FileOutputStream fileOutput = new FileOutputStream(file);
            if (urlConnection != null) {
                InputStream inputStream = urlConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
            }
            fileOutput.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean netCheckin() {
        try {
            ConnectivityManager nInfo = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }


}
