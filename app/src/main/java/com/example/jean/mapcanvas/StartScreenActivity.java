package com.example.jean.mapcanvas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by i on 2019-01-17.
 */

public class StartScreenActivity extends AppCompatActivity {
    ImageView Hansel_and_Gretel_IMG;
    ImageView Hansel_and_Gretel_TITLE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startscreen);

        Hansel_and_Gretel_IMG = findViewById(R.id.image);
        Hansel_and_Gretel_TITLE = findViewById(R.id.title);
    }
}
