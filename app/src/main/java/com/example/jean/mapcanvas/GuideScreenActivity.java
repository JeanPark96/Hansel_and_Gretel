package com.example.jean.mapcanvas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by i on 2019-03-08.
 */

public class GuideScreenActivity extends AppCompatActivity {
    public static ImageView guideScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("사용법");
        setContentView(R.layout.activity_guide);

        guideScreen = (ImageView)findViewById(R.id.guideScreen);
    }

    public void onClick(View view){
        AlertDialog.Builder guideAlert = new AlertDialog.Builder(GuideScreenActivity.this);
        guideAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent intent = new Intent(GuideScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        guideAlert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        guideAlert.setMessage("길 기록을 시작하시겠습니까?");
        guideAlert.show();
    }
}
