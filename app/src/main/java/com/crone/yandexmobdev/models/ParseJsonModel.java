package com.crone.yandexmobdev.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by CRN_soft on 03.04.2016.
 */
public class ParseJsonModel implements Parcelable {
    public int id;
    public String name;
    public String urlcover;
    public String geners;
    public int tracks;
    public int albums;

    public ParseJsonModel(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(geners);
        dest.writeString(urlcover);
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeInt(albums);
        dest.writeInt(tracks);
    }
}
