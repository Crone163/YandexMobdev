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


import java.util.ArrayList;

import android.widget.ProgressBar;
import android.widget.Toast;

public class ArtistsFragment extends Fragment {

    private static Bundle mBundleRecyclerViewState;
    private RecyclerView mRecycleView;
    private ArrayList<ParseJsonModel> mDataList = new ArrayList<>();
    private ArtistAdapter mArtistAdapter;
    private RequestQueue mRequestQueue;

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // сохраняем состояние RecycleView
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecycleView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(ConstantManager.LIST_STATE_KEY, listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // возвращаем состояние
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(ConstantManager.LIST_STATE_KEY);
            mRecycleView.getLayoutManager().onRestoreInstanceState(listState);
            // Если свернули приложение, чтобы включить интернет, то после возвращения делаем заново запрос.
            if (mDataList.size() == 0) {
                setRequestQueue(mRequestQueue, mArtistAdapter);
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleView = (RecyclerView) view.findViewById(R.id.recycle);

        mRequestQueue = Volley.newRequestQueue(getContext());

        mRecycleView.addOnItemTouchListener(
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
                            intent.putExtra(ConstantManager.BIGICON_DETAIL, getBigIconFromCache(mDataList.get(position).id, mRequestQueue));
                            intent.putExtra(ConstantManager.DESC_DETAIL, getDescriptionFromCache(mDataList.get(position).id, mRequestQueue));
                            startActivity(intent);
                        }
                    }
                })
        );

        if (savedInstanceState != null) {
            mDataList = savedInstanceState.getParcelableArrayList(ConstantManager.PARCABLE_SAVE);
        }

        mArtistAdapter = new ArtistAdapter(mDataList);
        mRecycleView.setAdapter(mArtistAdapter);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(mLayoutManager);

        if (savedInstanceState == null) {
            setRequestQueue(mRequestQueue, mArtistAdapter);
        }
    }

    // сохраняем ArrayList в Bundle
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ConstantManager.PARCABLE_SAVE, mDataList);
    }

    /**
     * Используется в приложении при первом запуске, парсится с интернета JSON, если ошибка, то берем JSON из кэша.
     *
     * @param requestQueue - передаем Volley request.
     * @param rvAdapter    - передаем ArtistAdapter.
     */
    private void setRequestQueue(final RequestQueue requestQueue, final ArtistAdapter rvAdapter) {
        final ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
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
                    // после обработки данных прячем прогресс бар.
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (requestQueue.getCache().get(ConstantManager.URLJSON) != null) {
                    setDataFromCache(rvAdapter, new String(requestQueue.getCache().get(ConstantManager.URLJSON).data));
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), R.string.internet_issue, Toast.LENGTH_SHORT).show();
                }
            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    /**
     * в ArrayList заносится информация с локального JSON и передается в RecycleView.
     *
     * @param adapter - в какой адаптер будет заноситься информация.
     */
    private void setDataFromCache(ArtistAdapter adapter, String cacheRequest) {
        if (cacheRequest != null) {
            try {
                JSONArray jsonArray = new JSONArray(cacheRequest);
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataToArray(jsonArray, i);
                }
                // обновляем адаптер
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
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
    private String getBigIconFromCache(int id, RequestQueue requestQueue) {
        String bigIcon = new String(requestQueue.getCache().get(ConstantManager.URLJSON).data);
        try {
            JSONArray jsonArray = new JSONArray(bigIcon);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull(ConstantManager.ID)) {
                    if (jsonObject.getInt(ConstantManager.ID) == id) {
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
        return bigIcon;
    }

    /**
     * Получаем описание из локального JSON по клику на айтем RecycleView.
     *
     * @param id - указываем ID исполнителя на которого нажали.
     * @return - описание исполнителя
     */
    private String getDescriptionFromCache(int id, RequestQueue requestQueue) {
        String desc = new String(requestQueue.getCache().get(ConstantManager.URLJSON).data);
        try {
            JSONArray jsonArray = new JSONArray(desc);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull(ConstantManager.ID)) {
                    if (jsonObject.getInt(ConstantManager.ID) == id) {
                        desc = jsonObject.getString(ConstantManager.DESCRIPTION);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return desc;
    }


}
