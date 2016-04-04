package com.crone.yandexmobdev.fragments;


import android.content.Intent;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.crone.yandexmobdev.R;
import com.crone.yandexmobdev.activities.DetailActivity;
import com.crone.yandexmobdev.adapters.ArtistAdapter;
import com.crone.yandexmobdev.models.ParseJsonModel;
import com.crone.yandexmobdev.utils.ConstantManager;
import com.crone.yandexmobdev.utils.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.util.Log;

public class ArtistsFragment extends Fragment {

    private View mView;
    private static Bundle mBundleRecyclerViewState;
    private RecyclerView mRecyvleView;
    private ArrayList<ParseJsonModel> mDataList = new ArrayList<>();

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_artists, container, false);
        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // сохраняем состояние RecyvleView при повороте экрана
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyvleView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(ConstantManager.LIST_STATE_KEY, listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // возвращаем состояние при повороте
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(ConstantManager.LIST_STATE_KEY);
            mRecyvleView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }


    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyvleView = (RecyclerView) mView.findViewById(R.id.recycle);
        mRecyvleView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mDataList.size() != 0) {
                            Intent intent = new Intent(getActivity(), DetailActivity.class);
                            intent.putExtra(ConstantManager.NAME_DETAIL, mDataList.get(position).name);
                            intent.putExtra(ConstantManager.GENRES_DETAIL, mDataList.get(position).geners);
                            intent.putExtra(ConstantManager.TRACKS_DETAIL, mDataList.get(position).tracks);
                            intent.putExtra(ConstantManager.ALBUMS_DETAIL, mDataList.get(position).albums);
                            // парсим большую иконку и описание из локального JSON.
                            intent.putExtra(ConstantManager.BIGICON_DETAIL, getBigIconFromCache(mDataList.get(position).id));
                            intent.putExtra(ConstantManager.DESC_DETAIL, getDescriptionFromCache(mDataList.get(position).id));
                            startActivity(intent);
                        }
                    }
                })
        );

        if (savedInstanceState == null) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mView.getContext());
            mRecyvleView.setLayoutManager(mLayoutManager);
        }

        final ArtistAdapter rvAdapter = new ArtistAdapter(mDataList);
        mRecyvleView.setAdapter(rvAdapter);

        final ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);

        final RequestQueue requestQueue = Volley.newRequestQueue(mView.getContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(ConstantManager.URLJSON, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            dataToArray(response, i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    // обновляем адаптер
                    rvAdapter.notifyDataSetChanged();

                    // создаем поток в котором будет записываться ответ от сервера в файл кеша.
                    if (savedInstanceState == null) {
                        final String lastResponse = response.toString();
                        Handler mHandler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                createAndSaveFile(ConstantManager.CACHEFILE, lastResponse);
                            }
                        };
                        mHandler.post(runnable);
                    }
                    // после обработки данных прячем прогресс бар.
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // если ошибка при парсинке, то берем локальную версию JSON.
                if (savedInstanceState == null)
                    setDataFromCache(rvAdapter);
                progressBar.setVisibility(View.GONE);
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    /**
     * создает файл в кэше с последним ответом от сервера.
     *
     * @param params        - имя файла кэша.
     * @param mJsonResponse - полученный JSON от сервера.
     */
    public void createAndSaveFile(String params, String mJsonResponse) {
        try {
            FileWriter file = new FileWriter(getActivity().getCacheDir() + "/" + params);
            file.write(mJsonResponse);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * считываем файл в котором хранится последний ответ в JSON.
     *
     * @param params - имя файла кэша.
     * @return - возвращает JSON из файла кэша.
     */
    private String readJsonData(String params) {
        String mResponse = null;
        try {
            File f = new File(getActivity().getCacheDir() + "/" + params);
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            mResponse = new String(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mResponse;
    }

    /**
     * в ArrayList заносится информация с локального JSON и передается в RecycleView.
     *
     * @param adapter - в какой адаптер будет заноситься информация.
     */
    private void setDataFromCache(ArtistAdapter adapter) {
        String responseFromCache = readJsonData(ConstantManager.CACHEFILE);
        if (responseFromCache != null) {
            try {
                JSONArray jsonArray = new JSONArray(responseFromCache);
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataToArray(jsonArray, i);
                }
                // обновляем адаптер
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } else {
            // TODO добавить сообщение на отсутствие интернета
            Toast.makeText(getActivity(), R.string.internet_issue, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Заносим распарсенные данные в ArrayList
     *
     * @param jsonArray - передаем данные для последующей обработки
     * @param i         - передаем номер текущей длины jsonarray
     * @throws JSONException
     */
    private void dataToArray(JSONArray jsonArray, int i) throws JSONException {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        ParseJsonModel model = new ParseJsonModel();
        // парсим айди
        if (!jsonObject.isNull(ConstantManager.ID)) {
            model.id = jsonObject.getInt(ConstantManager.ID);
        }
        // парсим жанры
        if (!jsonObject.isNull(ConstantManager.GENRES)) {
            JSONArray genres = jsonObject.optJSONArray(ConstantManager.GENRES);
            if (genres != null) {
                // обнуляем переменную жанры
                model.geners = "";
                for (int x = 0; x < genres.length(); x++) {
                    model.geners += genres.optString(x);
                    // добавляем запятую
                    if (x != genres.length() - 1) model.geners += ", ";
                }
            }
        }
        // парсим имя
        if (!jsonObject.isNull(ConstantManager.NAME)) {
            model.name = jsonObject.getString(ConstantManager.NAME);
        }
        // парсим ссылку на изображение 300х300
        if (!jsonObject.isNull(ConstantManager.COVER)) {
            model.urlcover = jsonObject.getString(ConstantManager.COVER);
            JSONObject jObject = new JSONObject(model.urlcover);
            model.urlcover = jObject.getString(ConstantManager.SMALLICON);
        }
        // парсим количество треков
        if (!jsonObject.isNull(ConstantManager.TRACKS)) {
            model.tracks = jsonObject.getInt(ConstantManager.TRACKS);
        }
        // парсим количество альбомов
        if (!jsonObject.isNull(ConstantManager.ALBUMS)) {
            model.albums = jsonObject.getInt(ConstantManager.ALBUMS);
        }
        // заполняем лист нашей моделью
        mDataList.add(i, model);
    }


    /**
     * Получаем ссылку на Big Icon из локального JSON, чтобы не нагружать лишний раз сеть.
     *
     * @param id - указываем ID исполнителя на которого нажали.
     * @return - возвращает ссылку на big icon.
     */
    private String getBigIconFromCache(int id) {
        String bigIcon = null;
        String responseFromCache = readJsonData(ConstantManager.CACHEFILE);
        int g = 0;
        if (responseFromCache != null) {
            try {
                JSONArray jsonArray = new JSONArray(responseFromCache);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (!jsonObject.isNull(ConstantManager.ID)) {
                        g = jsonObject.getInt(ConstantManager.ID);
                        if (g == id) {
                            bigIcon = jsonObject.getString(ConstantManager.COVER);
                            JSONObject jObject = new JSONObject(bigIcon);
                            bigIcon = jObject.getString(ConstantManager.BIGICON);
                            break;
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return bigIcon;
    }

    /**
     * Получаем описание из локального JSON по клику на айтем RecycleView.
     *
     * @param id - указываем ID исполнителя на которого нажали.
     * @return - описание исполнителя
     */
    private String getDescriptionFromCache(int id) {
        String desc = null;
        String responseFromCache = readJsonData(ConstantManager.CACHEFILE);
        int g = 0;
        if (responseFromCache != null) {
            try {
                JSONArray jsonArray = new JSONArray(responseFromCache);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (!jsonObject.isNull(ConstantManager.ID)) {
                        g = jsonObject.getInt(ConstantManager.ID);
                        if (g == id) {
                            desc = jsonObject.getString(ConstantManager.DESCRIPTION);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return desc;
    }


}
