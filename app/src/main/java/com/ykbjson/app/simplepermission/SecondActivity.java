package com.ykbjson.app.simplepermission;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(this, TestFragment.class.getName(), getIntent().getExtras()))
                .commit();
    }
}
