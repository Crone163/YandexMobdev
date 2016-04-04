package com.crone.yandexmobdev.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.crone.yandexmobdev.R;
import com.crone.yandexmobdev.fragments.ArtistsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity, ArtistsFragment.newInstance())
                .commit();
    }
}