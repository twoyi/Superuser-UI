package com.koushikdutta.superuser;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

public class ActivityLog extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
}
