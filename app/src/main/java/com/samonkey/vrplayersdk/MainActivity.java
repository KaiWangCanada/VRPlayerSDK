package com.samonkey.vrplayersdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
//    public static String URL = "http://demand.samonkey.com/media/4/t1920.mp4";
    public static String URL = "http://demand.samonkey.com/media/4/ta1.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_main_normal:
                startActivity(new Intent(this, NormalActivity.class));
                break;
            case R.id.btn_main_vr:
                startActivity(new Intent(this, VRActivity.class));
                break;
        }
    }
}
