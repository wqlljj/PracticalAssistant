package com.example.sx.practicalassistant.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.sx.practicalassistant.app.BaseContants;

/**
 * Created by cloud on 2018/11/1.
 */

public class BaseActivity extends AppCompatActivity {
    public int lifeState=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        lifeState= BaseContants.ONCREATE;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        lifeState= BaseContants.ONSTART;
        super.onStart();
    }

    @Override
    protected void onStop() {
        lifeState= BaseContants.ONSTOP;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifeState= BaseContants.ONDESTROY;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        lifeState= BaseContants.ONPAUSE;
        super.onPause();
    }

    @Override
    protected void onResume() {
        lifeState= BaseContants.ONRESUME;
        super.onResume();
    }
}
